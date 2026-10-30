package com.android1500.androidfaker.ui.platform.feature.hook

/** wb2 retains the original data-class name/fields, but not its original package. */
data class HookData(
    val id: String,
    val title: String,
    val value: String? = null,
    val value2: String? = null,
    val checked: Boolean = false,
) {
    val displayValue: String get() = value ?: "Not set"
    val displayValue2: String get() = value2 ?: "Not set"
}
