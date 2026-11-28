package raut.mayur.kotlinytmusicscraper.pages

import raut.mayur.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)