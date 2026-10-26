package com.example.data.remote.api

import com.example.data.remote.model.InsForgeBloodRequestDto
import com.example.data.remote.model.InsForgeDownloadStrategyDto
import com.example.data.remote.model.InsForgeUserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface InsForgeApiService {

    @GET("api/database/records/users")
    suspend fun getAllUsers(
        @Query("order") order: String = "is_available.desc"
    ): List<InsForgeUserDto>

    @GET("api/database/records/users")
    suspend fun getUserByPhone(
        @Query("phone_number") phoneFilter: String // e.g. "eq.01711223344"
    ): List<InsForgeUserDto>

    @POST("api/database/records/users")
    suspend fun insertUser(
        @Body user: InsForgeUserDto
    ): Response<ResponseBody>

    @PATCH("api/database/records/users")
    suspend fun updateUser(
        @Query("id") idFilter: String, // e.g. "eq.1"
        @Body updates: Map<String, Any>
    ): Response<ResponseBody>

    @GET("api/database/records/blood_requests")
    suspend fun getAllBloodRequests(
        @Query("order") order: String = "created_at.desc"
    ): List<InsForgeBloodRequestDto>

    @POST("api/database/records/blood_requests")
    suspend fun insertBloodRequest(
        @Body request: InsForgeBloodRequestDto
    ): Response<ResponseBody>

    @PATCH("api/database/records/blood_requests")
    suspend fun updateBloodRequest(
        @Query("id") idFilter: String, // e.g. "eq.1"
        @Body updates: Map<String, Any>
    ): Response<ResponseBody>

    @GET("api/storage/buckets/{bucket}/download-strategy/objects/{objectKey}")
    suspend fun getDownloadStrategy(
        @Path("bucket") bucket: String,
        @Path("objectKey", encoded = true) objectKey: String
    ): InsForgeDownloadStrategyDto
}
