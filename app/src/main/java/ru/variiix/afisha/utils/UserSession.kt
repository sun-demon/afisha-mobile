package ru.variiix.afisha.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object UserSession {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_TOKEN = "auth_token"
    private lateinit var prefs: SharedPreferences

    private var cachedToken: String? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(UserSession.PREFS_NAME, Context.MODE_PRIVATE)
        cachedToken = prefs.getString(KEY_TOKEN, null)
    }

    fun isAuthorized(): Boolean {
        return cachedToken != null
    }

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
        this.cachedToken = token
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
        cachedToken = null
    }

    fun getToken(): String? {
        return cachedToken
    }
}
