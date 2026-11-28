package raut.mayur.data.di.loader

import raut.mayur.media3.di.loadMediaService
import raut.mayur.data.loginsync.LoginSyncSenderRepositoryImpl
import raut.mayur.data.loginsync.LoginSyncStore
import raut.mayur.domain.repository.LoginSyncSenderRepository
import org.koin.core.context.loadKoinModules
import org.simpmusic.loginsync.LoginSyncClient
import org.koin.dsl.module

actual fun loadMediaService() {
    loadMediaService()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncSenderRepository> { LoginSyncSenderRepositoryImpl(get(), LoginSyncClient()) }
        },
    )
}
