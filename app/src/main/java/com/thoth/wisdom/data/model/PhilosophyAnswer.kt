package com.thoth.wisdom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

data class PhilosophyAnswer(
    val id: Long = 0,
    val questionId: Long,
    val answer: String,
    val timestamp: Date = Date(),
    val sources: List<Source> = emptyList()
)
