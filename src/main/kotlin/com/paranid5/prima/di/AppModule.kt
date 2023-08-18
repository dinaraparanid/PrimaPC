package com.paranid5.prima.di

import com.paranid5.prima.domain.StorageHandler
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    includes(globalsModule)
}

private val globalsModule = module {
    singleOf(::StorageHandler)
}