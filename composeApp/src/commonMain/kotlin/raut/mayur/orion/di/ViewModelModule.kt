package raut.mayur.orion.di

import raut.mayur.orion.viewModel.AlbumViewModel
import raut.mayur.orion.utils.VersionManager
import raut.mayur.orion.viewModel.AnalyticsViewModel
import raut.mayur.orion.viewModel.ListenTogetherSettingsViewModel
import raut.mayur.orion.viewModel.ListenTogetherViewModel
import raut.mayur.orion.viewModel.ArtistViewModel
import raut.mayur.orion.viewModel.BrowseViewModel
import raut.mayur.orion.viewModel.BrowserLoginViewModel
import raut.mayur.orion.viewModel.CommentsViewModel
import raut.mayur.orion.viewModel.SoundCloudPlayerViewModel
import raut.mayur.orion.viewModel.HomeViewModel
import raut.mayur.orion.viewModel.ImportViewModel
import raut.mayur.orion.viewModel.LibraryDynamicPlaylistViewModel
import raut.mayur.orion.viewModel.LibrarySidebarViewModel
import raut.mayur.orion.viewModel.LibraryViewModel
import raut.mayur.orion.viewModel.LocalPlaylistViewModel
import raut.mayur.orion.viewModel.LogInViewModel
import raut.mayur.orion.viewModel.LoginSyncHostViewModel
import raut.mayur.orion.viewModel.LoginSyncSenderViewModel
import raut.mayur.orion.viewModel.MoodViewModel
import raut.mayur.orion.viewModel.MoreAlbumsViewModel
import raut.mayur.orion.viewModel.NotificationViewModel
import raut.mayur.orion.viewModel.NowPlayingBottomSheetViewModel
import raut.mayur.orion.viewModel.PlaylistViewModel
import raut.mayur.orion.viewModel.PodcastViewModel
import raut.mayur.orion.viewModel.SearchViewModel
import raut.mayur.orion.viewModel.AutoEqViewModel
import raut.mayur.orion.viewModel.SettingsViewModel
import raut.mayur.orion.viewModel.SharedViewModel
import raut.mayur.orion.viewModel.SongSelectionViewModel
import raut.mayur.orion.viewModel.TasteViewModel
import raut.mayur.orion.viewModel.WrappedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            SongSelectionViewModel(
                get(),
                get(),
            )
        }
        // Only one of these resolves on a given platform: Desktop binds the host repository, Android the
        // sender. Koin resolves dependencies when a ViewModel is created, not when it is declared.
        viewModel { LoginSyncHostViewModel(get()) }
        viewModel { LoginSyncSenderViewModel(get()) }
        viewModel { BrowserLoginViewModel(get()) }
        viewModel { CommentsViewModel(get(), get()) }
        viewModel { SoundCloudPlayerViewModel(get()) }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ImportViewModel(
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AutoEqViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            BrowseViewModel(
                get(),
            )
        }
        viewModel {
            LibrarySidebarViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            WrappedViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            TasteViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ListenTogetherSettingsViewModel(get())
        }
        viewModel {
            ListenTogetherViewModel(
                repository = get(),
                dataStore = get(),
                bridge = get(),
            )
        }

    }
