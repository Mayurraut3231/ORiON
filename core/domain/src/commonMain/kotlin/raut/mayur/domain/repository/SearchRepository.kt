package raut.mayur.domain.repository

import raut.mayur.domain.data.entities.SearchHistory
import raut.mayur.domain.data.model.searchResult.SearchSuggestions
import raut.mayur.domain.data.model.searchResult.albums.AlbumsResult
import raut.mayur.domain.data.model.searchResult.artists.ArtistsResult
import raut.mayur.domain.data.model.searchResult.playlists.PlaylistsResult
import raut.mayur.domain.data.model.searchResult.songs.SongsResult
import raut.mayur.domain.data.model.searchResult.videos.VideosResult
import raut.mayur.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getSearchHistory(): Flow<List<SearchHistory>>

    fun insertSearchHistory(searchHistory: SearchHistory): Flow<Long>

    suspend fun deleteSearchHistory()

    fun getSearchDataSong(query: String): Flow<Resource<ArrayList<SongsResult>>>

    fun getSearchDataVideo(query: String): Flow<Resource<ArrayList<VideosResult>>>

    fun getSearchDataPodcast(query: String): Flow<Resource<ArrayList<PlaylistsResult>>>

    fun getSearchDataFeaturedPlaylist(query: String): Flow<Resource<ArrayList<PlaylistsResult>>>

    fun getSearchDataArtist(query: String): Flow<Resource<ArrayList<ArtistsResult>>>

    fun getSearchDataAlbum(query: String): Flow<Resource<ArrayList<AlbumsResult>>>

    fun getSearchDataPlaylist(query: String): Flow<Resource<ArrayList<PlaylistsResult>>>

    fun getSuggestQuery(query: String): Flow<Resource<SearchSuggestions>>
}