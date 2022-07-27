package com.dinaraparanid.prima.ui.utils.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.replaceCurrent
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value

abstract class AbstractScreen<S : ScreenElement.Screen, C : Config>(componentContext: ComponentContext) :
    ScreenElement,
    ComponentContext by componentContext {
    override val routerState: Value<RouterState<*, S>>
        get() = router.state

    protected abstract var _currentConfigState: MutableState<C>

    val currentConfigState: State<C>
        get() = _currentConfigState

    protected val router by lazy {
        router(
            initialConfiguration = _currentConfigState.value as Config,
            handleBackButton = true,
            childFactory = { config, _ ->
                @Suppress("UNCHECKED_CAST")
                getChild(config as C)
            }
        )
    }

    abstract val initialConfig: C

    protected abstract fun getChild(config: C): S

    protected fun changeConfig(config: C) {
        _currentConfigState.value = config
        router.replaceCurrent(_currentConfigState.value)
    }

    @Composable
    fun start() {
        _currentConfigState = remember { mutableStateOf(initialConfig) }
    }
}