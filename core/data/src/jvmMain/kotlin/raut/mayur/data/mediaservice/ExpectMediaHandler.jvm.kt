package raut.mayur.data.mediaservice

import raut.mayur.domain.repository.AnalyticsRepository
import raut.mayur.domain.repository.DiscordRepository

actual fun createMediaServiceHandler(
    dataStoreManager: raut.mayur.domain.manager.DataStoreManager,
    songRepository: raut.mayur.domain.repository.SongRepository,
    streamRepository: raut.mayur.domain.repository.StreamRepository,
    localPlaylistRepository: raut.mayur.domain.repository.LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    discordRepository: DiscordRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): raut.mayur.domain.mediaservice.handler.MediaPlayerHandler =
    JvmMediaPlayerHandlerImpl(
        dataStoreManager = dataStoreManager,
        songRepository = songRepository,
        streamRepository = streamRepository,
        localPlaylistRepository = localPlaylistRepository,
        analyticsRepository = analyticsRepository,
        discordRepository = discordRepository,
        coroutineScope = coroutineScope,
    )