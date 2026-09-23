package com.enzvuck.icon.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_icons",
    indices = [Index(value = ["packageName"], unique = true)]
)
data class CustomIconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val launcherActivity: String = "",
    val resourceName: String = "",
    val iconPath: String,
    val sourceImagePath: String?,
    val configurationJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "icon_packs")
data class IconPackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val previewPath: String?,
    val iconCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "icon_pack_items",
    foreignKeys = [
        ForeignKey(
            entity = IconPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomIconEntity::class,
            parentColumns = ["id"],
            childColumns = ["customIconId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["packId"]),
        Index(value = ["customIconId"]),
        Index(value = ["packId", "packageName"], unique = true)
    ]
)
data class IconPackItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packId: Long,
    val customIconId: Long,
    val packageName: String,
    val appName: String,
    val launcherActivity: String,
    val drawableName: String
)
