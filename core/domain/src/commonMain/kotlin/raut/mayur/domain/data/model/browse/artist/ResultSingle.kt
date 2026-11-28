package raut.mayur.domain.data.model.browse.artist

import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.data.type.HomeContentType

data class ResultSingle(
    val browseId: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType