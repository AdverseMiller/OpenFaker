package com.android1500.androidfaker.data.loader

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import java.lang.reflect.Member

/** Reconstructed adapter; its original runtime name was OooO00o. */
class XposedHookBridge(private val module: XposedInterface) : IHookBridge {
    override fun hook(member: Member): IHookBridge.Builder {
        val executable = member as Executable
        return object : IHookBridge.Builder {
            override fun intercept(interceptor: IHookBridge.Interceptor) {
                module.hook(executable).intercept { chain ->
                    interceptor.intercept(object : IHookBridge.Chain {
                        override val args: List<Any?> get() = chain.args
                        override val thisObject: Any? get() = chain.thisObject
                        override fun proceed(): Any? = chain.proceed()
                    })
                }
            }
        }
    }

    override fun log(priority: Int, tag: String, message: String, throwable: Throwable?) {
        if (throwable == null) module.log(priority, tag, message)
        else module.log(priority, tag, message, throwable)
    }
}
