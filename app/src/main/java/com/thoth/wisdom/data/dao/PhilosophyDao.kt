package com.thoth.wisdom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.thoth.wisdom.data.model.PhilosophyQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface PhilosophyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: PhilosophyQuestion): Long
    
    @Update
    suspend fun updateQuestion(question: PhilosophyQuestion)
    
    @Query("SELECT * FROM philosophy_questions ORDER BY timestamp DESC")
    fun getAllQuestions(): Flow<List<PhilosophyQuestion>>
    
    @Query("SELECT * FROM philosophy_questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): PhilosophyQuestion?
    
    @Query("SELECT * FROM philosophy_questions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteQuestions(): Flow<List<PhilosophyQuestion>>
    
    @Query("SELECT * FROM philosophy_questions WHERE category = :category ORDER BY timestamp DESC")
    fun getQuestionsByCategory(category: String): Flow<List<PhilosophyQuestion>>
    
    @Query("SELECT * FROM philosophy_questions WHERE question LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchQuestions(query: String): Flow<List<PhilosophyQuestion>>
}
