package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BloodRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodRequestDao {
    @Query("SELECT * FROM blood_requests ORDER BY isFulfilled ASC, createdAt DESC")
    fun getAllRequests(): Flow<List<BloodRequestEntity>>

    @Query("SELECT * FROM blood_requests WHERE bloodGroup = :bloodGroup ORDER BY isFulfilled ASC, createdAt DESC")
    fun getRequestsByBloodGroup(bloodGroup: String): Flow<List<BloodRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BloodRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<BloodRequestEntity>)

    @Update
    suspend fun updateRequest(request: BloodRequestEntity)

    @Query("UPDATE blood_requests SET isFulfilled = 1 WHERE id = :requestId")
    suspend fun markFulfilled(requestId: Int)

    @Query("DELETE FROM blood_requests WHERE id = :requestId")
    suspend fun deleteRequest(requestId: Int)

    @Query("SELECT COUNT(*) FROM blood_requests")
    suspend fun getRequestCount(): Int
}
