package com.example.mytodoapp.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

object LocaleHelper {
    const val LANG_ENGLISH = "en"
    const val LANG_URDU = "ur"
    const val LANG_FRENCH = "fr"
    const val LANG_CHINESE = "zh"
    const val LANG_ARABIC = "ar"

    val SUPPORTED_LANGUAGES = listOf(
        LanguageOption(LANG_ENGLISH, "English", "English", false),
        LanguageOption(LANG_URDU, "اردو", "Urdu", true),
        LanguageOption(LANG_FRENCH, "Français", "French", false),
        LanguageOption(LANG_CHINESE, "中文", "Chinese", false),
        LanguageOption(LANG_ARABIC, "العربية", "Arabic", true)
    )

    fun isRtl(languageCode: String): Boolean {
        return languageCode == LANG_URDU || languageCode == LANG_ARABIC
    }

    fun getLayoutDirection(languageCode: String): LayoutDirection {
        return if (isRtl(languageCode)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun getDisplayName(languageCode: String): String {
        return SUPPORTED_LANGUAGES.find { it.code == languageCode }?.nativeName ?: "English"
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val configurationContext = context.createConfigurationContext(config)
        return LocalizedContext(configurationContext, context as? androidx.activity.ComponentActivity)
    }
}

class LocalizedContext(
    base: Context,
    private val activity: androidx.activity.ComponentActivity?
) : android.content.ContextWrapper(base), androidx.activity.result.ActivityResultRegistryOwner {
    override val activityResultRegistry: androidx.activity.result.ActivityResultRegistry
        get() = activity?.activityResultRegistry
            ?: (baseContext as? androidx.activity.result.ActivityResultRegistryOwner)?.activityResultRegistry
            ?: throw IllegalStateException("No ActivityResultRegistry found in LocalizedContext")
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val isRtl: Boolean
)
