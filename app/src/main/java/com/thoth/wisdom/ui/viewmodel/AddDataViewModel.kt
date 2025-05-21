package com.thoth.wisdom.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thoth.wisdom.data.model.SelectedFile
import com.thoth.wisdom.data.model.UserData
import com.thoth.wisdom.data.repository.UserDataRepository
import kotlinx.coroutines.launch
import java.util.UUID

class AddDataViewModel(
    private val userDataRepository: UserDataRepository,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _selectedFiles = MutableLiveData<List<SelectedFile>>(emptyList())
    val selectedFiles: LiveData<List<SelectedFile>> = _selectedFiles

    private val _uploadProgress = MutableLiveData<Int>(0)
    val uploadProgress: LiveData<Int> = _uploadProgress

    private val _uploadSuccess = MutableLiveData<Boolean>(false)
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _uploadError = MutableLiveData<String>("")
    val uploadError: LiveData<String> = _uploadError

    fun addSelectedFile(uri: Uri) {
        val fileName = getFileName(uri)
        val fileSize = getFileSize(uri)
        
        val currentList = _selectedFiles.value?.toMutableList() ?: mutableListOf()
        currentList.add(SelectedFile(uri, fileName, fileSize))
        _selectedFiles.value = currentList
    }

    fun removeSelectedFile(position: Int) {
        val currentList = _selectedFiles.value?.toMutableList() ?: mutableListOf()
        if (position in currentList.indices) {
            currentList.removeAt(position)
            _selectedFiles.value = currentList
        }
    }

    fun hasSelectedFiles(): Boolean {
        return !_selectedFiles.value.isNullOrEmpty()
    }

    fun addLink(url: String) {
        viewModelScope.launch {
            try {
                _uploadProgress.value = 50
                
                // استخراج عنوان من الرابط
                val title = extractTitleFromUrl(url)
                
                // حفظ الرابط
                userDataRepository.saveLink(url, title)
                
                _uploadProgress.value = 100
                _uploadSuccess.value = true
            } catch (e: Exception) {
                _uploadError.value = "حدث خطأ أثناء حفظ الرابط: ${e.message}"
                _uploadProgress.value = 0
            }
        }
    }

    fun uploadFiles() {
        viewModelScope.launch {
            try {
                val files = _selectedFiles.value ?: return@launch
                
                _uploadProgress.value = 10
                
                // رفع كل ملف على حدة
                files.forEachIndexed { index, selectedFile ->
                    val progress = ((index + 1) * 100) / files.size
                    _uploadProgress.value = progress
                    
                    userDataRepository.saveFileFromUri(selectedFile.uri, selectedFile.name)
                }
                
                _uploadSuccess.value = true
            } catch (e: Exception) {
                _uploadError.value = "حدث خطأ أثناء رفع الملفات: ${e.message}"
                _uploadProgress.value = 0
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = ""
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }
        if (result.isEmpty()) {
            result = uri.path?.substringAfterLast('/') ?: "file_${UUID.randomUUID()}"
        }
        return result
    }

    private fun getFileSize(uri: Uri): Long {
        var result = 0L
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        result = cursor.getLong(sizeIndex)
                    }
                }
            }
        }
        if (result == 0L) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                result = inputStream.available().toLong()
            }
        }
        return result
    }

    private fun extractTitleFromUrl(url: String): String {
        // استخراج مبسط للعنوان من الرابط
        return try {
            val domain = url.substringAfter("://").substringBefore("/")
            val path = url.substringAfter(domain).substringBefore("?").substringBefore("#")
            if (path.isNotEmpty() && path != "/") {
                path.substringAfterLast('/').ifEmpty { domain }
            } else {
                domain
            }
        } catch (e: Exception) {
            "رابط: $url"
        }
    }
}
