package raut.mayur.data.mediaservice

import raut.mayur.domain.manager.DataStoreManager
import raut.mayur.domain.mediaservice.handler.MediaPlayerHandler
import raut.mayur.domain.repository.AnalyticsRepository
import raut.mayur.domain.repository.DiscordRepository
import raut.mayur.domain.repository.LocalPlaylistRepository
import raut.mayur.domain.repository.SongRepository
import raut.mayur.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

expect fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    discordRepository: DiscordRepository,
    coroutineScope: CoroutineScope,
): MediaPlayerHandler