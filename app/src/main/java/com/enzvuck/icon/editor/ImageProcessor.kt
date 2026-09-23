package com.enzvuck.icon.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.enzvuck.icon.domain.model.BackgroundType
import com.enzvuck.icon.domain.model.EditorConfiguration
import com.enzvuck.icon.domain.model.IconFilter
import com.enzvuck.icon.domain.model.IconShape
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Pure graphics engine for processing custom icons.
 *
 * CRITICAL CLEAN ICON RULE:
 * This processor NEVER adds any watermark, enzvuck logo, 'E' badge, or external branding.
 * It produces clean, pristine icon bitmaps exactly as configured by the user.
 */
object ImageProcessor {

    /**
     * Renders a processed icon bitmap from the source bitmap according to the EditorConfiguration.
     * Output size defaults to standard Android icon size (512x512).
     */
    fun processIcon(
        source: Bitmap,
        config: EditorConfiguration,
        targetSize: Int = 512
    ): Bitmap {
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Prepare transformed & color-adjusted source image layer
        val adjustedSource = renderTransformedSource(source, config, targetSize)

        // 2. Render background layer (if any)
        renderBackground(canvas, config, targetSize)

        // 3. Composite transformed source inside shape mask
        val maskedSource = applyShapeMask(adjustedSource, config, targetSize)
        canvas.drawBitmap(maskedSource, 0f, 0f, null)

        // 4. Draw border if enabled
        renderBorder(canvas, config, targetSize)

        return output
    }

