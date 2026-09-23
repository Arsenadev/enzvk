package com.enzvuck.icon.export

/**
 * Normalizes application package names and labels into valid Android drawable resource identifiers.
 *
 * Rules:
 * - Lowercase only
 * - Must start with a lowercase letter or underscore
 * - Only letters [a-z], digits [0-9], and underscores [_]
 * - No spaces, dashes, dots, or invalid symbols
 * - Non-empty
 */
object ResourceNameNormalizer {

    /**
     * Normalizes a package name (e.g. "com.spotify.music" -> "spotify", "org.telegram.messenger" -> "telegram")
     * or an app name (e.g. "Google Chrome" -> "chrome") into a clean resource name.
     */
    fun normalize(packageName: String, appName: String? = null): String {
        // First try deriving a clean name from the package name
        val candidate = extractPackageKeyword(packageName)
        val normalized = cleanString(candidate)
        if (normalized.isNotBlank() && isValidIdentifier(normalized)) {
            return normalized
        }

        // Fallback to app name if available
        if (!appName.isNullOrBlank()) {
            val fromApp = cleanString(appName)
            if (fromApp.isNotBlank() && isValidIdentifier(fromApp)) {
                return fromApp
            }
        }

        // Final fallback: sanitized full package name
        val sanitizedPackage = packageName.replace('.', '_').replace('-', '_').lowercase()
        val finalClean = cleanString(sanitizedPackage)
        return if (isValidIdentifier(finalClean)) finalClean else "icon_${System.currentTimeMillis()}"
    }

    private fun extractPackageKeyword(packageName: String): String {
        val parts = packageName.split('.').filter { it.isNotBlank() }
        if (parts.isEmpty()) return "icon"

        // Common package prefixes to strip
        val commonPrefixes = setOf("com", "org", "net", "io", "android", "google", "app", "apps")
        val filtered = parts.filterNot { commonPrefixes.contains(it.lowercase()) }

        return when {
            filtered.isNotEmpty() -> filtered.first()
            else -> parts.last()
        }
    }

    fun cleanString(input: String): String {
        val lower = input.lowercase().trim()
        val replaced = lower.replace(Regex("[^a-z0-9_]"), "_")
        val collapsed = replaced.replace(Regex("_+"), "_").trim('_')

        return if (collapsed.isNotEmpty() && collapsed[0].isDigit()) {
            "ic_$collapsed"
        } else {
            collapsed
        }
    }

    fun isValidIdentifier(name: String): Boolean {
        if (name.isEmpty()) return false
        val first = name[0]
        if (!first.isLetter() && first != '_') return false
        for (ch in name) {
            if (!ch.isLetterOrDigit() && ch != '_') return false
        }
        return true
    }
}
