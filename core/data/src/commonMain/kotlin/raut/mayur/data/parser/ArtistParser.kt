package raut.mayur.data.parser

import raut.mayur.data.mapping.toYouTubeWatchEndpoint
import raut.mayur.domain.data.model.browse.artist.Albums
import raut.mayur.domain.data.model.browse.artist.ArtistBrowse
import raut.mayur.domain.data.model.browse.artist.Related
import raut.mayur.domain.data.model.browse.artist.ResultAlbum
import raut.mayur.domain.data.model.browse.artist.ResultPlaylist
import raut.mayur.domain.data.model.browse.artist.ResultRelated
import raut.mayur.domain.data.model.browse.artist.ResultSingle
import raut.mayur.domain.data.model.browse.artist.ResultSong
import raut.mayur.domain.data.model.browse.artist.ResultVideo
import raut.mayur.domain.data.model.browse.artist.Singles
import raut.mayur.domain.data.model.browse.artist.Songs
import raut.mayur.domain.data.model.searchResult.songs.Album
import raut.mayur.domain.data.model.searchResult.songs.Artist
import raut.mayur.domain.data.model.searchResult.songs.Thumbnail
import raut.mayur.domain.utils.LIVE_RESULT_TYPE
import raut.mayur.kotlinytmusicscraper.models.AlbumItem
import raut.mayur.kotlinytmusicscraper.models.ArtistItem
import raut.mayur.kotlinytmusicscraper.models.PlaylistItem
import raut.mayur.kotlinytmusicscraper.models.SongItem
import raut.mayur.kotlinytmusicscraper.models.VideoItem
import raut.mayur.kotlinytmusicscraper.pages.ArtistPage
import raut.mayur.logger.Logger

internal fun parseArtistData(data: ArtistPage): ArtistBrowse {
    for (i in data.sections) {
        Logger.d("data", "title: ${i.title}")
        Logger.d("data", "items: ${i.items}")
    }
    val songSection = data.sections.find { it.items.firstOrNull() is SongItem }
    val albumSection =
        data.sections.find { artistSection ->
            artistSection.items.firstOrNull().let { it is AlbumItem && !it.isSingle }
        }
    val singleSection =
        data.sections.find { artistSection ->
            artistSection.items.firstOrNull().let { it is AlbumItem && it.isSingle }
        }
    val videoSection =
        data.sections.find { it.items.firstOrNull() is VideoItem }
    val featuredOnSection =
        data.sections.find { it.items.firstOrNull() is PlaylistItem }
    Logger.w("ArtistParser", "videoSection: ${videoSection?.items}")
    Logger.w("ArtistParser", "featuredOnSection: ${featuredOnSection?.items}")
    val relatedSection = data.sections.find { it.items.firstOrNull() is ArtistItem }
    val listSong: ArrayList<ResultSong> = arrayListOf()
    val listAlbum: ArrayList<ResultAlbum> = arrayListOf()
    val listSingle: ArrayList<ResultSingle> = arrayListOf()
    val listRelated: ArrayList<ResultRelated> = arrayListOf()
    val listVideo: ArrayList<ResultVideo> = arrayListOf()
    val listFeaturedOn: ArrayList<ResultPlaylist> = arrayListOf()
    albumSection?.items?.forEach { album ->
        listAlbum.add(
            ResultAlbum(
                browseId = (album as AlbumItem).browseId,
                isExplicit = false,
                thumbnails = listOf(Thumbnail(544, album.thumbnail, 544)),
                title = album.title,
                year = album.year.toString(),
            ),
        )
    }
    singleSection?.items?.forEach {
        val single = it as AlbumItem
        listSingle.add(
            ResultSingle(
                browseId = single.browseId,
                thumbnails = listOf(Thumbnail(544, single.thumbnail, 544)),
                title = single.title,
                year = single.year.toString(),
            ),
        )
    }
    songSection?.items?.forEach {
        val song = it as SongItem
        listSong.add(
            ResultSong(
                videoId = song.id,
                title = song.title,
                artists =
                    song.artists.map { artist ->
                        Artist(
                            id = artist.id ?: "",
                            name = artist.name,
                        )
                    },
                album = Album(id = song.album?.id ?: "", name = song.album?.name ?: ""),
                likeStatus = "INDIFFERENT",
                thumbnails = listOf(Thumbnail(544, song.thumbnail, 544)),
                isAvailable = true,
                isExplicit = false,
                videoType = song.musicVideoType,
                durationSeconds = song.duration ?: 0,
                isLive = song.isLive,
            ),
        )
    }
    featuredOnSection?.items?.forEach {
        val playlist = it as PlaylistItem
        listFeaturedOn.add(
            ResultPlaylist(
                id = playlist.id,
                author = playlist.author?.name ?: "",
                thumbnails = listOf(Thumbnail(544, playlist.thumbnail, 544)),
                title = playlist.title,
            ),
        )
    }
    relatedSection?.items?.forEach {
        val artist = it as ArtistItem
        listRelated.add(
            ResultRelated(
                browseId = artist.id,
                subscribers = artist.subscribers ?: "",
                thumbnails = listOf(Thumbnail(544, artist.thumbnail, 544)),
                title = artist.title,
            ),
        )
    }
    videoSection?.items?.forEach {
        val video = it as VideoItem
        listVideo.add(
            ResultVideo(
                artists =
                    video.artists.map { artist ->
                        Artist(
                            id = artist.id ?: "",
                            name = artist.name,
                        )
                    },
                category = null,
                duration = null,
                durationSeconds = video.duration,
                resultType = if (video.isLive) LIVE_RESULT_TYPE else null,
                thumbnails = video.thumbnails?.thumbnails?.toListThumbnail(),
                title = video.title,
                videoId = video.id,
                videoType = video.musicVideoType,
                views = video.view,
                year = "",
            ),
        )
    }
    Logger.d("ArtistParser", "listSong: ${listSong.size}")
    Logger.d("ArtistParser", "listAlbum: ${listAlbum.size}")
    Logger.d("ArtistParser", "listSingle: ${listSingle.size}")
    Logger.d("ArtistParser", "listRelated: ${listRelated.size}")
    return ArtistBrowse(
        albums = Albums(browseId = "", results = listAlbum, params = ""),
        channelId = data.artist.id,
        description = data.description,
        name = data.artist.title,
        radioId = data.artist.radioEndpoint?.toYouTubeWatchEndpoint(),
        related = Related(browseId = "", results = listRelated),
        shuffleId = data.artist.shuffleEndpoint?.toYouTubeWatchEndpoint(),
        singles = Singles(browseId = "", params = "", results = listSingle),
        songs = Songs(browseId = songSection?.moreEndpoint?.browseId, results = listSong),
        subscribed = false,
        subscribers = data.subscribers,
        thumbnails = listOf(Thumbnail(617, data.artist.thumbnail.replace("w1483", "w617"), 617)),
        views = data.view,
        video = listVideo,
        videoList = videoSection?.moreEndpoint?.browseId,
        featuredOn = listFeaturedOn,
    )
}