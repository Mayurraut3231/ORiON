package raut.mayur.domain.data.model.browse.artist

import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.data.type.HomeContentType

data class ResultAlbum(
    val browseId: String,
    val isExplicit: Boolean,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType