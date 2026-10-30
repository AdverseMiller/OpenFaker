package com.android1500.androidfaker.data.loader

import com.androidfaker.data.repository.util.HookConfig

interface IHookEntry {
    fun hook(bridge: IHookBridge, classLoader: ClassLoader, config: HookConfig)
}
