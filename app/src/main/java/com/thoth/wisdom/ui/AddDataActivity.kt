package com.thoth.wisdom.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thoth.wisdom.R
import com.thoth.wisdom.databinding.ActivityAddDataBinding
import com.thoth.wisdom.ui.adapter.SelectedFileAdapter
import com.thoth.wisdom.ui.viewmodel.AddDataViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class AddDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataBinding
    private lateinit var viewModel: AddDataViewModel
    private lateinit var selectedFileAdapter: SelectedFileAdapter
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.addSelectedFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // إعداد شريط الأدوات
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_new_data)
        
        // إعداد ViewModel
        viewModel = ViewModelProvider(this)[AddDataViewModel::class.java]
        
        // إعداد محول الملفات المختارة
        setupAdapter()
        
        // إعداد المستمعين
        setupListeners()
        
        // مراقبة البيانات
        observeData()
    }
    
    private fun setupAdapter() {
        selectedFileAdapter = SelectedFileAdapter { position ->
            viewModel.removeSelectedFile(position)
        }
        binding.selectedFilesRecycler.apply {
            layoutManager = LinearLayoutManager(this@AddDataActivity)
            adapter = selectedFileAdapter
        }
    }
    
    private fun setupListeners() {
        // منطقة السحب والإفلات
        binding.dragDropArea.setOnClickListener {
            openFilePicker()
        }
        
        // رقائق نوع الملف
        binding.fileTypeChips.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_word -> {
                    binding.urlInputLayout.visibility = View.GONE
                }
                R.id.chip_pdf -> {
                    binding.urlInputLayout.visibility = View.GONE
                }
                R.id.chip_image -> {
                    binding.urlInputLayout.visibility = View.GONE
                }
                R.id.chip_link -> {
                    binding.urlInputLayout.visibility = View.VISIBLE
                }
            }
        }
        
        // زر الرفع
        binding.uploadButton.setOnClickListener {
            if (binding.chip_link.isChecked) {
                val url = binding.urlInput.text.toString().trim()
                if (url.isNotEmpty()) {
                    viewModel.addLink(url)
                } else {
                    Toast.makeText(this, "الرجاء إدخال رابط صالح", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (viewModel.hasSelectedFiles()) {
                    viewModel.uploadFiles()
                } else {
                    Toast.makeText(this, "الرجاء اختيار ملف واحد على الأقل", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun observeData() {
        // مراقبة الملفات المختارة
        viewModel.selectedFiles.observe(this) { files ->
            selectedFileAdapter.submitList(files)
            binding.selectedFilesRecycler.visibility = if (files.isEmpty()) View.GONE else View.VISIBLE
        }
        
        // مراقبة حالة الرفع
        viewModel.uploadProgress.observe(this) { progress ->
            if (progress > 0) {
                binding.progressLayout.visibility = View.VISIBLE
                binding.progressBar.progress = progress
                binding.progressText.text = "جاري الرفع... $progress%"
            } else {
                binding.progressLayout.visibility = View.GONE
            }
        }
        
        // مراقبة نجاح الرفع
        viewModel.uploadSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_upload), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        // مراقبة خطأ الرفع
        viewModel.uploadError.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            
            // تحديد أنواع الملفات المسموح بها حسب الرقاقة المختارة
            val mimeTypes = when {
                binding.chip_word.isChecked -> arrayOf(
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
                binding.chip_pdf.isChecked -> arrayOf("application/pdf")
                binding.chip_image.isChecked -> arrayOf("image/*")
                else -> arrayOf("*/*")
            }
            
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        
        filePickerLauncher.launch(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
