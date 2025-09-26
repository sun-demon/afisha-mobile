package ru.variiix.afisha.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object LocalFavorites {
    private const val PREFS_NAME = "local_favorites"
    private const val KEY_IDS = "favorite_ids"
    private lateinit var prefs: SharedPreferences
    private val ids = mutableSetOf<String>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ids.clear()
        ids.addAll(prefs.getStringSet(KEY_IDS, emptySet()) ?: emptySet())
    }

    fun contains(id: String): Boolean {
        return ids.contains(id)
    }

    fun add(id: String) {
        ids.add(id)
        save()
    }

    fun remove(id: String) {
        ids.remove(id)
        save()
    }

    private fun save() {
        prefs.edit { putStringSet(KEY_IDS, ids) }
    }

    fun getParam(): String? {
        return ids.joinToString(",").ifEmpty { "," }
    }

    fun isEmpty(): Boolean {
        return ids.isEmpty()
    }

    fun clear() {
        ids.clear()
        save()
    }
}
