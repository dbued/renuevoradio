package com.example.data.remote

import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ArtworkService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    private val tag = "ArtworkService"
    private val cache = LruCache<String, String>(100)

    suspend fun getArtworkUrl(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        val cleanArtist = cleanSearchTerm(artist)
        val cleanTitle = cleanSearchTerm(title)

        // Ignore generic station names
        if (cleanTitle.isBlank() || cleanTitle.contains("Transmisión en Vivo", ignoreCase = true)) {
            return@withContext null
        }

        val cacheKey = "$cleanArtist::$cleanTitle".lowercase()
        cache.get(cacheKey)?.let { return@withContext it }

        // Try combined search first: "Artist Title"
        var foundUrl = searchItunes("$cleanArtist $cleanTitle")

        // If not found, try searching just title if it's descriptive enough
        if (foundUrl == null && cleanTitle.length > 3) {
            foundUrl = searchItunes(cleanTitle)
        }

        if (foundUrl != null) {
            cache.put(cacheKey, foundUrl)
        }

        return@withContext foundUrl
    }

    private fun searchItunes(term: String): String? {
        try {
            val encodedTerm = URLEncoder.encode(term.trim(), "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedTerm&entity=song&limit=1"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "RenuevoRadio/2.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return null
            }

            val bodyString = response.body?.string() ?: return null
            response.close()

            val json = JSONObject(bodyString)
            val resultCount = json.optInt("resultCount", 0)
            if (resultCount > 0) {
                val results = json.getJSONArray("results")
                if (results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val rawArtworkUrl = if (first.has("artworkUrl100")) first.getString("artworkUrl100") else null
                    if (!rawArtworkUrl.isNullOrBlank()) {
                        // Request high quality 600x600 image instead of 100x100 thumbnail
                        val hdArtworkUrl = rawArtworkUrl.replace("100x100bb", "600x600bb")
                        Log.d(tag, "Found iTunes artwork for '$term': $hdArtworkUrl")
                        return hdArtworkUrl
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed iTunes artwork lookup for '$term': ${e.message}")
        }
        return null
    }

    private fun cleanSearchTerm(term: String): String {
        return term
            .replace(Regex("(?i)ft\\.?|feat\\.?|featuring"), "")
            .replace(Regex("(?i)Brandon lake cover|live|en vivo|radio|version"), "")
            .replace(Regex("[()\\[\\]\\-_]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
