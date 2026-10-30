package com.android1500.androidfaker.data.loader

import java.lang.reflect.Member

interface IHookBridge {
    fun hook(member: Member): Builder
    fun log(priority: Int, tag: String, message: String, throwable: Throwable?)

    interface Builder { fun intercept(interceptor: Interceptor) }
    interface Chain {
        val thisObject: Any?
        val args: List<Any?>
        fun proceed(): Any?
    }
    fun interface Interceptor { fun intercept(chain: Chain): Any? }
}
