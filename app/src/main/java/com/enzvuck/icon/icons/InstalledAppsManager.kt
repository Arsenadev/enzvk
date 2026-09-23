package com.enzvuck.icon.icons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.enzvuck.icon.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Detects launchable applications installed on device using standard Android package APIs.
 * Reads strictly necessary metadata: label, packageName, launcherActivity, original icon.
 */
class InstalledAppsManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Queries launchable applications on device.
     */
    suspend fun getLaunchableApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        val appList = mutableListOf<AppInfo>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            // Exclude this app from the installed apps list so users don't accidentally try to skin enzvuck itself
            if (packageName == context.packageName) continue
            if (seenPackages.contains(packageName)) continue
            seenPackages.add(packageName)

            val label = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (_: Exception) {
                resolveInfo.activityInfo.name
            }

            val launcherActivity = resolveInfo.activityInfo.name

            appList.add(
                AppInfo(
                    packageName = packageName,
                    appName = label,
                    launcherActivity = launcherActivity
                )
            )
        }

        // Sort alphabetically by app name
        appList.sortedBy { it.appName.lowercase() }
    }

    /**
     * Dynamically detects the actual launcher activity for a given package name using PackageManager.
     * Never returns fake or hardcoded activities.
     */
    fun getLauncherActivityForPackage(packageName: String): String {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = packageName
            }
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }

            val detected = resolveInfos.firstOrNull()?.activityInfo?.name
            if (!detected.isNullOrBlank()) {
                return detected
            }

            // Fallback: check launch intent
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            val className = launchIntent?.component?.className
            if (!className.isNullOrBlank()) {
                return className
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return "${packageName}.MainActivity"
    }

    /**
     * Loads the original application icon drawable.
     */
    fun loadOriginalAppIconDrawable(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts the original application icon as a clean Bitmap.
     */
    suspend fun loadOriginalAppIconBitmap(packageName: String, targetSize: Int = 512): Bitmap? =
        withContext(Dispatchers.IO) {
            val drawable = loadOriginalAppIconDrawable(packageName) ?: return@withContext null
            drawableToBitmap(drawable, targetSize)
        }

    /**
     * Converts any Android Drawable (including AdaptiveIconDrawable) to a clean Bitmap.
     */
    fun drawableToBitmap(drawable: Drawable, targetSize: Int = 512): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width == targetSize && bmp.height == targetSize) {
                return bmp.copy(Bitmap.Config.ARGB_8888, true)
            }
        }

        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, targetSize, targetSize)
        drawable.draw(canvas)
        return bitmap
    }
}
