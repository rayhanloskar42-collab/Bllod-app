package com.example.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("blood_donor_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUserSession(userId: Int) {
        prefs.edit()
            .putInt(KEY_LOGGED_IN_USER_ID, userId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getLoggedInUserId(): Int? {
        val id = prefs.getInt(KEY_LOGGED_IN_USER_ID, -1)
        return if (id != -1) id else null
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && prefs.getInt(KEY_LOGGED_IN_USER_ID, -1) != -1
    }

    fun clearSession() {
        prefs.edit()
            .clear()
            .apply()
    }
}
