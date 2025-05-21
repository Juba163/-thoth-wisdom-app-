package com.thoth.wisdom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val type: UserDataType,
    val size: Long,
    val uploadDate: Date = Date(),
    val processed: Boolean = false,
    val url: String? = null
)

enum class UserDataType {
    PDF,
    WORD,
    IMAGE,
    LINK
}
