package com.thoth.wisdom.data.repository

import android.util.Log
import com.thoth.wisdom.data.dao.PhilosophyDao
import com.thoth.wisdom.data.model.PhilosophyAnswer
import com.thoth.wisdom.data.model.PhilosophyQuestion
import com.thoth.wisdom.data.model.Source
import com.thoth.wisdom.data.model.SourceType
import com.thoth.wisdom.network.OpenAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Date

class PhilosophyRepository(
    private val philosophyDao: PhilosophyDao,
    private val openAIService: OpenAIService
) {
    private val TAG = "PhilosophyRepository"
    private val API_KEY = "Bearer sk-..." // سيتم استبداله بمفتاح API حقيقي من إعدادات المستخدم
    
    // الحصول على جميع الأسئلة
    fun getAllQuestions(): Flow<List<PhilosophyQuestion>> {
        return philosophyDao.getAllQuestions()
    }
    
    // الحصول على الأسئلة المفضلة
    fun getFavoriteQuestions(): Flow<List<PhilosophyQuestion>> {
        return philosophyDao.getFavoriteQuestions()
    }
    
    // البحث عن أسئلة
    fun searchQuestions(query: String): Flow<List<PhilosophyQuestion>> {
        return philosophyDao.searchQuestions(query)
    }
    
    // طرح سؤال فلسفي جديد
    suspend fun askPhilosophyQuestion(questionText: String, category: String? = null): Pair<PhilosophyQuestion, PhilosophyAnswer> {
        return withContext(Dispatchers.IO) {
            try {
                // حفظ السؤال في قاعدة البيانات
                val question = PhilosophyQuestion(
                    question = questionText,
                    category = category,
                    timestamp = Date()
                )
                val questionId = philosophyDao.insertQuestion(question)
                
                // إعداد طلب OpenAI
                val requestBody = mapOf(
                    "model" to "gpt-4o",
                    "messages" to listOf(
                        mapOf(
                            "role" to "system",
                            "content" to "أنت نموذج لغوي متخصص في علم الفلسفة بكافة فروعها والمنطق ونظرية المعرفة القديمة والحديثة والمعاصرة. قدم إجابات دقيقة ومفصلة مع ذكر المصادر والمراجع."
                        ),
                        mapOf(
                            "role" to "user",
                            "content" to questionText
                        )
                    ),
                    "temperature" to 0.7,
                    "max_tokens" to 2000
                )
                
                // إرسال الطلب إلى OpenAI
                val response = openAIService.askPhilosophyQuestion(API_KEY, requestBody)
                
                // معالجة الاستجابة
                val choices = response["choices"] as List<Map<String, Any>>
                val message = choices[0]["message"] as Map<String, Any>
                val content = message["content"] as String
                
                // استخراج المصادر (في تطبيق حقيقي، سنستخدم معالجة أكثر تعقيدًا)
                val sources = extractSources(content)
                
                // إنشاء كائن الإجابة
                val answer = PhilosophyAnswer(
                    questionId = questionId,
                    answer = content,
                    timestamp = Date(),
                    sources = sources
                )
                
                // إرجاع السؤال والإجابة
                Pair(question.copy(id = questionId), answer)
            } catch (e: Exception) {
                Log.e(TAG, "Error asking philosophy question", e)
                throw e
            }
        }
    }
    
    // تحديث حالة المفضلة للسؤال
    suspend fun toggleFavorite(question: PhilosophyQuestion) {
        withContext(Dispatchers.IO) {
            philosophyDao.updateQuestion(question.copy(isFavorite = !question.isFavorite))
        }
    }
    
    // استخراج المصادر من النص (مبسط)
    private fun extractSources(content: String): List<Source> {
        val sources = mutableListOf<Source>()
        
        // هذه طريقة مبسطة، في التطبيق الحقيقي سنستخدم معالجة أكثر تعقيدًا
        if (content.contains("المصادر:", ignoreCase = true) || 
            content.contains("المراجع:", ignoreCase = true)) {
            
            val sourceSection = content.split(Regex("المصادر:|المراجع:", RegexOption.IGNORE_CASE))[1]
            val sourceLines = sourceSection.split("\n")
            
            for (line in sourceLines) {
                if (line.trim().isNotEmpty()) {
                    sources.add(
                        Source(
                            title = line.trim(),
                            type = determineSourceType(line),
                            details = null
                        )
                    )
                }
            }
        }
        
        return sources
    }
    
    // تحديد نوع المصدر (مبسط)
    private fun determineSourceType(source: String): SourceType {
        return when {
            source.contains("كتاب", ignoreCase = true) -> SourceType.BOOK
            source.contains("مقال", ignoreCase = true) -> SourceType.ARTICLE
            source.contains("http", ignoreCase = true) || 
            source.contains("www", ignoreCase = true) -> SourceType.WEBSITE
            else -> SourceType.BOOK // افتراضي
        }
    }
}
