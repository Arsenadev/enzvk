package com.enzvuck.icon

import com.enzvuck.icon.domain.model.BackgroundConfig
import com.enzvuck.icon.domain.model.BackgroundType
import com.enzvuck.icon.domain.model.BorderConfig
import com.enzvuck.icon.domain.model.CropConfig
import com.enzvuck.icon.domain.model.EditorConfiguration
import com.enzvuck.icon.domain.model.IconFilter
import com.enzvuck.icon.domain.model.IconShape
import com.enzvuck.icon.domain.model.ShapeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditorConfigurationTest {

    @Test
    fun testJsonSerializationRoundtrip() {
        val original = EditorConfiguration(
            crop = CropConfig(zoom = 1.8f, panX = 0.1f, panY = -0.2f),
            shape = ShapeConfig(shape = IconShape.SQUIRCLE, customRadius = 0.35f),
            background = BackgroundConfig(type = BackgroundType.COLOR, primaryColor = 0xFFD8FD49),
            border = BorderConfig(enabled = true, thickness = 4.0f, color = 0xFFFFFFFF),
            filter = com.enzvuck.icon.domain.model.FilterConfig(filter = IconFilter.MONO)
        )

        val json = original.toJsonString()
        assertNotNull(json)
        assertTrue(json.contains("squircle"))
        assertTrue(json.contains("mono"))

        val deserialized = EditorConfiguration.fromJsonString(json)
        assertEquals(original.shape.shape, deserialized.shape.shape)
        assertEquals(original.shape.customRadius, deserialized.shape.customRadius, 0.001f)
        assertEquals(original.background.type, deserialized.background.type)
        assertEquals(original.background.primaryColor, deserialized.background.primaryColor)
        assertEquals(original.border.enabled, deserialized.border.enabled)
        assertEquals(original.border.thickness, deserialized.border.thickness, 0.001f)
        assertEquals(original.filter.filter, deserialized.filter.filter)
    }
}
