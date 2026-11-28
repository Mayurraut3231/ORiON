package raut.mayur.data.parser

import raut.mayur.domain.data.model.browse.album.AlbumBrowse
import raut.mayur.domain.data.model.browse.album.Track
import raut.mayur.domain.data.model.browse.artist.ResultAlbum
import raut.mayur.domain.data.model.searchResult.songs.Album
import raut.mayur.domain.data.model.searchResult.songs.Artist
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.utils.LIVE_RESULT_TYPE
import raut.mayur.kotlinytmusicscraper.pages.AlbumPage
import raut.mayur.logger.Logger

internal fun parseAlbumData(data: AlbumPage): AlbumBrowse {
    val artist: ArrayList<Artist> = arrayListOf()
    Logger.d("AlbumParser", "Parsing album ${data.album.title}: ${data.songs.size} songs")
    data.album.artists?.forEach {
        artist.add(Artist(it.id, it.name))
    }
    val songs: ArrayList<Track> = arrayListOf()
    data.songs.forEach { songItem ->
        songs.add(
            Track(
                album =
                    Album(
                        id = data.album.id,
                        name = data.album.title,
                    ),
                artists =
                    songItem.artists.map { artistItem ->
                        Artist(
                            id = artistItem.id,
                            name = artistItem.name,
                        )
                    },
                duration =
                    if (songItem.duration != null) {
                        "%02d:%02d".format(
                            (songItem.duration ?: 0) / 60,
                            (songItem.duration ?: 0) % 60,
                        )
                    } else {
                        ""
                    },
                durationSeconds = songItem.duration ?: 0,
                isAvailable = false,
                isExplicit = songItem.explicit,
                likeStatus = "INDIFFERENT",
                thumbnails = songItem.thumbnails?.thumbnails?.toListThumbnail() ?: listOf(),
                title = songItem.title,
                videoId = songItem.id,
                videoType = songItem.musicVideoType,
                category = null,
                feedbackTokens = null,
                resultType = if (songItem.isLive) LIVE_RESULT_TYPE else null,
                year = data.album.year.toString(),
            ),
        )
    }

    return AlbumBrowse(
        artists = artist,
        audioPlaylistId = data.album.playlistId,
        description = data.description ?: "",
        duration = data.duration ?: "",
        durationSeconds = 0,
        thumbnails = data.thumbnails?.thumbnails?.toListThumbnail() ?: listOf(),
        title = data.album.title,
        trackCount = songs.size,
        tracks = songs,
        type = "Album",
        year = data.album.year.toString(),
        otherVersion =
            data.otherVersion.map {
                ResultAlbum(
                    browseId = it.browseId,
                    isExplicit = it.explicit,
                    thumbnails = listOf(Thumbnail(800, it.thumbnail, 800)),
                    title = it.title,
                    year = data.album.year.toString(),
                )
            },
    )
}