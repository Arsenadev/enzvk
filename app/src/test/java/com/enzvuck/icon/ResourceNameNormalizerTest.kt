package com.enzvuck.icon

import com.enzvuck.icon.domain.model.IconPackItem
import com.enzvuck.icon.export.AppFilterGenerator
import com.enzvuck.icon.export.ResourceNameNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceNameNormalizerTest {

    @Test
    fun testNormalizeStandardPackages() {
        val res1 = ResourceNameNormalizer.normalize("com.spotify.music", "Spotify")
        assertEquals("spotify", res1)

        val res2 = ResourceNameNormalizer.normalize("org.telegram.messenger", "Telegram")
        assertEquals("telegram", res2)
    }

    @Test
    fun testNormalizeWithSpecialCharactersAndNumbers() {
        val res1 = ResourceNameNormalizer.normalize("com.123app.cool", "123 Super & App!")
        assertFalse(res1.contains(" "))
        assertFalse(res1.contains("&"))
        assertFalse(res1.contains("!"))
        assertTrue(res1.matches(Regex("^[a-z_][a-z0-9_]*$")))

        val res2 = ResourceNameNormalizer.normalize("com.example.super_music", "Super & Music!!")
        assertEquals("super_music", res2)
        assertTrue(res2.matches(Regex("^[a-z_][a-z0-9_]*$")))
    }

    @Test
    fun testAppFilterXmlGeneration() {
        val entries = listOf(
            IconPackItem(
                id = 1L,
                packId = 1L,
                customIconId = 1L,
                packageName = "com.spotify.music",
                appName = "Spotify",
                launcherActivity = "com.spotify.music.MainActivity",
                drawableName = "ic_app_spotify"
            ),
            IconPackItem(
                id = 2L,
                packId = 1L,
                customIconId = 2L,
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                launcherActivity = "org.telegram.ui.LaunchActivity",
                drawableName = "ic_app_telegram"
            )
        )

        val xml = AppFilterGenerator.generate(entries)
        assertTrue(xml.contains("<resources>"))
        assertTrue(xml.contains("</resources>"))
        assertTrue(xml.contains("ComponentInfo{com.spotify.music/com.spotify.music.MainActivity}"))
        assertTrue(xml.contains("drawable=\"ic_app_spotify\""))
        assertTrue(xml.contains("ComponentInfo{org.telegram.messenger/org.telegram.ui.LaunchActivity}"))
        assertTrue(xml.contains("drawable=\"ic_app_telegram\""))
    }
}
