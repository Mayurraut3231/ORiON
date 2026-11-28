package raut.mayur.kotlinytmusicscraper.models.body

import raut.mayur.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)