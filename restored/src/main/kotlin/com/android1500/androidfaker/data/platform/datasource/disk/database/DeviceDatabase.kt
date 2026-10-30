package com.android1500.androidfaker.data.platform.datasource.disk.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import com.android1500.androidfaker.ui.platform.feature.hook.Model
import java.io.Closeable
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// Names/fields survive in oe3/vx3; original packages do not.
data class Manufacture(val manufacture: String)
data class OS(val osv: String)

/** Recovered read-only catalog, separate from the app's writable Room database. */
class DeviceDatabase internal constructor(openDatabase: () -> SQLiteDatabase) : Closeable {
    private val database by lazy(openDatabase)

    constructor(context: Context) : this({
        val target = context.getDatabasePath(ASSET_NAME)
        if (!target.exists()) {
            target.parentFile?.mkdirs()
            context.assets.open(ASSET_NAME).use { input -> target.outputStream().use { input.copyTo(it) } }
        }
        SQLiteDatabase.openDatabase(target.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    })

    fun getManufacturers(): List<Manufacture> = query(
        "SELECT manufacture FROM devices GROUP BY manufacture ORDER BY manufacture COLLATE NOCASE",
    ) { Manufacture(it.text("manufacture")) }

    fun getModels(manufacturer: String): List<Model> = query(
        "SELECT * FROM devices WHERE manufacture = ?", arrayOf(manufacturer),
    ) { row ->
        fun decoded(column: String) = decodeValue(row.text(column))
        Model(decoded("buildId"), decoded("display"), decoded("product"), decoded("board"),
            decoded("device"), row.text("manufacture"), decoded("brand"), decoded("model"),
            decoded("bootloader"), decoded("radio"), decoded("hardware"), decoded("tag"),
            decoded("type"), decoded("fingerprint"), decoded("incremental"), decoded("osv"),
            decoded("sdk"), decoded("host"), decoded("user"))
    }.distinctBy { it.model }.sortedBy { it.model }

    fun getOsVersions(model: String): List<OS> = query("SELECT model, osv FROM devices") {
        decodeValue(it.text("model")) to decodeValue(it.text("osv"))
    }.filter { it.first == model }.map { OS(it.second) }.distinctBy { it.osv }.sortedBy { it.osv }

    fun getCountries(): List<String> = query(
        "SELECT DISTINCT Country FROM mcc_mnc ORDER BY Country COLLATE NOCASE",
    ) { it.text("Country") }

    fun getOperators(country: String): List<String> = query(
        "SELECT DISTINCT Network FROM mcc_mnc WHERE Country = ? ORDER BY Network COLLATE NOCASE", arrayOf(country),
    ) { it.text("Network") }

    // Deliberately scoped by operator only, as in the recovered query (not additionally by country).
    fun getMccMncCodes(operator: String): List<String> = query(
        "SELECT DISTINCT MCC, MNC FROM mcc_mnc WHERE Network = ?", arrayOf(operator),
    ) { it.text("MCC") + it.text("MNC") }

    fun getCountryIso(country: String): String? = database.rawQuery(
        "SELECT DISTINCT ISO FROM mcc_mnc WHERE Country = ? LIMIT 1", arrayOf(country),
    ).use { if (it.moveToFirst()) it.text("ISO") else null }

    private fun <T> query(sql: String, args: Array<String>? = null, read: (Cursor) -> T): List<T> =
        database.rawQuery(sql.trimIndent(), args).use { cursor ->
            buildList { while (cursor.moveToNext()) add(read(cursor)) }
        }

    override fun close() { if (database.isOpen) database.close() }

    companion object {
        const val ASSET_NAME = "sqliteManger.db"

        private fun Cursor.text(column: String): String = getString(getColumnIndexOrThrow(column)) ?: ""

        /** Best-effort legacy field decoding; malformed/plain values pass through unchanged. */
        internal fun decodeValue(value: String): String {
            if (value.isEmpty()) return value
            return runCatching {
                val bytes = Base64.decode(value, Base64.DEFAULT)
                if (bytes.size < 32) return value
                var key = "mypassword".toByteArray(Charsets.UTF_8) + bytes.copyOfRange(16, 32)
                repeat(20) { key = MessageDigest.getInstance("SHA-256").digest(key) }
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(bytes.copyOfRange(0, 16)))
                val plain = cipher.doFinal(bytes.copyOfRange(32, bytes.size)).toString(Charsets.UTF_8)
                // The original removes a second trailing count after the cipher's padding removal.
                if (plain.isNotEmpty() && plain.last().code in 1..plain.length) plain.dropLast(plain.last().code)
                else plain
            }.getOrDefault(value)
        }
    }
}
