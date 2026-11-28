package raut.mayur.data.di.loader

import com.simpmusic.media_jvm.di.loadDesktopPlayerModule
import raut.mayur.data.browserlogin.BrowserLoginRepositoryImpl
import raut.mayur.data.loginsync.LoginSyncHostRepositoryImpl
import raut.mayur.data.loginsync.LoginSyncStore
import raut.mayur.domain.repository.LoginSyncHostRepository
import raut.mayur.domain.repository.BrowserLoginRepository
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

actual fun loadMediaService() {
    loadDesktopPlayerModule()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncHostRepository> { LoginSyncHostRepositoryImpl(get()) }
            single<BrowserLoginRepository> { BrowserLoginRepositoryImpl() }
        },
    )
}
