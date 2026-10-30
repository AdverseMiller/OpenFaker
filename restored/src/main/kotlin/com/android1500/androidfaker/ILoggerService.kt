package com.android1500.androidfaker

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import kotlinx.parcelize.parcelableCreator

/** Recovered one-way Binder wire contract (transaction 1). */
interface ILoggerService : IInterface {
    fun addLogs(logs: List<LogEntity>?)

    abstract class Stub : Binder(), ILoggerService {
        init { attachInterface(this, DESCRIPTOR) }
        override fun asBinder(): IBinder = this
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in 1..16777215) data.enforceInterface(DESCRIPTOR)
            if (code == INTERFACE_TRANSACTION) { reply?.writeString(DESCRIPTOR); return true }
            if (code != 1) return super.onTransact(code, data, reply, flags)
            addLogs(data.createTypedArrayList(parcelableCreator<LogEntity>()))
            return true
        }
    }

    companion object {
        const val DESCRIPTOR = "com.android1500.androidfaker.ILoggerService"
        fun asInterface(binder: IBinder): ILoggerService =
            binder.queryLocalInterface(DESCRIPTOR) as? ILoggerService ?: object : ILoggerService {
                override fun asBinder() = binder
                override fun addLogs(logs: List<LogEntity>?) {
                    val data = Parcel.obtain()
                    try {
                        data.writeInterfaceToken(DESCRIPTOR)
                        data.writeTypedList(logs)
                        binder.transact(1, data, null, IBinder.FLAG_ONEWAY)
                    } finally { data.recycle() }
                }
            }
    }
}
