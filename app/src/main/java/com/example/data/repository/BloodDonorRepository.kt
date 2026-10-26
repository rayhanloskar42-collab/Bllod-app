package com.example.data.repository

import android.util.Log
import com.example.data.dao.BloodRequestDao
import com.example.data.dao.UserDao
import com.example.data.model.BloodRequestEntity
import com.example.data.model.UserEntity
import com.example.data.remote.api.InsForgeApiService
import com.example.data.remote.api.InsForgeClient
import com.example.data.remote.model.toInsForgeDto
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BloodDonorRepository(
    private val userDao: UserDao,
    private val bloodRequestDao: BloodRequestDao,
    private val sessionManager: SessionManager,
    private val apiService: InsForgeApiService = InsForgeClient.apiService
) {
    val allDonors: Flow<List<UserEntity>> = userDao.getAllDonors()
    val allRequests: Flow<List<BloodRequestEntity>> = bloodRequestDao.getAllRequests()

    fun getUserById(userId: Int): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun getUserByIdSync(userId: Int): UserEntity? = userDao.getUserByIdSync(userId)

    suspend fun getUserByPhone(phone: String): UserEntity? = userDao.getUserByPhone(phone)

    /**
     * Syncs latest donors and blood requests from InsForge live cloud database
     */
    suspend fun syncFromCloud() = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch remote users/donors
            val remoteUsers = apiService.getAllUsers()
            if (remoteUsers.isNotEmpty()) {
                val entities = remoteUsers.map { it.toEntity() }
                userDao.insertUsers(entities)
            }

            // 2. Fetch remote blood requests
            val remoteRequests = apiService.getAllBloodRequests()
            if (remoteRequests.isNotEmpty()) {
                val reqEntities = remoteRequests.map { it.toEntity() }
                bloodRequestDao.insertRequests(reqEntities)
            }
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Cloud sync failed (offline or network error): ${e.message}")
        }
    }

    suspend fun getHomeVideoUrl(): String = withContext(Dispatchers.IO) {
        try {
            val strategy = apiService.getDownloadStrategy(
                bucket = "ontro",
                objectKey = "Medical_video_about_blood_donation_20261026123030.mp4"
            )
            strategy.url ?: DEFAULT_VIDEO_URL
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to fetch video strategy from InsForge: ${e.message}")
            DEFAULT_VIDEO_URL
        }
    }

    companion object {
        const val DEFAULT_VIDEO_URL = "https://cdn.insforge.dev/storage/99cuxkhf/ontro/Medical_video_about_blood_donation_20261026123030.mp4?v=954617201079a1392adf92c210f2d1c9&Expires=1793664000&Key-Pair-Id=K3DD3AWDLYT9QW&Signature=xt4WMWiI0q4kfuFysslfmrRPz05MUrxweVNrbeZh2XkzVcDZy5r1bGvcxFdq1qfgFv0D8mak6ilgepxmMLlyPhHLBaUHeE4s3Ngmdx1xEIiMzarkES6sU5JoVVMZ6pvg10a4duvIsNBTOqya-do2TPF-QnuUn9EQHPnKmMAEOnit5Hg2dwwr-RbsuwjxNdC189greKjBW-jSsHBTVUqUZXBkZ5EGDBjH~P3xV6AVt58Dpm5ZOOZxzpYN3iso3N~ZIQC9RecxcYN3g-ONDZtmQxhfk2e~AnTVrYyJ9QN9a7AQBfn1dauBDWbmoXSGxptx7hKAC0kJDFJQC8rJFLk~cQ__"
    }

    suspend fun registerUser(user: UserEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1. Check if user already exists in InsForge cloud
            val remoteExisting = try {
                apiService.getUserByPhone("eq.${user.phoneNumber}")
            } catch (e: Exception) {
                emptyList()
            }

            if (remoteExisting.isNotEmpty()) {
                return@withContext Result.failure(Exception("This phone number is already registered in InsForge. Please login."))
            }

            // 2. Also check local DB
            val localExisting = userDao.getUserByPhone(user.phoneNumber)
            if (localExisting != null) {
                return@withContext Result.failure(Exception("This phone number is already registered. Please login."))
            }

            // 3. Post to InsForge Cloud Database
            val userDto = user.toInsForgeDto()
            val response = apiService.insertUser(userDto)

            if (!response.isSuccessful && response.code() != 201) {
                return@withContext Result.failure(Exception("Failed to register on InsForge server (Code: ${response.code()})"))
            }

            // 4. Retrieve generated ID from cloud or save locally
            val createdUserList = try {
                apiService.getUserByPhone("eq.${user.phoneNumber}")
            } catch (e: Exception) {
                emptyList()
            }

            val savedEntity = if (createdUserList.isNotEmpty()) {
                createdUserList.first().toEntity()
            } else {
                user
            }

            val existingLocal = userDao.getUserByPhone(savedEntity.phoneNumber)
            val finalId = if (existingLocal != null) {
                val updated = savedEntity.copy(id = existingLocal.id)
                userDao.updateUser(updated)
                existingLocal.id.toLong()
            } else {
                val insertedId = userDao.insertUser(savedEntity)
                if (savedEntity.id > 0) savedEntity.id.toLong() else insertedId
            }
            sessionManager.saveUserSession(finalId.toInt())

            Result.success(finalId)
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Registration error: ${e.message}", e)
            Result.failure(Exception(e.message ?: "Registration failed"))
        }
    }

    suspend fun loginUser(phone: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            // 1. Check InsForge live cloud first
            var cloudUser: UserEntity? = null
            try {
                val cloudResults = apiService.getUserByPhone("eq.$phone")
                if (cloudResults.isNotEmpty()) {
                    cloudUser = cloudResults.first().toEntity()
                }
            } catch (e: Exception) {
                Log.w("BloodDonorRepository", "Cloud check skipped: ${e.message}")
            }

            if (cloudUser != null) {
                if (cloudUser.password != password) {
                    return@withContext Result.failure(Exception("Incorrect password. Please try again."))
                }
                // Cache user to local room
                userDao.insertUsers(listOf(cloudUser))
                sessionManager.saveUserSession(cloudUser.id)
                return@withContext Result.success(cloudUser)
            }

            // 2. Fallback to local Room database (if created previously or offline)
            val localUser = userDao.getUserByPhone(phone)
            if (localUser != null) {
                if (localUser.password != password) {
                    return@withContext Result.failure(Exception("Incorrect password. Please try again."))
                }
                sessionManager.saveUserSession(localUser.id)
                return@withContext Result.success(localUser)
            }

            Result.failure(Exception("Account not found with this phone number. Please register first."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getLoggedInUserId(): Int? = sessionManager.getLoggedInUserId()

    fun logout() {
        sessionManager.clearSession()
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        try {
            apiService.updateUser(
                idFilter = "eq.${user.id}",
                updates = mapOf(
                    "phone_number" to user.phoneNumber,
                    "location" to user.location,
                    "age" to user.age
                )
            )
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to update profile on InsForge: ${e.message}")
        }
    }

    suspend fun updateAvailability(userId: Int, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateAvailability(userId, isAvailable)
        try {
            apiService.updateUser(
                idFilter = "eq.$userId",
                updates = mapOf("is_available" to isAvailable)
            )
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to update availability on InsForge: ${e.message}")
        }
    }

    suspend fun logDonation(userId: Int, date: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId)
        val newCount = (user?.totalDonations ?: 0) + 1
        userDao.logDonation(userId, date)
        try {
            apiService.updateUser(
                idFilter = "eq.$userId",
                updates = mapOf(
                    "last_donation_date" to date,
                    "total_donations" to newCount
                )
            )
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to log donation on InsForge: ${e.message}")
        }
    }

    suspend fun createBloodRequest(request: BloodRequestEntity): Long = withContext(Dispatchers.IO) {
        val localId = bloodRequestDao.insertRequest(request)
        try {
            val reqDto = request.copy(id = localId.toInt()).toInsForgeDto()
            apiService.insertBloodRequest(reqDto)
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to post blood request to InsForge: ${e.message}")
        }
        localId
    }

    suspend fun markRequestFulfilled(requestId: Int) = withContext(Dispatchers.IO) {
        bloodRequestDao.markFulfilled(requestId)
        try {
            apiService.updateBloodRequest(
                idFilter = "eq.$requestId",
                updates = mapOf("is_fulfilled" to true)
            )
        } catch (e: Exception) {
            Log.e("BloodDonorRepository", "Failed to mark fulfilled on InsForge: ${e.message}")
        }
    }

    suspend fun deleteBloodRequest(requestId: Int) = withContext(Dispatchers.IO) {
        bloodRequestDao.deleteRequest(requestId)
    }
}
