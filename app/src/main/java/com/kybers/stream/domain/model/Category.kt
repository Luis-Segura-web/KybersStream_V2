package com.kybers.stream.domain.model

data class Category(
    val categoryId: String,
    val categoryName: String,
    val parentId: Int = 0,
    val type: CategoryType
)

enum class CategoryType {
    LIVE_TV,
    VOD,
    SERIES
}