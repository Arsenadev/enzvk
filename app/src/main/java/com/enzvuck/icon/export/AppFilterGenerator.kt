package com.enzvuck.icon.export

import com.enzvuck.icon.domain.model.IconPackItem

/**
 * Generates standard Android launcher-compatible `appfilter.xml` files.
 *
 * Each item maps:
 * ComponentInfo{<packageName>/<launcherActivity>} -> <drawableName>
 */
object AppFilterGenerator {

    /**
     * Generates a complete appfilter.xml document string for the given pack items.
     */
    fun generate(items: List<IconPackItem>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")

        val sortedItems = items.sortedBy { it.packageName }
        for (item in sortedItems) {
            val component = if (item.launcherActivity.isNotBlank()) {
                "${item.packageName}/${item.launcherActivity}"
            } else {
                "${item.packageName}/${item.packageName}.MainActivity"
            }

            sb.append("    <!-- ${escapeXml(item.appName)} -->\n")
            sb.append("    <item component=\"ComponentInfo{$component}\" drawable=\"${item.drawableName}\" />\n")
        }

        sb.append("</resources>\n")
        return sb.toString()
    }

    /**
     * Parses an appfilter.xml document string back into component -> drawable mappings.
     */
    fun parse(xmlContent: String): List<AppFilterEntry> {
        val entries = mutableListOf<AppFilterEntry>()
        val regex = Regex("""<item\s+component="ComponentInfo\{([^/]+)/([^}]+)\}"\s+drawable="([^"]+)"""")
        val matches = regex.findAll(xmlContent)
        for (match in matches) {
            val packageName = match.groupValues[1].trim()
            val activity = match.groupValues[2].trim()
            val drawable = match.groupValues[3].trim()
            entries.add(AppFilterEntry(packageName, activity, drawable))
        }
        return entries
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    data class AppFilterEntry(
        val packageName: String,
        val launcherActivity: String,
        val drawableName: String
    )
}
