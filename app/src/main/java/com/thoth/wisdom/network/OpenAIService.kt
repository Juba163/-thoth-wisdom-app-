package com.thoth.wisdom.network

import com.thoth.wisdom.data.model.PhilosophyAnswer
import com.thoth.wisdom.data.model.PhilosophyQuestion
import com.thoth.wisdom.data.model.Source
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {
    
    @POST("chat/completions")
    suspend fun askPhilosophyQuestion(
        @Header("Authorization") apiKey: String,
        @Body requestBody: Map<String, Any>
    ): Map<String, Any>
    
    // واجهة لتحليل الملفات المرفوعة
    @POST("embeddings")
    suspend fun getEmbeddings(
        @Header("Authorization") apiKey: String,
        @Body requestBody: Map<String, Any>
    ): Map<String, Any>
    
    // واجهة لتحليل الروابط
    @POST("chat/completions")
    suspend fun analyzeWebContent(
        @Header("Authorization") apiKey: String,
        @Body requestBody: Map<String, Any>
    ): Map<String, Any>
}
