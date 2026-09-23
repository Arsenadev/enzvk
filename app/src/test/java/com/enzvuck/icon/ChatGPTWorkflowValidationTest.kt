package com.enzvuck.icon

import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.domain.model.IconPackItem
import com.enzvuck.icon.export.AppFilterGenerator
import com.enzvuck.icon.export.ResourceNameNormalizer
import com.enzvuck.icon.export.ThemePackager
import com.enzvuck.icon.launcher.LauncherThemeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatGPTWorkflowValidationTest {

    @Test
    fun testChatGptWorkflowAndAppFilterXml() {
        val packageName = "com.openai.chatgpt"
        val appName = "ChatGPT"
        val launcherActivity = "com.openai.chatgpt.MainActivity"

        // 1. Normalize resource name
        val resourceName = ResourceNameNormalizer.normalize(packageName, appName)
        assertEquals("chatgpt", resourceName)

        // 2. Custom icon representation
        val customIcon = CustomIcon(
            id = 42L,
            packageName = packageName,
            appName = appName,
            iconPath = "/fake/storage/chatgpt.webp",
            launcherActivity = launcherActivity,
            resourceName = resourceName
        )
        assertEquals("chatgpt", customIcon.resourceName)
        assertEquals(launcherActivity, customIcon.launcherActivity)

        // 3. Icon Pack Item mapping
        val packItem = IconPackItem(
            id = 1L,
            packId = 100L,
            customIconId = customIcon.id,
            packageName = customIcon.packageName,
            appName = customIcon.appName,
            launcherActivity = customIcon.launcherActivity,
            drawableName = customIcon.resourceName
        )

        // 4. Generate appfilter.xml
        val appfilterXml = AppFilterGenerator.generate(listOf(packItem))
        assertTrue(appfilterXml.contains("<resources>"))
        assertTrue(appfilterXml.contains("ComponentInfo{com.openai.chatgpt/com.openai.chatgpt.MainActivity}"))
        assertTrue(appfilterXml.contains("drawable=\"chatgpt\""))
        assertTrue(appfilterXml.contains("</resources>"))

        // 5. Validate pack
        val pack = IconPack(
            id = 100L,
            name = "Test Pack",
            description = "Pack description",
            author = "enzvuck",
            version = "1.0.0",
            iconCount = 1
        )
        val validation = ThemePackager.validatePack(listOf(packItem to customIcon), checkFileExists = false)
        assertTrue("Pack should be valid", validation is ThemePackager.PackValidationResult.Valid)
    }

    @Test
    fun testLauncherSupportResolution() {
        // Pixel Launcher (standard launcher with no public icon pack apply API)
        val pixelSupport = LauncherThemeHelper.isLauncherApplySupported("com.google.android.apps.nexuslauncher")
        assertFalse("Pixel Launcher should not claim direct apply support", pixelSupport)

        // Samsung One UI Home
        val oneUiSupport = LauncherThemeHelper.isLauncherApplySupported("com.sec.android.app.launcher")
        assertFalse("One UI Home should not claim direct apply support", oneUiSupport)

        // Nova Launcher
        val novaSupport = LauncherThemeHelper.isLauncherApplySupported("com.teslacoilsw.launcher")
        assertTrue("Nova Launcher should support direct apply intent", novaSupport)

        // Lawnchair
        val lawnchairSupport = LauncherThemeHelper.isLauncherApplySupported("ch.deletescape.lawnchair.plah")
        assertTrue("Lawnchair should support direct apply intent", lawnchairSupport)
    }
}
