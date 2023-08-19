package com.paranid5.prima.presentation.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet

abstract class AbstractNavigator<C : Config>(
    componentContext: ComponentContext,
    initialConfig: C
) : ComponentContext by componentContext {
    protected val navigation = StackNavigation<C>()
    private val currentConfigMutableState = MutableStateFlow(initialConfig)

    val currentConfigState: StateFlow<C>
        get() = currentConfigMutableState

    abstract val stack: Value<ChildStack<C, Screen>>

    protected fun changeConfig(config: C) =
        navigation.push(currentConfigMutableState.updateAndGet { config })

    protected abstract fun getScreenFromConfig(config: C): Screen
}