package raut.mayur.domain.data.model.metadata

import raut.mayur.domain.data.model.searchResult.songs.Album
import raut.mayur.domain.data.model.searchResult.songs.Artist
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail

data class MetadataSong(
    val album: Album,
    val artists: List<Artist>,
    val duration: String,
    val durationSeconds: Int,
    val isExplicit: Boolean,
    val lyrics: Lyrics,
    val resultType: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val videoId: String,
    val videoType: String,
    val year: Any,
)