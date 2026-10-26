package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CallRecordEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun clearActiveProfiles()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :id")
    suspend fun setActiveProfileById(id: Long)

    @Transaction
    suspend fun switchActiveProfile(id: Long) {
        clearActiveProfiles()
        setActiveProfileById(id)
    }

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}

@Dao
interface SmsMessageDao {
    @Query("SELECT * FROM sms_messages WHERE profileId = :profileId ORDER BY timestamp ASC")
    fun getMessagesForProfile(profileId: Long): Flow<List<SmsMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessageEntity): Long

    @Query("DELETE FROM sms_messages WHERE profileId = :profileId")
    suspend fun deleteMessagesForProfile(profileId: Long)
}

@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getCallRecordsForProfile(profileId: Long): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(record: CallRecordEntity): Long

    @Query("DELETE FROM call_records WHERE profileId = :profileId")
    suspend fun deleteCallRecordsForProfile(profileId: Long)
}
