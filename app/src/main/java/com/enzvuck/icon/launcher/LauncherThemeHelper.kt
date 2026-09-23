package com.enzvuck.icon.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Handles launcher detection and determines whether the current launcher supports
 * direct third-party icon-pack application via public Intent APIs.
 *
 * CRITICAL RULE:
 * Never claim an icon is "Applied" unless the launcher actually supports and confirms it.
 * Never use shortcuts or fake successful application.
 */
class LauncherThemeHelper(private val context: Context) {

    data class LauncherInfo(
        val packageName: String,
        val launcherName: String,
        val supportsDirectApply: Boolean,
        val applyIntentAction: String? = null
    )

    /**
     * Detects the current default home launcher.
     */
    fun getCurrentLauncher(): LauncherInfo {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = context.packageManager.resolveActivity(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        val pkg = resolveInfo?.activityInfo?.packageName ?: ""
        val name = try {
            if (pkg.isNotBlank()) {
                resolveInfo?.loadLabel(context.packageManager)?.toString() ?: pkg
            } else {
                "Default Launcher"
            }
        } catch (_: Exception) {
            pkg.ifBlank { "Default Launcher" }
        }

        val (supports, action) = checkSupport(pkg)
        return LauncherInfo(
            packageName = pkg,
            launcherName = name,
            supportsDirectApply = supports,
            applyIntentAction = action
        )
    }

    companion object {
        fun checkSupport(packageName: String): Pair<Boolean, String?> {
            val lower = packageName.lowercase()
            return when {
                lower.contains("novalauncher") || lower.contains("teslacoilsw") ->
                    Pair(true, "com.novalauncher.THEME")
                lower.contains("adw") ->
                    Pair(true, "org.adw.launcher.SET_THEME")
                lower.contains("actionlauncher") ->
                    Pair(true, "com.actionlauncher.THEME")
                lower.contains("smartlauncher") ->
                    Pair(true, "ginlemon.smartlauncher.setGSLTHEME")
                lower.contains("lawnchair") ->
                    Pair(true, "ch.deletescape.lawnchair.APPLY_ICONS")
                else ->
                    // Stock launchers (Pixel Launcher, Samsung One UI Home, MIUI, standard AOSP Launcher3, etc.)
                    // DO NOT support direct icon-pack application via public intent API.
                    Pair(false, null)
            }
        }

        fun isLauncherApplySupported(packageName: String): Boolean {
            return checkSupport(packageName).first
        }
    }

    /**
     * Builds an intent to apply or open theme settings in the supported launcher, if possible.
     */
    fun buildApplyIntent(packPackageName: String): Intent? {
        val launcher = getCurrentLauncher()
        if (!launcher.supportsDirectApply || launcher.applyIntentAction == null) {
            return null
        }

        return try {
            val intent = Intent(launcher.applyIntentAction).apply {
                when (launcher.applyIntentAction) {
                    "com.novalauncher.THEME" -> {
                        type = "novatheme"
                        putExtra("theme", packPackageName)
                    }
                    "org.adw.launcher.SET_THEME" -> {
                        putExtra("org.adw.launcher.theme.NAME", packPackageName)
                    }
                    "com.actionlauncher.THEME" -> {
                        putExtra("apply_icon_pack", packPackageName)
                    }
                    "ginlemon.smartlauncher.setGSLTHEME" -> {
                        putExtra("package", packPackageName)
                    }
                    "ch.deletescape.lawnchair.APPLY_ICONS" -> {
                        putExtra("package", packPackageName)
                    }
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent
        } catch (_: Exception) {
            null
        }
    }
}
