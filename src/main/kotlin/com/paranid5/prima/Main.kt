package com.paranid5.prima

import com.paranid5.prima.di.appModule
import com.paranid5.prima.presentation.App
import org.koin.core.context.startKoin

fun main() {
    startKoin { modules(appModule) }
    App()
}