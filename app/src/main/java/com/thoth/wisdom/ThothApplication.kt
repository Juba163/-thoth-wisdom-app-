package com.thoth.wisdom

import android.app.Application
import androidx.room.Room
import com.thoth.wisdom.data.AppDatabase
import com.thoth.wisdom.data.repository.PhilosophyRepository
import com.thoth.wisdom.data.repository.UserDataRepository
import com.thoth.wisdom.network.OpenAIService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ThothApplication : Application() {

    // قاعدة البيانات المحلية
    lateinit var database: AppDatabase
        private set

    // خدمة OpenAI
    lateinit var openAIService: OpenAIService
        private set

    // مستودعات البيانات
    lateinit var philosophyRepository: PhilosophyRepository
        private set
    
    lateinit var userDataRepository: UserDataRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // تهيئة قاعدة البيانات
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "thoth_database"
        ).build()

        // تهيئة خدمة OpenAI
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openAIService = retrofit.create(OpenAIService::class.java)

        // تهيئة المستودعات
        philosophyRepository = PhilosophyRepository(database.philosophyDao(), openAIService)
        userDataRepository = UserDataRepository(database.userDataDao(), applicationContext)
    }
}
