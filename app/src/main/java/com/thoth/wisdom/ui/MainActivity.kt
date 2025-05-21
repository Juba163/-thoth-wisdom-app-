package com.thoth.wisdom.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thoth.wisdom.databinding.ActivityMainBinding
import com.thoth.wisdom.ui.viewmodel.MainViewModel
import android.content.Intent
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.thoth.wisdom.R
import com.thoth.wisdom.ui.adapter.QuestionAdapter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var suggestedQuestionsAdapter: QuestionAdapter
    private lateinit var previousQuestionsAdapter: QuestionAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // إعداد شريط الأدوات
        setSupportActionBar(binding.toolbar)
        
        // إعداد القائمة الجانبية
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.app_name, R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        
        binding.navView.setNavigationItemSelectedListener(this)
        
        // إعداد ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // إعداد محولات العرض
        setupAdapters()
        
        // إعداد المستمعين
        setupListeners()
        
        // مراقبة البيانات
        observeData()
    }
    
    private fun setupAdapters() {
        // إعداد محول الأسئلة المقترحة
        suggestedQuestionsAdapter = QuestionAdapter { question ->
            openQuestionDetail(question.id)
        }
        binding.suggestedQuestionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = suggestedQuestionsAdapter
        }
        
        // إعداد محول الأسئلة السابقة
        previousQuestionsAdapter = QuestionAdapter { question ->
            openQuestionDetail(question.id)
        }
        binding.previousQuestionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = previousQuestionsAdapter
        }
    }
    
    private fun setupListeners() {
        // زر البحث
        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                askQuestion(query)
            }
        }
        
        // زر إضافة بيانات جديدة
        binding.fabAddData.setOnClickListener {
            startActivity(Intent(this, AddDataActivity::class.java))
        }
        
        // رقائق التصفية
        binding.filterChips.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_logic -> viewModel.filterQuestionsByCategory("المنطق")
                R.id.chip_epistemology -> viewModel.filterQuestionsByCategory("نظرية المعرفة")
                R.id.chip_ethics -> viewModel.filterQuestionsByCategory("الأخلاق")
                R.id.chip_metaphysics -> viewModel.filterQuestionsByCategory("الميتافيزيقا")
                else -> viewModel.loadAllQuestions()
            }
        }
    }
    
    private fun observeData() {
        // مراقبة الأسئلة المقترحة
        viewModel.suggestedQuestions.observe(this) { questions ->
            suggestedQuestionsAdapter.submitList(questions)
            binding.suggestedQuestionsRecycler.visibility = if (questions.isEmpty()) View.GONE else View.VISIBLE
        }
        
        // مراقبة الأسئلة السابقة
        viewModel.previousQuestions.observe(this) { questions ->
            previousQuestionsAdapter.submitList(questions)
            binding.previousQuestionsRecycler.visibility = if (questions.isEmpty()) View.GONE else View.VISIBLE
        }
        
        // مراقبة حالة التحميل
        viewModel.isLoading.observe(this) { isLoading ->
            // يمكن إظهار شريط التقدم هنا
        }
    }
    
    private fun askQuestion(question: String) {
        // عرض شاشة التحميل
        // ...
        
        // طلب الإجابة من ViewModel
        viewModel.askQuestion(question)
            .observe(this) { result ->
                // إخفاء شاشة التحميل
                // ...
                
                // فتح شاشة تفاصيل السؤال
                if (result != null) {
                    openQuestionDetail(result.first.id)
                } else {
                    // عرض رسالة خطأ
                }
            }
    }
    
    private fun openQuestionDetail(questionId: Long) {
        val intent = Intent(this, QuestionDetailActivity::class.java).apply {
            putExtra("QUESTION_ID", questionId)
        }
        startActivity(intent)
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // نحن بالفعل في الشاشة الرئيسية
            }
            R.id.nav_favorites -> {
                // فتح شاشة المفضلة
                // startActivity(Intent(this, FavoritesActivity::class.java))
            }
            R.id.nav_my_data -> {
                startActivity(Intent(this, MyDataActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_about -> {
                // فتح شاشة حول التطبيق
                // startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
