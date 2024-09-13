package com.sitegist.ticketvalidation

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs

open class Utils {

    companion object {

        class AppPreferences(
            var user: String,
            var email: String,
            var password: String,
            var token: String
        )

        fun getAppPreferences(context: Context): AppPreferences {
            Prefs.Builder()
                .setContext(context)
                .setMode(AppCompatActivity.MODE_PRIVATE)
                .setPrefsName(context.packageName)
                .setUseDefaultSharedPreference(true)
                .build()

            val prefs = AppPreferences(
                "",
                "",
                "",
                ""
            )
            prefs.user = Prefs.getString("preference_user", "Unknown")
            prefs.email = Prefs.getString("preference_mail", "")
            prefs.password = Prefs.getString("preference_password", "")
            prefs.token = Prefs.getString("preference_token", "")

            return prefs
        }

        fun isValidEmail(email: String): Boolean {
            return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

}