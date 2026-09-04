kotlin
package com.example.piniterator

import android.content.Context
import android.content.Context.MODE_PRIVATE

class PinStorage(context: Context) {
    private val prefs = context.getSharedPreferences("pin_prefs", MODE_PRIVATE)
    private val STEP = 3751
    private val MAX = 10000

    var current: Int
        get() = prefs.getInt("current", 0)
        set(value) = prefs.edit().putInt("current", value).apply()

    var count: Int
        get() = prefs.getInt("count", 0)
        set(value) = prefs.edit().putInt("count", value).apply()

    fun getNext(): Int? {
        if (count >= MAX) return null
        current = (current + STEP) % MAX
        count++
        return current
    }

    fun reset() {
        current = 0
        count = 0
        prefs.edit().clear().apply()
    }
}
