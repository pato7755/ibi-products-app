package com.task.ibiproductsapp.domain.model

enum class SortOption(val label: String, val key: String) {
    DEFAULT("Default", "default"),
    PRICE_ASC("Price: Low to High", "price_asc"),
    PRICE_DESC("Price: High to Low", "price_desc"),
    RATING("Top Rated", "rating"),
    NAME("Name A-Z", "name");

    fun toApiParams(): Pair<String, String> = when (this) {
        DEFAULT -> "id" to "asc"
        PRICE_ASC -> "price" to "asc"
        PRICE_DESC -> "price" to "desc"
        RATING -> "rating" to "desc"
        NAME -> "title" to "asc"
    }
}