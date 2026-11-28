package raut.mayur.domain.data.model.searchResult

import raut.mayur.domain.data.type.SearchResultType

data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<SearchResultType>,
)