package com.example.mytodoapp

import androidx.compose.ui.unit.LayoutDirection
import com.example.mytodoapp.util.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun testSupportedLanguagesCount() {
        assertEquals(5, LocaleHelper.SUPPORTED_LANGUAGES.size)
    }

    @Test
    fun testRtlDetection() {
        assertTrue("Urdu should be RTL", LocaleHelper.isRtl("ur"))
        assertTrue("Arabic should be RTL", LocaleHelper.isRtl("ar"))
        assertFalse("English should not be RTL", LocaleHelper.isRtl("en"))
        assertFalse("French should not be RTL", LocaleHelper.isRtl("fr"))
        assertFalse("Chinese should not be RTL", LocaleHelper.isRtl("zh"))
    }

    @Test
    fun testLayoutDirectionMapping() {
        assertEquals(LayoutDirection.Rtl, LocaleHelper.getLayoutDirection("ur"))
        assertEquals(LayoutDirection.Rtl, LocaleHelper.getLayoutDirection("ar"))
        assertEquals(LayoutDirection.Ltr, LocaleHelper.getLayoutDirection("en"))
        assertEquals(LayoutDirection.Ltr, LocaleHelper.getLayoutDirection("fr"))
        assertEquals(LayoutDirection.Ltr, LocaleHelper.getLayoutDirection("zh"))
    }

    @Test
    fun testLanguageDisplayNames() {
        assertEquals("English", LocaleHelper.getDisplayName("en"))
        assertEquals("اردو", LocaleHelper.getDisplayName("ur"))
        assertEquals("Français", LocaleHelper.getDisplayName("fr"))
        assertEquals("中文", LocaleHelper.getDisplayName("zh"))
        assertEquals("العربية", LocaleHelper.getDisplayName("ar"))
    }
}
