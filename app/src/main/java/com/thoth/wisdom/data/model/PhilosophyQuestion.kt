package com.thoth.wisdom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "philosophy_questions")
data class PhilosophyQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val category: String? = null,
    val timestamp: Date = Date(),
    val isFavorite: Boolean = false
)