    /**
     * Applies crop, transform, color adjustments, and filters to source bitmap.
     */
    private fun renderTransformedSource(
        source: Bitmap,
        config: EditorConfiguration,
        targetSize: Int
    ): Bitmap {
        val layer = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(layer)

        val matrix = Matrix()

        // Center the source onto target canvas
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()

        val baseScale = min(targetSize / srcW, targetSize / srcH)
        matrix.postTranslate(-srcW / 2f, -srcH / 2f)

        // Apply Crop zoom & pan
        val cropZoom = config.crop.zoom.coerceIn(0.1f, 5.0f)
        matrix.postScale(cropZoom, cropZoom)
        matrix.postRotate(config.crop.rotation)
        matrix.postTranslate(config.crop.panX * targetSize, config.crop.panY * targetSize)

        // Apply Transform controls
        val transformScale = config.transform.scale.coerceIn(0.1f, 5.0f)
        val scaleX = if (config.transform.flipHorizontal) -transformScale else transformScale
        val scaleY = if (config.transform.flipVertical) -transformScale else transformScale
        matrix.postScale(scaleX, scaleY)
        matrix.postRotate(config.transform.rotation)

        matrix.postScale(baseScale, baseScale)
        matrix.postTranslate(
            targetSize / 2f + config.transform.posX * targetSize,
            targetSize / 2f + config.transform.posY * targetSize
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = createColorFilter(config)
        paint.alpha = (config.adjustments.opacity.coerceIn(0f, 1f) * 255).toInt()

        canvas.drawBitmap(source, matrix, paint)
        return layer
    }

    /**
     * Constructs composite ColorFilter from adjustments and filter presets.
     */
    fun createColorFilter(config: EditorConfiguration): ColorMatrixColorFilter {
        val cm = ColorMatrix()

        // Brightness & Contrast
        // Brightness: -1.0 to 1.0 -> offset in range -255 to 255
        // Contrast: 0.0 to 2.0 -> scale
        val contrast = config.adjustments.contrast.coerceIn(0f, 3f)
        val brightnessOffset = (config.adjustments.brightness.coerceIn(-1f, 1f) + config.adjustments.exposure.coerceIn(-1f, 1f)) * 255f
        val t = (1.0f - contrast) / 2.0f * 255f

        val cmContrast = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, t + brightnessOffset,
                0f, contrast, 0f, 0f, t + brightnessOffset,
                0f, 0f, contrast, 0f, t + brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(cmContrast)

        // Saturation
        val sat = config.adjustments.saturation.coerceIn(0f, 3f)
        val cmSat = ColorMatrix().apply { setSaturation(if (config.adjustments.grayscale) 0f else sat) }
        cm.postConcat(cmSat)

        // Invert
        if (config.adjustments.invert) {
            val cmInvert = ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(cmInvert)
        }

        // Apply Preset Filters
        when (config.filter.filter) {
            IconFilter.ORIGINAL -> { /* no-op */ }
            IconFilter.MONO -> {
                val mono = ColorMatrix().apply { setSaturation(0f) }
                cm.postConcat(mono)
            }
            IconFilter.DARK -> {
                val dark = ColorMatrix(
                    floatArrayOf(
                        0.7f, 0f, 0f, 0f, -30f,
                        0f, 0.7f, 0f, 0f, -30f,
                        0f, 0f, 0.7f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(dark)
            }
            IconFilter.LIGHT -> {
                val light = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, 40f,
                        0f, 1.2f, 0f, 0f, 40f,
                        0f, 0f, 1.2f, 0f, 40f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(light)
            }
            IconFilter.CONTRAST -> {
                val hiContrast = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, -50f,
                        0f, 1.4f, 0f, 0f, -50f,
                        0f, 0f, 1.4f, 0f, -50f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(hiContrast)
            }
            IconFilter.SOFT -> {
                val soft = ColorMatrix(
                    floatArrayOf(
                        0.9f, 0f, 0f, 0f, 25f,
                        0f, 0.9f, 0f, 0f, 25f,
                        0f, 0f, 0.9f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(soft)
            }
            IconFilter.INVERT -> {
                val inv = ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(inv)
            }
        }

        return ColorMatrixColorFilter(cm)
    }

    /**
     * Renders background layer based on background configuration and shape.
     */
    private fun renderBackground(canvas: Canvas, config: EditorConfiguration, size: Int) {
        val bgType = config.background.type
        if (bgType == BackgroundType.TRANSPARENT || bgType == BackgroundType.ORIGINAL) {
            return
        }

        val shapePath = createShapePath(config, size)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        when (bgType) {
            BackgroundType.WHITE -> paint.color = Color.WHITE
            BackgroundType.BLACK -> paint.color = Color.BLACK
            BackgroundType.COLOR -> paint.color = config.background.primaryColor.toInt()
            BackgroundType.GRADIENT_LINEAR -> {
                val angleRad = Math.toRadians(config.background.gradientAngle.toDouble())
                val x0 = size / 2f - (size / 2f) * cos(angleRad).toFloat()
                val y0 = size / 2f - (size / 2f) * sin(angleRad).toFloat()
                val x1 = size / 2f + (size / 2f) * cos(angleRad).toFloat()
                val y1 = size / 2f + (size / 2f) * sin(angleRad).toFloat()

                paint.shader = LinearGradient(
                    x0, y0, x1, y1,
                    config.background.primaryColor.toInt(),
                    config.background.secondaryColor.toInt(),
                    Shader.TileMode.CLAMP
                )
            }
            BackgroundType.GRADIENT_RADIAL -> {
                paint.shader = RadialGradient(
                    size / 2f, size / 2f, size / 2f,
                    config.background.primaryColor.toInt(),
                    config.background.secondaryColor.toInt(),
                    Shader.TileMode.CLAMP
                )
            }
            else -> {}
        }

        // Draw shadow under background if enabled
        if (config.shadow.enabled) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.BLACK
                alpha = (config.shadow.opacity.coerceIn(0f, 1f) * 255).toInt()
            }
            canvas.save()
            canvas.translate(config.shadow.offsetX, config.shadow.offsetY)
            canvas.drawPath(shapePath, shadowPaint)
            canvas.restore()
        }

        canvas.drawPath(shapePath, paint)
    }

    /**
     * Clips the image layer to the selected shape.
     */
    private fun applyShapeMask(source: Bitmap, config: EditorConfiguration, size: Int): Bitmap {
        if (config.shape.shape == IconShape.ORIGINAL) {
            return source
        }

        val masked = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(masked)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        val shapePath = createShapePath(config, size)
        canvas.drawPath(shapePath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return masked
    }

    /**
     * Creates the geometric Path for the chosen icon shape.
     */
    fun createShapePath(config: EditorConfiguration, size: Int): Path {
        val path = Path()
        val s = size.toFloat()
        val rect = RectF(0f, 0f, s, s)

        when (config.shape.shape) {
            IconShape.ORIGINAL, IconShape.SQUARE -> {
                path.addRect(rect, Path.Direction.CW)
            }
            IconShape.ROUNDED_SQUARE -> {
                val radius = s * 0.22f
                path.addRoundRect(rect, radius, radius, Path.Direction.CW)
            }
            IconShape.SQUIRCLE -> {
                // Approximate superellipse / squircle
                val r = s * 0.46f
                val cx = s / 2f
                val cy = s / 2f
                val n = 4.0 // power for squircle
                val step = 360
                for (i in 0..step) {
                    val angle = Math.toRadians(i.toDouble())
                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val sgnCos = if (cosA >= 0) 1 else -1
                    val sgnSin = if (sinA >= 0) 1 else -1
                    val x = cx + r * sgnCos * Math.pow(Math.abs(cosA), 2.0 / n).toFloat()
                    val y = cy + r * sgnSin * Math.pow(Math.abs(sinA), 2.0 / n).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            IconShape.CIRCLE -> {
                path.addCircle(s / 2f, s / 2f, s / 2f, Path.Direction.CW)
            }
            IconShape.HEXAGON -> {
                val cx = s / 2f
                val cy = s / 2f
                val r = s / 2f
                for (i in 0 until 6) {
                    val angle = Math.toRadians(60.0 * i - 30.0)
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            IconShape.CUSTOM_RADIUS -> {
                val radius = s * config.shape.customRadius.coerceIn(0.0f, 0.5f)
                path.addRoundRect(rect, radius, radius, Path.Direction.CW)
            }
        }
        return path
    }

    /**
     * Draws border stroke along the shape path if border is enabled.
     */
    private fun renderBorder(canvas: Canvas, config: EditorConfiguration, size: Int) {
        if (!config.border.enabled || config.border.thickness <= 0f) return

        val shapePath = createShapePath(config, size)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = config.border.thickness * (size / 100f) // scale with resolution
            color = config.border.color.toInt()
            alpha = (config.border.opacity.coerceIn(0f, 1f) * 255).toInt()
        }
        canvas.drawPath(shapePath, borderPaint)
    }
}
