package raut.mayur.domain.data.model.mediaService

import raut.mayur.domain.data.model.searchResult.songs.Album
import raut.mayur.domain.data.model.searchResult.songs.Artist
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail

data class Song(
    val title: String?,
    val artists: List<Artist>?,
    val duration: Long,
    val lyrics: Any,
    val album: Album,
    val videoId: String,
    val thumbnail: Thumbnail?,
    val isLocal: Boolean,
)