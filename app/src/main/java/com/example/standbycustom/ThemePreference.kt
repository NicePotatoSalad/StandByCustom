package com.example.standbycustom

/**
 * User-selectable theme: follow device (Auto), force Light, or force Dark.
 */
enum class ThemeOption(val value: Int) {
    Auto(0),
    Light(1),
    Dark(2);

    companion object {
        fun fromValue(value: Int): ThemeOption = entries.find { it.value == value } ?: Auto
    }
}
