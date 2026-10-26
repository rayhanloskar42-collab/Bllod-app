package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Int): UserEntity?

    @Query("SELECT * FROM users ORDER BY isAvailable DESC, registeredAt DESC")
    fun getAllDonors(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE bloodGroup = :bloodGroup ORDER BY isAvailable DESC, registeredAt DESC")
    fun getDonorsByBloodGroup(bloodGroup: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isAvailable = :isAvailable WHERE id = :userId")
    suspend fun updateAvailability(userId: Int, isAvailable: Boolean)

    @Query("UPDATE users SET lastDonationDate = :date, totalDonations = totalDonations + 1 WHERE id = :userId")
    suspend fun logDonation(userId: Int, date: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
