package com.enzvuck.icon.domain.model

import org.json.JSONObject

/**
 * Represents an installed launchable application on the user's device.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val launcherActivity: String,
    val isCustomized: Boolean = false,
    val customIconId: Long? = null,
    val customIconPath: String? = null
)

/**
 * Supported geometric clipping shapes for icons.
 */
enum class IconShape {
    ORIGINAL,
    SQUARE,
    ROUNDED_SQUARE,
    SQUIRCLE,
    CIRCLE,
    HEXAGON,
    CUSTOM_RADIUS
}

/**
 * Supported background types for custom icons.
 */
enum class BackgroundType {
    TRANSPARENT,
    ORIGINAL,
    WHITE,
    BLACK,
    COLOR,
    GRADIENT_LINEAR,
    GRADIENT_RADIAL
}

/**
 * Preset filters for icon adjustments.
 */
enum class IconFilter {
    ORIGINAL,
    MONO,
    DARK,
    LIGHT,
    CONTRAST,
    SOFT,
    INVERT
}

data class CropConfig(
    val isSquare: Boolean = true,
    val zoom: Float = 1.0f,
    val panX: Float = 0.0f,
    val panY: Float = 0.0f,
    val rotation: Float = 0.0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("isSquare", isSquare)
        put("zoom", zoom.toDouble())
        put("panX", panX.toDouble())
        put("panY", panY.toDouble())
        put("rotation", rotation.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): CropConfig {
            if (json == null) return CropConfig()
            return CropConfig(
                isSquare = json.optBoolean("isSquare", true),
                zoom = json.optDouble("zoom", 1.0).toFloat(),
                panX = json.optDouble("panX", 0.0).toFloat(),
                panY = json.optDouble("panY", 0.0).toFloat(),
                rotation = json.optDouble("rotation", 0.0).toFloat()
            )
        }
    }
}

data class TransformConfig(
    val scale: Float = 1.0f,
    val posX: Float = 0.0f,
    val posY: Float = 0.0f,
    val rotation: Float = 0.0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("scale", scale.toDouble())
        put("posX", posX.toDouble())
        put("posY", posY.toDouble())
        put("rotation", rotation.toDouble())
        put("flipHorizontal", flipHorizontal)
        put("flipVertical", flipVertical)
    }

    companion object {
        fun fromJson(json: JSONObject?): TransformConfig {
            if (json == null) return TransformConfig()
            return TransformConfig(
                scale = json.optDouble("scale", 1.0).toFloat(),
                posX = json.optDouble("posX", 0.0).toFloat(),
                posY = json.optDouble("posY", 0.0).toFloat(),
                rotation = json.optDouble("rotation", 0.0).toFloat(),
                flipHorizontal = json.optBoolean("flipHorizontal", false),
                flipVertical = json.optBoolean("flipVertical", false)
            )
        }
    }
}

data class ShapeConfig(
    val shape: IconShape = IconShape.ROUNDED_SQUARE,
    val customRadius: Float = 0.25f // 0.0f to 0.5f (relative to size)
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("shape", shape.name.lowercase())
        put("customRadius", customRadius.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): ShapeConfig {
            if (json == null) return ShapeConfig()
            val shapeStr = json.optString("shape", "rounded_square").uppercase()
            val shape = try { IconShape.valueOf(shapeStr) } catch (_: Exception) { IconShape.ROUNDED_SQUARE }
            return ShapeConfig(
                shape = shape,
                customRadius = json.optDouble("customRadius", 0.25).toFloat()
            )
        }
    }
}

data class BackgroundConfig(
    val type: BackgroundType = BackgroundType.TRANSPARENT,
    val primaryColor: Long = 0xFF1C1F26,
    val secondaryColor: Long = 0xFF0D0E12,
    val gradientAngle: Float = 45f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("type", type.name.lowercase())
        put("primaryColor", primaryColor)
        put("secondaryColor", secondaryColor)
        put("gradientAngle", gradientAngle.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): BackgroundConfig {
            if (json == null) return BackgroundConfig()
            val typeStr = json.optString("type", "transparent").uppercase()
            val type = try { BackgroundType.valueOf(typeStr) } catch (_: Exception) { BackgroundType.TRANSPARENT }
            return BackgroundConfig(
                type = type,
                primaryColor = json.optLong("primaryColor", 0xFF1C1F26),
                secondaryColor = json.optLong("secondaryColor", 0xFF0D0E12),
                gradientAngle = json.optDouble("gradientAngle", 45.0).toFloat()
            )
        }
    }
}

data class BorderConfig(
    val enabled: Boolean = false,
    val thickness: Float = 4f, // dp
    val color: Long = 0xFFD8FD49,
    val opacity: Float = 1.0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("enabled", enabled)
        put("thickness", thickness.toDouble())
        put("color", color)
        put("opacity", opacity.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): BorderConfig {
            if (json == null) return BorderConfig()
            return BorderConfig(
                enabled = json.optBoolean("enabled", false),
                thickness = json.optDouble("thickness", 4.0).toFloat(),
                color = json.optLong("color", 0xFFD8FD49),
                opacity = json.optDouble("opacity", 1.0).toFloat()
            )
        }
    }
}

data class ShadowConfig(
    val enabled: Boolean = false,
    val opacity: Float = 0.4f,
    val blur: Float = 12f,
    val offsetX: Float = 0f,
    val offsetY: Float = 8f,
    val spread: Float = 0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("enabled", enabled)
        put("opacity", opacity.toDouble())
        put("blur", blur.toDouble())
        put("offsetX", offsetX.toDouble())
        put("offsetY", offsetY.toDouble())
        put("spread", spread.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): ShadowConfig {
            if (json == null) return ShadowConfig()
            return ShadowConfig(
                enabled = json.optBoolean("enabled", false),
                opacity = json.optDouble("opacity", 0.4).toFloat(),
                blur = json.optDouble("blur", 12.0).toFloat(),
                offsetX = json.optDouble("offsetX", 0.0).toFloat(),
                offsetY = json.optDouble("offsetY", 8.0).toFloat(),
                spread = json.optDouble("spread", 0.0).toFloat()
            )
        }
    }
}

