package ru.variiix.afisha.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import ru.variiix.afisha.models.User

object UserSession {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "user_data"

    private lateinit var prefs: SharedPreferences
    private var cachedToken: String? = null
    private var cachedUser: User? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(UserSession.PREFS_NAME, Context.MODE_PRIVATE)
        cachedToken = prefs.getString(KEY_TOKEN, null)

        val json = prefs.getString(KEY_USER, null)
        if (json != null) {
            cachedUser = gson.fromJson(json, User::class.java)
        }
    }

    fun isAuthorized(): Boolean = cachedToken != null

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
        this.cachedToken = token
    }

    fun saveUser(user: User) {
        prefs.edit { putString(KEY_USER, gson.toJson(user)) }
        cachedUser = user
    }

    fun clear() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER)
        }
        cachedToken = null
        cachedUser = null
    }

    fun getToken(): String? = cachedToken
    fun getUser(): User? = cachedUser
}
