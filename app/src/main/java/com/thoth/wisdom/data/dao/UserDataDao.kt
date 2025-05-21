package com.thoth.wisdom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thoth.wisdom.data.model.UserData
import com.thoth.wisdom.data.model.UserDataType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserData): Long
    
    @Update
    suspend fun updateUserData(userData: UserData)
    
    @Delete
    suspend fun deleteUserData(userData: UserData)
    
    @Query("SELECT * FROM user_data ORDER BY uploadDate DESC")
    fun getAllUserData(): Flow<List<UserData>>
    
    @Query("SELECT * FROM user_data WHERE id = :dataId")
    suspend fun getUserDataById(dataId: Long): UserData?
    
    @Query("SELECT * FROM user_data WHERE type = :type ORDER BY uploadDate DESC")
    fun getUserDataByType(type: UserDataType): Flow<List<UserData>>
    
    @Query("SELECT COUNT(*) FROM user_data")
    fun getUserDataCount(): Flow<Int>
    
    @Query("SELECT SUM(size) FROM user_data")
    fun getTotalUserDataSize(): Flow<Long>
    
    @Query("SELECT * FROM user_data WHERE processed = 0")
    fun getUnprocessedUserData(): Flow<List<UserData>>
}
