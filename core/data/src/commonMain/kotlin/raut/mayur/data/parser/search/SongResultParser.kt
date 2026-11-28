package raut.mayur.data.parser.search

import raut.mayur.domain.data.model.searchResult.songs.Album
import raut.mayur.domain.data.model.searchResult.songs.SongsResult
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.utils.LIVE_RESULT_TYPE
import raut.mayur.kotlinytmusicscraper.models.SongItem
import raut.mayur.kotlinytmusicscraper.pages.SearchResult

internal fun parseSearchSong(result: SearchResult): ArrayList<SongsResult> {
    val songsResult: ArrayList<SongsResult> = arrayListOf()
    result.items.forEach {
        val song = it as SongItem
        songsResult.add(
            SongsResult(
                album =
                    if (song.album != null) {
                        Album(
                            id = song.album!!.id,
                            name = song.album!!.name,
                        )
                    } else {
                        null
                    },
                artists =
                    song.artists.map { artistItem ->
                        raut.mayur.domain.data.model.searchResult.songs.Artist(
                            id = artistItem.id,
                            name = artistItem.name,
                        )
                    },
                category = "Song",
                duration = if (song.duration != null) "%02d:%02d".format(song.duration!! / 60, song.duration!! % 60) else "",
                durationSeconds = song.duration ?: 0,
                feedbackTokens = null,
                isExplicit = song.explicit,
                resultType = if (song.isLive) LIVE_RESULT_TYPE else "Song",
                thumbnails = listOf(Thumbnail(544, Regex("([wh])120").replace(song.thumbnail, "$1544"), 544)),
                title = song.title,
                videoId = song.id,
                videoType = song.musicVideoType,
                year = "",
            ),
        )
    }
    return songsResult
}