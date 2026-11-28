package raut.mayur.data.di

import raut.mayur.common.AppIdentity
import raut.mayur.common.Config.SERVICE_SCOPE
import raut.mayur.data.io.fileDir
import raut.mayur.data.repository.AccountRepositoryImpl
import raut.mayur.data.repository.AlbumRepositoryImpl
import raut.mayur.data.repository.AnalyticsRepositoryImpl
import raut.mayur.data.repository.ArtistRepositoryImpl
import raut.mayur.data.repository.AutoEqRepositoryImpl
import raut.mayur.data.lyrics.LyricsRomanizerRepositoryImpl
import raut.mayur.data.repository.CommonRepositoryImpl
import raut.mayur.data.repository.DiscordRepositoryImpl
import raut.mayur.data.repository.HomeRepositoryImpl
import raut.mayur.data.repository.ImportRepositoryImpl
import raut.mayur.data.repository.LocalPlaylistRepositoryImpl
import raut.mayur.data.repository.LyricsCanvasRepositoryImpl
import raut.mayur.data.repository.PlaylistRepositoryImpl
import raut.mayur.data.repository.PodcastRepositoryImpl
import raut.mayur.data.repository.SearchRepositoryImpl
import raut.mayur.data.repository.SongRepositoryImpl
import raut.mayur.data.repository.StreamRepositoryImpl
import raut.mayur.data.repository.TasteRepositoryImpl
import raut.mayur.data.repository.UpdateRepositoryImpl
import raut.mayur.domain.repository.AccountRepository
import raut.mayur.domain.repository.AlbumRepository
import raut.mayur.domain.repository.AnalyticsRepository
import raut.mayur.domain.repository.ArtistRepository
import raut.mayur.domain.repository.AutoEqRepository
import raut.mayur.domain.repository.LyricsRomanizerRepository
import raut.mayur.domain.repository.CommonRepository
import raut.mayur.domain.repository.DiscordRepository
import raut.mayur.domain.repository.HomeRepository
import raut.mayur.domain.repository.ImportRepository
import raut.mayur.domain.repository.LocalPlaylistRepository
import raut.mayur.domain.repository.LyricsCanvasRepository
import raut.mayur.domain.repository.PlaylistRepository
import raut.mayur.domain.repository.PodcastRepository
import raut.mayur.domain.repository.SearchRepository
import raut.mayur.domain.repository.SongRepository
import raut.mayur.domain.repository.StreamRepository
import raut.mayur.domain.repository.TasteRepository
import raut.mayur.domain.repository.UpdateRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule =
    module {
        single<AccountRepository>(createdAtStart = true) {
            AccountRepositoryImpl(get(), get())
        }

        single<AlbumRepository>(createdAtStart = true) {
            AlbumRepositoryImpl(get(), get())
        }

        single<ArtistRepository>(createdAtStart = true) {
            ArtistRepositoryImpl(get(), get(), get())
        }

        single<CommonRepository>(createdAtStart = true) {
            CommonRepositoryImpl(get(named(SERVICE_SCOPE)), get(), get(), get(), get(), get()).apply {
                this.init("${fileDir()}/ytdlp-cookie.txt", get(), get<AppIdentity>().isDevBuild)
            }
        }

        // At start: a Full build drops the token kizzy's login left behind as soon as it launches.
        single<DiscordRepository>(createdAtStart = true) {
            DiscordRepositoryImpl(get())
        }

        // Lazy for the same reason its client is: the picker is the only thing that wants it.
        single<AutoEqRepository> {
            AutoEqRepositoryImpl(get(), get())
        }

        // Lazy: constructing it costs a few File.length() calls (plus, while the pack is missing,
        // one background read of the stored selection), but the kuromoji dictionary behind it is
        // loaded on first Japanese line, or right after a download installs it, and never at
        // startup — so this must NOT be createdAtStart, or every launch pays for a feature most
        // listeners leave off. The path is where Android keeps the downloaded ipadic pack (the
        // APK no longer bundles it); Desktop and iOS ignore it.
        single<LyricsRomanizerRepository> {
            LyricsRomanizerRepositoryImpl("${fileDir()}/kuromoji-ipadic", get())
        }

        single<HomeRepository>(createdAtStart = true) {
            HomeRepositoryImpl(get(), get())
        }

        single<ImportRepository>(createdAtStart = true) {
            ImportRepositoryImpl(get())
        }

        single<LocalPlaylistRepository>(createdAtStart = true) {
            LocalPlaylistRepositoryImpl(get(), get())
        }

        single<LyricsCanvasRepository>(createdAtStart = true) {
            LyricsCanvasRepositoryImpl(get(), get(), get(), get(), get())
        }

        single<PlaylistRepository>(createdAtStart = true) {
            PlaylistRepositoryImpl(get(), get(), get())
        }

        single<PodcastRepository>(createdAtStart = true) {
            PodcastRepositoryImpl(get(), get())
        }

        single<SearchRepository>(createdAtStart = true) {
            SearchRepositoryImpl(get(), get())
        }

        single<SongRepository>(createdAtStart = true) {
            SongRepositoryImpl(get(), get(), get())
        }

        single<StreamRepository>(createdAtStart = true) {
            StreamRepositoryImpl(get(), get())
        }

        single<UpdateRepository>(createdAtStart = true) {
            UpdateRepositoryImpl(get())
        }

        single<AnalyticsRepository>(createdAtStart = true) {
            AnalyticsRepositoryImpl(get())
        }

        // Lazy: only the taste card in Library asks for it.
        single<TasteRepository> {
            TasteRepositoryImpl(get(), get())
        }
    }