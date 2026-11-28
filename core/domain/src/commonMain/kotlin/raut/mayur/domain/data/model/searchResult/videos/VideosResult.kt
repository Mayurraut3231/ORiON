package raut.mayur.domain.data.model.searchResult.videos

import raut.mayur.domain.data.model.searchResult.songs.Artist
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.data.type.SearchResultType

data class VideosResult(
    val artists: List<Artist>?,
    val category: String?,
    val duration: String?,
    val durationSeconds: Int?,
    val resultType: String?,
    val thumbnails: List<Thumbnail>?,
    val title: String,
    val videoId: String,
    val videoType: String?,
    val views: String?,
    val year: Any,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.VIDEO
}