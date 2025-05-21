package com.thoth.wisdom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.thoth.wisdom.data.model.PhilosophyAnswer
import com.thoth.wisdom.data.model.PhilosophyQuestion
import com.thoth.wisdom.data.repository.PhilosophyRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(
    private val philosophyRepository: PhilosophyRepository
) : ViewModel() {

    private val _suggestedQuestions = MutableLiveData<List<PhilosophyQuestion>>()
    val suggestedQuestions: LiveData<List<PhilosophyQuestion>> = _suggestedQuestions

    private val _previousQuestions = MutableLiveData<List<PhilosophyQuestion>>()
    val previousQuestions: LiveData<List<PhilosophyQuestion>> = _previousQuestions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadSuggestedQuestions()
        loadAllQuestions()
    }

    fun loadAllQuestions() {
        viewModelScope.launch {
            philosophyRepository.getAllQuestions().collect { questions ->
                _previousQuestions.value = questions
            }
        }
    }

    fun filterQuestionsByCategory(category: String) {
        viewModelScope.launch {
            philosophyRepository.searchQuestions(category).collect { questions ->
                _previousQuestions.value = questions
            }
        }
    }

    private fun loadSuggestedQuestions() {
        // في تطبيق حقيقي، يمكن تحميل هذه من قاعدة البيانات أو من الخادم
        _suggestedQuestions.value = listOf(
            PhilosophyQuestion(1, "ما هي الحقيقة؟", "نظرية المعرفة"),
            PhilosophyQuestion(2, "ما هو الوجود؟", "الميتافيزيقا"),
            PhilosophyQuestion(3, "ما هي العلاقة بين العقل والجسد؟", "فلسفة العقل"),
            PhilosophyQuestion(4, "ما هي الأخلاق؟", "الأخلاق")
        )
    }

    fun askQuestion(question: String): LiveData<Pair<PhilosophyQuestion, PhilosophyAnswer>?> {
        return liveData {
            _isLoading.value = true
            try {
                val result = philosophyRepository.askPhilosophyQuestion(question)
                emit(result)
            } catch (e: Exception) {
                emit(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(question: PhilosophyQuestion) {
        viewModelScope.launch {
            philosophyRepository.toggleFavorite(question)
        }
    }
}
