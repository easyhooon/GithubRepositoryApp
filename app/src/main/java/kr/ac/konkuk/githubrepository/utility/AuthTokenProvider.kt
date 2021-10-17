package kr.ac.konkuk.githubrepository.utility

import android.content.Context
import androidx.preference.PreferenceManager

class AuthTokenProvider(private val context: Context) {

    companion object {

        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    //auth_token 이라고 하는 키값을 기반으로 value 가 저장됨
    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_AUTH_TOKEN, null)

}