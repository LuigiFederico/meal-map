package it.mealmap.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUpdaterTest {
    private fun release(tag: String, vararg assets: String) = """
        {
          "tag_name": "$tag",
          "name": "$tag",
          "assets": [${assets.joinToString {
        """{"name": "$it", "browser_download_url": "https://x/$it"}"""
    }}]
        }
    """.trimIndent()

    @Test
    fun readsTheVersionAndTheApkOfTheRelease() {
        assertEquals(
            AppRelease("1.0.12", 12, "https://x/meal-map-v1.0.12.apk"),
            parseRelease(release("v1.0.12", "notes.txt", "meal-map-v1.0.12.apk")),
        )
    }

    @Test
    fun ignoresAReleaseWithoutApkOrWithAnUnknownTag() {
        assertNull(parseRelease(release("v1.0.12", "notes.txt")))
        assertNull(parseRelease(release("latest", "meal-map.apk")))
    }
}
