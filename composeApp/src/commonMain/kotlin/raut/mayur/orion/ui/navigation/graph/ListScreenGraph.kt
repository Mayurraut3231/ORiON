package raut.mayur.orion.ui.navigation.graph

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import raut.mayur.orion.ui.navigation.destination.list.AlbumDestination
import raut.mayur.orion.ui.navigation.destination.list.ArtistDestination
import raut.mayur.orion.ui.navigation.destination.list.BrowseDestination
import raut.mayur.orion.ui.navigation.destination.list.LocalPlaylistDestination
import raut.mayur.orion.ui.navigation.destination.list.MoreAlbumsDestination
import raut.mayur.orion.ui.navigation.destination.list.PlaylistDestination
import raut.mayur.orion.ui.navigation.destination.list.PodcastDestination
import raut.mayur.orion.ui.screen.library.LocalPlaylistScreen
import raut.mayur.orion.ui.screen.other.AlbumScreen
import raut.mayur.orion.ui.screen.other.ArtistScreen
import raut.mayur.orion.ui.screen.other.BrowseScreen
import raut.mayur.orion.ui.screen.other.MoreAlbumsScreen
import raut.mayur.orion.ui.screen.other.PlaylistScreen
import raut.mayur.orion.ui.screen.other.PodcastScreen
import raut.mayur.orion.ui.theme.ForceDarkContent

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun NavGraphBuilder.listScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<AlbumDestination> { entry ->
        val data = entry.toRoute<AlbumDestination>()
        ForceDarkContent {
            AlbumScreen(
                browseId = data.browseId,
                navController = navController,
            )
        }
    }
    composable<ArtistDestination> { entry ->
        val data = entry.toRoute<ArtistDestination>()
        ForceDarkContent {
            ArtistScreen(
                channelId = data.channelId,
                navController = navController,
            )
        }
    }
    composable<LocalPlaylistDestination> { entry ->
        val data = entry.toRoute<LocalPlaylistDestination>()
        ForceDarkContent {
            LocalPlaylistScreen(
                id = data.id,
                navController = navController,
            )
        }
    }
    composable<MoreAlbumsDestination> { entry ->
        val data = entry.toRoute<MoreAlbumsDestination>()
        MoreAlbumsScreen(
            innerPadding = innerPadding,
            navController = navController,
            type = data.type,
            id = data.id,
        )
    }
    composable<BrowseDestination> { entry ->
        val data = entry.toRoute<BrowseDestination>()
        BrowseScreen(
            innerPadding = innerPadding,
            navController = navController,
            browseId = data.browseId,
            params = data.params,
            title = data.title,
        )
    }
    composable<PlaylistDestination> { entry ->
        val data = entry.toRoute<PlaylistDestination>()
        ForceDarkContent {
            PlaylistScreen(
                playlistId = data.playlistId,
                isYourYouTubePlaylist = data.isYourYouTubePlaylist,
                navController = navController,
            )
        }
    }
    composable<PodcastDestination> { entry ->
        val data = entry.toRoute<PodcastDestination>()
        ForceDarkContent {
            PodcastScreen(
                podcastId = data.podcastId,
                navController = navController,
            )
        }
    }
}