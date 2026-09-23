package com.enzvuck.icon.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.enzvuck.icon.data.database.EnzvuckDatabase
import com.enzvuck.icon.data.entity.CustomIconEntity
import com.enzvuck.icon.data.entity.IconPackEntity
import com.enzvuck.icon.data.entity.IconPackItemEntity
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.EditorConfiguration
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.domain.model.IconPackItem
import com.enzvuck.icon.export.ResourceNameNormalizer
import com.enzvuck.icon.export.ThemePackager
import com.enzvuck.icon.icons.InstalledAppsManager
import com.enzvuck.icon.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class IconRepository(
    private val context: Context,
    private val database: EnzvuckDatabase,
    val storageManager: StorageManager,
    val appsManager: InstalledAppsManager,
    val themePackager: ThemePackager
) {
    private val customIconDao = database.customIconDao()
    private val iconPackDao = database.iconPackDao()

    val allCustomIcons: Flow<List<CustomIcon>> = customIconDao.getAllCustomIcons().map { list ->
        list.map { it.toDomain() }
    }

    val allIconPacks: Flow<List<IconPack>> = iconPackDao.getAllPacks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getCustomIconByPackage(packageName: String): CustomIcon? = withContext(Dispatchers.IO) {
        customIconDao.getByPackageName(packageName)?.toDomain()
    }

    suspend fun getCustomIconById(id: Long): CustomIcon? = withContext(Dispatchers.IO) {
        customIconDao.getById(id)?.toDomain()
    }

    /**
     * Saves a custom icon: writes image file without watermarks, saves record in database.
     */
    suspend fun saveCustomIcon(
        packageName: String,
        appName: String,
        bitmap: Bitmap,
        sourceImagePath: String?,
        configuration: EditorConfiguration
    ): CustomIcon = withContext(Dispatchers.IO) {
        val savedIconPath = storageManager.saveCustomIcon(packageName, bitmap)
        val entity = CustomIconEntity(
            packageName = packageName,
            appName = appName,
            iconPath = savedIconPath,
            sourceImagePath = sourceImagePath,
            configurationJson = configuration.toJsonString(),
            updatedAt = System.currentTimeMillis()
        )
        val id = customIconDao.insert(entity)
        entity.copy(id = id).toDomain()
    }

    suspend fun deleteCustomIcon(id: Long, packageName: String) = withContext(Dispatchers.IO) {
        customIconDao.deleteById(id)
        storageManager.deleteIconFiles(packageName)
    }

    suspend fun duplicateCustomIcon(icon: CustomIcon): CustomIcon = withContext(Dispatchers.IO) {
        val originalBmp = storageManager.loadBitmap(icon.iconPath)
        val newPackage = "${icon.packageName}.copy_${System.currentTimeMillis() % 10000}"
        val newPath = if (originalBmp != null) {
            storageManager.saveCustomIcon(newPackage, originalBmp)
        } else {
            icon.iconPath
        }

        val entity = CustomIconEntity(
            packageName = newPackage,
            appName = "${icon.appName} (Copy)",
            iconPath = newPath,
            sourceImagePath = icon.sourceImagePath,
            configurationJson = icon.configuration.toJsonString(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newId = customIconDao.insert(entity)
        entity.copy(id = newId).toDomain()
    }

    /**
     * Creates an Icon Pack containing the specified custom icons.
     */
    suspend fun createIconPack(
        name: String,
        description: String,
        author: String,
        version: String,
        selectedIcons: List<CustomIcon>,
        installedApps: List<AppInfo>
    ): Long = withContext(Dispatchers.IO) {
        val packEntity = IconPackEntity(
            name = name,
            description = description,
            author = author,
            version = version,
            previewPath = null,
            iconCount = selectedIcons.size,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val packId = iconPackDao.insertPack(packEntity)

        val appMap = installedApps.associateBy { it.packageName }
        val itemEntities = selectedIcons.map { icon ->
            val app = appMap[icon.packageName]
            val launcherActivity = app?.launcherActivity ?: "${icon.packageName}.MainActivity"
            val drawableName = ResourceNameNormalizer.normalize(icon.packageName, icon.appName)

            IconPackItemEntity(
                packId = packId,
                customIconId = icon.id,
                packageName = icon.packageName,
                appName = icon.appName,
                launcherActivity = launcherActivity,
                drawableName = drawableName
            )
        }
        iconPackDao.insertPackItems(itemEntities)
        packId
    }

    suspend fun deletePack(packId: Long) = withContext(Dispatchers.IO) {
        iconPackDao.deletePackById(packId)
    }

    suspend fun getPackItems(packId: Long): List<Pair<IconPackItem, CustomIcon>> = withContext(Dispatchers.IO) {
        val itemEntities = iconPackDao.getItemsForPackSync(packId)
        itemEntities.mapNotNull { itemEntity ->
            val iconEntity = customIconDao.getById(itemEntity.customIconId)
            if (iconEntity != null) {
                itemEntity.toDomain() to iconEntity.toDomain()
            } else {
                null
            }
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        customIconDao.deleteAll()
        storageManager.clearAllIcons()
        storageManager.clearCache()
    }

    // Extensions
    private fun CustomIconEntity.toDomain(): CustomIcon {
        return CustomIcon(
            id = id,
            packageName = packageName,
            appName = appName,
            iconPath = iconPath,
            sourceImagePath = sourceImagePath,
            configuration = EditorConfiguration.fromJsonString(configurationJson),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun IconPackEntity.toDomain(): IconPack {
        return IconPack(
            id = id,
            name = name,
            description = description,
            author = author,
            version = version,
            previewPath = previewPath,
            iconCount = iconCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun IconPackItemEntity.toDomain(): IconPackItem {
        return IconPackItem(
            id = id,
            packId = packId,
            customIconId = customIconId,
            packageName = packageName,
            appName = appName,
            launcherActivity = launcherActivity,
            drawableName = drawableName
        )
    }
}
