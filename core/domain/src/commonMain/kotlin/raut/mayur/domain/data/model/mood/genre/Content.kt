package raut.mayur.domain.data.model.mood.genre

import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.data.type.HomeContentType

data class Content(
    val playlistBrowseId: String,
    val thumbnail: List<Thumbnail>?,
    val title: Title,
) : HomeContentType