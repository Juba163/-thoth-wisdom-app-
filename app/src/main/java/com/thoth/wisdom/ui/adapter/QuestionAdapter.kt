package com.thoth.wisdom.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thoth.wisdom.data.model.PhilosophyQuestion
import com.thoth.wisdom.databinding.ItemQuestionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class QuestionAdapter(
    private val onItemClick: (PhilosophyQuestion) -> Unit
) : ListAdapter<PhilosophyQuestion, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QuestionViewHolder(
        private val binding: ItemQuestionBinding,
        private val onItemClick: (PhilosophyQuestion) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ar"))

        fun bind(question: PhilosophyQuestion) {
            binding.questionText.text = question.question
            binding.questionDate.text = dateFormat.format(question.timestamp)
            
            binding.root.setOnClickListener {
                onItemClick(question)
            }
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<PhilosophyQuestion>() {
        override fun areItemsTheSame(oldItem: PhilosophyQuestion, newItem: PhilosophyQuestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhilosophyQuestion, newItem: PhilosophyQuestion): Boolean {
            return oldItem == newItem
        }
    }
}