data class AdjustmentConfig(
    val brightness: Float = 0.0f,   // -1.0 to 1.0
    val contrast: Float = 1.0f,     // 0.0 to 2.0
    val saturation: Float = 1.0f,   // 0.0 to 2.0
    val exposure: Float = 0.0f,     // -1.0 to 1.0
    val opacity: Float = 1.0f,      // 0.0 to 1.0
    val grayscale: Boolean = false,
    val invert: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("brightness", brightness.toDouble())
        put("contrast", contrast.toDouble())
        put("saturation", saturation.toDouble())
        put("exposure", exposure.toDouble())
        put("opacity", opacity.toDouble())
        put("grayscale", grayscale)
        put("invert", invert)
    }

    companion object {
        fun fromJson(json: JSONObject?): AdjustmentConfig {
            if (json == null) return AdjustmentConfig()
            return AdjustmentConfig(
                brightness = json.optDouble("brightness", 0.0).toFloat(),
                contrast = json.optDouble("contrast", 1.0).toFloat(),
                saturation = json.optDouble("saturation", 1.0).toFloat(),
                exposure = json.optDouble("exposure", 0.0).toFloat(),
                opacity = json.optDouble("opacity", 1.0).toFloat(),
                grayscale = json.optBoolean("grayscale", false),
                invert = json.optBoolean("invert", false)
            )
        }
    }
}

data class FilterConfig(
    val filter: IconFilter = IconFilter.ORIGINAL
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("filter", filter.name.lowercase())
    }

    companion object {
        fun fromJson(json: JSONObject?): FilterConfig {
            if (json == null) return FilterConfig()
            val filterStr = json.optString("filter", "original").uppercase()
            val filter = try { IconFilter.valueOf(filterStr) } catch (_: Exception) { IconFilter.ORIGINAL }
            return FilterConfig(filter = filter)
        }
    }
}

/**
 * Complete editor configuration saved per custom icon.
 */
data class EditorConfiguration(
    val crop: CropConfig = CropConfig(),
    val transform: TransformConfig = TransformConfig(),
    val shape: ShapeConfig = ShapeConfig(),
    val background: BackgroundConfig = BackgroundConfig(),
    val border: BorderConfig = BorderConfig(),
    val shadow: ShadowConfig = ShadowConfig(),
    val adjustments: AdjustmentConfig = AdjustmentConfig(),
    val filter: FilterConfig = FilterConfig()
) {
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("crop", crop.toJson())
        json.put("transform", transform.toJson())
        json.put("shape", shape.toJson())
        json.put("background", background.toJson())
        json.put("border", border.toJson())
        json.put("shadow", shadow.toJson())
        json.put("adjustments", adjustments.toJson())
        json.put("filter", filter.toJson())
        return json.toString(2)
    }

    companion object {
        fun fromJsonString(jsonString: String?): EditorConfiguration {
            if (jsonString.isNullOrBlank()) return EditorConfiguration()
            return try {
                val json = JSONObject(jsonString)
                EditorConfiguration(
                    crop = CropConfig.fromJson(json.optJSONObject("crop")),
                    transform = TransformConfig.fromJson(json.optJSONObject("transform")),
                    shape = ShapeConfig.fromJson(json.optJSONObject("shape")),
                    background = BackgroundConfig.fromJson(json.optJSONObject("background")),
                    border = BorderConfig.fromJson(json.optJSONObject("border")),
                    shadow = ShadowConfig.fromJson(json.optJSONObject("shadow")),
                    adjustments = AdjustmentConfig.fromJson(json.optJSONObject("adjustments")),
                    filter = FilterConfig.fromJson(json.optJSONObject("filter"))
                )
            } catch (_: Exception) {
                EditorConfiguration()
            }
        }
    }
}

/**
 * Custom icon domain model.
 */
data class CustomIcon(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val iconPath: String,
    val sourceImagePath: String? = null,
    val configuration: EditorConfiguration = EditorConfiguration(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Icon pack domain model.
 */
data class IconPack(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val author: String = "enzvuck",
    val version: String = "1.0.0",
    val previewPath: String? = null,
    val iconCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Association of an icon within an icon pack.
 */
data class IconPackItem(
    val id: Long = 0,
    val packId: Long,
    val customIconId: Long,
    val packageName: String,
    val appName: String,
    val launcherActivity: String,
    val drawableName: String
)

/**
 * Theme package metadata model for theme.json.
 */
data class ThemePackageMetadata(
    val format: String = "enzvuck-theme",
    val version: Int = 1,
    val name: String,
    val description: String = "",
    val author: String = "enzvuck",
    val iconCount: Int,
    val preview: String = "preview.webp"
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("format", format)
        put("version", version)
        put("name", name)
        put("description", description)
        put("author", author)
        put("iconCount", iconCount)
        put("preview", preview)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): ThemePackageMetadata {
            return ThemePackageMetadata(
                format = json.optString("format", "enzvuck-theme"),
                version = json.optInt("version", 1),
                name = json.optString("name", "Untitled Pack"),
                description = json.optString("description", ""),
                author = json.optString("author", "enzvuck"),
                iconCount = json.optInt("iconCount", 0),
                preview = json.optString("preview", "preview.webp")
            )
        }
    }
}
