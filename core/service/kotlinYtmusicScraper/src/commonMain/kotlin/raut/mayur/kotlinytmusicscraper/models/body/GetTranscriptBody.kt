package raut.mayur.kotlinytmusicscraper.models.body

import raut.mayur.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)