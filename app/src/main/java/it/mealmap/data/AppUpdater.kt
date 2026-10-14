package it.mealmap.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A published version: [versionCode] is the N of its tag `v1.0.N`, as in the release workflow. */
data class AppRelease(val version: String, val versionCode: Int, val apkUrl: String)

/** Updates the app from the GitHub releases of the repo, where CI publishes the signed APK. */
class AppUpdater(private val context: Context) {
    suspend fun latestRelease(): AppRelease? = withContext(Dispatchers.IO) {
        download(LATEST_RELEASE_URL) { parseRelease(it.bufferedReader().readText()) }
    }

    /** Downloads the APK of [release] and opens the system installer on it. */
    suspend fun install(release: AppRelease) {
        val apk = File(context.cacheDir, "updates/meal-map.apk")
        withContext(Dispatchers.IO) {
            apk.parentFile?.mkdirs()
            download(release.apkUrl) { input -> apk.outputStream().use { input.copyTo(it) } }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun <T> download(url: String, read: (InputStream) -> T): T {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS
        return try {
            connection.inputStream.use(read)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/LuigiFederico/meal-map/releases/latest"
        const val TIMEOUT_MILLIS = 15_000
    }
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val assets: List<GitHubAsset>,
)

@Serializable
private data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
)

private val json = Json { ignoreUnknownKeys = true }

/** The release in a GitHub API response, or null when it has no APK or an unknown tag. */
fun parseRelease(body: String): AppRelease? {
    val release = json.decodeFromString<GitHubRelease>(body)
    val versionCode = release.tagName.substringAfterLast('.').toIntOrNull() ?: return null
    val apk = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return null
    return AppRelease(release.tagName.removePrefix("v"), versionCode, apk.downloadUrl)
}
