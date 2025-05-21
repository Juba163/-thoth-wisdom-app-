package com.thoth.wisdom.data.model

data class Source(
    val id: Long = 0,
    val title: String,
    val details: String? = null,
    val type: SourceType,
    val url: String? = null
)

enum class SourceType {
    BOOK,
    ARTICLE,
    WEBSITE,
    USER_DOCUMENT,
    USER_IMAGE,
    USER_LINK
}
