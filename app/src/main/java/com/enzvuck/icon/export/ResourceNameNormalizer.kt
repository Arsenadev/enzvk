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

    private val commonPrefixes = setOf("com", "org", "net", "io", "android", "google", "app", "apps")
    private val genericSuffixes = setOf("android", "music", "app", "apps", "mobile", "client", "release", "free", "pro", "ltd", "inc", "corp", "dev", "studio", "ui", "view", "phone")

    /**
     * Normalizes a package name (e.g. "com.openai.chatgpt" -> "chatgpt", "com.spotify.music" -> "spotify", "org.telegram.messenger" -> "telegram")
     * or an app name (e.g. "ChatGPT" -> "chatgpt") into a clean resource name.
     */
    fun normalize(packageName: String, appName: String? = null): String {
        val parts = packageName.split('.').filter { it.isNotBlank() }

        // 1. If clean appName matches any package component, that's the best candidate (e.g. "ChatGPT" matches "chatgpt" in com.openai.chatgpt)
        if (!appName.isNullOrBlank()) {
            val appClean = cleanString(appName)
            val match = parts.find { it.equals(appClean, ignoreCase = true) }
            if (match != null && isValidIdentifier(match.lowercase())) {
                return match.lowercase()
            }
        }

        // 2. Try deriving from package name (preferring the specific app segment over company/prefix)
        val candidate = extractPackageKeyword(packageName)
        val normalized = cleanString(candidate)
        if (normalized.isNotBlank() && isValidIdentifier(normalized)) {
            return normalized
        }

        // 3. Fallback to app name if available
        if (!appName.isNullOrBlank()) {
            val fromApp = cleanString(appName)
            if (fromApp.isNotBlank() && isValidIdentifier(fromApp)) {
                return fromApp
            }
        }

        // 4. Final fallback: sanitized full package name
        val sanitizedPackage = packageName.replace('.', '_').replace('-', '_').lowercase()
        val finalClean = cleanString(sanitizedPackage)
        return if (isValidIdentifier(finalClean)) finalClean else "icon_${System.currentTimeMillis()}"
    }

    private fun extractPackageKeyword(packageName: String): String {
        val parts = packageName.split('.').filter { it.isNotBlank() }
        if (parts.isEmpty()) return "icon"

        // If the last part is not a generic suffix or common prefix, prefer the last part (e.g. "chatgpt" in "com.openai.chatgpt")
        val last = parts.last().lowercase()
        if (!commonPrefixes.contains(last) && !genericSuffixes.contains(last)) {
            return last
        }

        val filtered = parts.filterNot { commonPrefixes.contains(it.lowercase()) || genericSuffixes.contains(it.lowercase()) }
        return when {
            filtered.isNotEmpty() -> filtered.last()
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
