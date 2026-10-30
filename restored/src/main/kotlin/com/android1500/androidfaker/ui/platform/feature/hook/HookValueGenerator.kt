package com.android1500.androidfaker.ui.platform.feature.hook

import java.security.SecureRandom
import kotlin.random.Random

/** Original HookViewModelKt generators, including their different random-number sources. */
internal class HookValueGenerator(
    private val random: Random = Random.Default,
    private val secure: SecureRandom = SecureRandom(),
) {
    fun ipv4(): String = "${random.nextInt(1, 224)}.${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(1, 255)}"
    fun gsfId(): String = buildString { repeat(16) { append(HEX[secure.nextInt(16)]) } }
    fun phoneNumber(): String = String.format("+1%03d%03d%04d", secure.nextInt(800) + 200,
        secure.nextInt(800) + 200, secure.nextInt(10000))

    fun imei(): String {
        val body = "35847631" + buildString { repeat(6) { append(secure.nextInt(10)) } }
        var sum = 0
        var doubled = true
        for (digit in body.reversed()) {
            var value = digit.digitToInt()
            if (doubled) { value *= 2; if (value > 9) value -= 9 }
            sum += value
            doubled = !doubled
        }
        return body + ((10 - sum % 10) % 10)
    }

    fun decimal(length: Int): String = buildString { for (i in 1..length) append(('0'..'9').random(random)) }
    fun hex(length: Int): String = buildString { for (i in 1..length) append(HEX.random(random)) }
    fun mac(): String = (1..6).joinToString(":") { String.format("%02X", random.nextInt(256)) }
    fun hardwareSerial(): String = buildString { repeat(10) { append(SERIAL.random(random)) } }
    // This is UUID-shaped, not an RFC version/variant-constrained UUID generator.
    fun uuidShape(): String = listOf(8, 4, 4, 4, 12).joinToString("-") { hex(it) }

    companion object {
        private const val HEX = "0123456789abcdef"
        private const val SERIAL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }
}
