package raut.mayur.kotlinytmusicscraper.models.body

import raut.mayur.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String?,
    val params: String?,
)