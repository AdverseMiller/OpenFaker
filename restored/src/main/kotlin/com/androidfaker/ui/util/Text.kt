package com.androidfaker.ui.util

import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/** Resource-aware text. The parcel field order follows the recovered APK. */
interface Text : Parcelable {
    fun resolve(resources: Resources): CharSequence

    operator fun invoke(resources: Resources): String = resolve(resources).toString()

    @Composable
    operator fun invoke(): String = invoke(LocalResources.current)
}

fun String.asText(): Text = StringText(this)
fun resourceText(@StringRes id: Int): Text = ResText(id)
fun resourceText(@StringRes id: Int, vararg args: Any?): Text = ResArgsText(id, args.toList())
fun pluralText(@PluralsRes id: Int, quantity: Int, vararg args: Any?): Text =
    PluralsText(id, quantity, args.toList())
fun concatenateText(vararg args: Text): Text = TextConcatenation(ArrayList(args.toList()))

@Parcelize
internal data class StringText(val string: String) : Text {
    override fun resolve(resources: Resources): CharSequence = string
    override fun toString(): String = "■"
}

@Parcelize
internal data class ResText(@param:StringRes val id: Int) : Text {
    override fun resolve(resources: Resources): CharSequence = resources.getText(id)
    override fun toString(): String = "■"
}

@Parcelize
internal data class ResArgsText(
    @param:StringRes val id: Int,
    val args: List<@RawValue Any?>,
) : Text {
    override fun resolve(resources: Resources): CharSequence =
        resources.getString(id, *args.resolve(resources))

    override fun toString(): String = "ResArgsText(id=$id, args=${args.description()})"
}

@Parcelize
internal data class PluralsText(
    @param:PluralsRes val id: Int,
    val quantity: Int,
    val args: List<@RawValue Any?>,
) : Text {
    override fun resolve(resources: Resources): CharSequence =
        resources.getQuantityString(id, quantity, *args.resolve(resources))

    override fun toString(): String =
        "PluralsText(id=$id, quantity=$quantity, args=${args.description()})"
}

@Parcelize
internal data class TextConcatenation(val args: ArrayList<Text>) : Text {
    override fun resolve(resources: Resources): CharSequence =
        args.joinToString(separator = "") { it.resolve(resources) }

    override fun toString(): String = "■"
}

private fun List<Any?>.resolve(resources: Resources): Array<Any?> =
    map { if (it is Text) it.resolve(resources) else it }.toTypedArray()

private fun List<Any?>.description(): String = joinToString(
    separator = ",", prefix = "(", postfix = ")",
)
