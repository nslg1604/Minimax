package org.niaz.minimax.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MyPrefs @Inject constructor(
    @ApplicationContext private val context: Context) {
    val MY_PREFS_MAIN = "MY_PREFS_MAIN"
    val MAX_LEVEL = "MAX_LEVEL"

    /**
     * Write to Shared Preferences
     */
    fun write(name: String?, data: String?) {
        val editor = context.getSharedPreferences(
            MY_PREFS_MAIN, Context.MODE_PRIVATE)?.edit()
        editor?.putString(name, data)
        editor?.apply()
    }

    /**
     * Read from Shared Preferences
     */
    fun read(name: String): String? {
        val prefs = context.getSharedPreferences(
            MY_PREFS_MAIN, Context.MODE_PRIVATE)
        return prefs?.getString(name, null)
    }
}
