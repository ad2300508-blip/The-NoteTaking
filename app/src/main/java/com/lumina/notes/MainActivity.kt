package com.lumina.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lumina.notes.data.settings.AppSettings
import com.lumina.notes.ui.LuminaApp
import com.lumina.notes.ui.theme.LuminaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as LuminaApplication).container
        val quickNote = isQuickNoteIntent(intent)

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsState(initial = AppSettings())
            val dark = settings.darkTheme ?: isSystemInDarkTheme()

            LuminaTheme(darkTheme = dark, dynamicColor = settings.dynamicColor) {
                LuminaApp(settings = settings, quickNote = quickNote)
            }
        }
    }

    /** True when launched from the S Pen Air Command "Create note" action. */
    private fun isQuickNoteIntent(intent: Intent?): Boolean =
        intent?.action == "android.intent.action.CREATE_NOTE"
}
