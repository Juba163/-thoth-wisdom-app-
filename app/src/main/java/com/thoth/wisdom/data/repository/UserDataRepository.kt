package com.thoth.wisdom.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.thoth.wisdom.data.dao.UserDataDao
import com.thoth.wisdom.data.model.UserData
import com.thoth.wisdom.data.model.UserDataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.apache.tika.Tika
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.UUID

class UserDataRepository(
    private val userDataDao: UserDataDao,
    private val context: Context
) {
    private val TAG = "UserDataRepository"
    private val tika = Tika()
    
    // الحصول على جميع بيانات المستخدم
    fun getAllUserData(): Flow<List<UserData>> {
        return userDataDao.getAllUserData()
    }
    
    // الحصول على بيانات المستخدم حسب النوع
    fun getUserDataByType(type: UserDataType): Flow<List<UserData>> {
        return userDataDao.getUserDataByType(type)
    }
    
    // الحصول على عدد بيانات المستخدم
    fun getUserDataCount(): Flow<Int> {
        return userDataDao.getUserDataCount()
    }
    
    // الحصول على إجمالي حجم بيانات المستخدم
    fun getTotalUserDataSize(): Flow<Long> {
        return userDataDao.getTotalUserDataSize()
    }
    
    // حفظ ملف من URI
    suspend fun saveFileFromUri(uri: Uri, fileName: String): UserData {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")
                
                // تحديد نوع الملف
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val fileType = when {
                    mimeType.startsWith("application/pdf") -> UserDataType.PDF
                    mimeType.startsWith("application/msword") || 
                    mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") -> UserDataType.WORD
                    mimeType.startsWith("image/") -> UserDataType.IMAGE
                    else -> UserDataType.PDF // افتراضي
                }
                
                // إنشاء مجلد للملفات إذا لم يكن موجودًا
                val filesDir = File(context.filesDir, "user_data")
                if (!filesDir.exists()) {
                    filesDir.mkdirs()
                }
                
                // إنشاء ملف جديد بمعرف فريد
                val uniqueFileName = "${UUID.randomUUID()}_$fileName"
                val file = File(filesDir, uniqueFileName)
                
                // نسخ المحتوى
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                
                // إغلاق المدخل
                inputStream.close()
                
                // إنشاء كائن بيانات المستخدم
                val userData = UserData(
                    name = fileName,
                    path = file.absolutePath,
                    type = fileType,
                    size = file.length(),
                    uploadDate = Date()
                )
                
                // حفظ في قاعدة البيانات
                val id = userDataDao.insertUserData(userData)
                
                // إرجاع الكائن مع المعرف
                userData.copy(id = id)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving file from URI", e)
                throw e
            }
        }
    }
    
    // حفظ رابط
    suspend fun saveLink(url: String, title: String): UserData {
        return withContext(Dispatchers.IO) {
            try {
                // إنشاء كائن بيانات المستخدم للرابط
                val userData = UserData(
                    name = title,
                    path = "",
                    type = UserDataType.LINK,
                    size = 0,
                    uploadDate = Date(),
                    url = url
                )
                
                // حفظ في قاعدة البيانات
                val id = userDataDao.insertUserData(userData)
                
                // إرجاع الكائن مع المعرف
                userData.copy(id = id)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving link", e)
                throw e
            }
        }
    }
    
    // حذف بيانات المستخدم
    suspend fun deleteUserData(userData: UserData) {
        withContext(Dispatchers.IO) {
            try {
                // حذف الملف إذا كان موجودًا
                if (userData.type != UserDataType.LINK && userData.path.isNotEmpty()) {
                    val file = File(userData.path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                
                // حذف من قاعدة البيانات
                userDataDao.deleteUserData(userData)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user data", e)
                throw e
            }
        }
    }
    
    // استخراج محتوى الملف (مبسط)
    suspend fun extractFileContent(userData: UserData): String {
        return withContext(Dispatchers.IO) {
            try {
                when (userData.type) {
                    UserDataType.PDF, UserDataType.WORD -> {
                        val file = File(userData.path)
                        if (file.exists()) {
                            tika.parseToString(file)
                        } else {
                            "لا يمكن قراءة الملف"
                        }
                    }
                    UserDataType.LINK -> {
                        "محتوى الرابط: ${userData.url}"
                    }
                    UserDataType.IMAGE -> {
                        "صورة: ${userData.name}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting file content", e)
                "حدث خطأ أثناء استخراج محتوى الملف"
            }
        }
    }
    
    // تحديث حالة المعالجة
    suspend fun updateProcessingStatus(userData: UserData, processed: Boolean) {
        withContext(Dispatchers.IO) {
            userDataDao.updateUserData(userData.copy(processed = processed))
        }
    }
}
