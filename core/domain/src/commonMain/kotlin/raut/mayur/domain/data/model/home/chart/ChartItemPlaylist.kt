package raut.mayur.domain.data.model.home.chart

import raut.mayur.domain.data.model.browse.artist.ResultPlaylist

data class ChartItemPlaylist(
    val title: String,
    val playlists: List<ResultPlaylist>,
)