package com.aubynsamuel.flashsend

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object MediaCacheManager {
    private const val TAG = "MediaCacheManager"
    private const val CACHE_DIR_NAME = "media_cache"

    // Ensure the cache directory exists.
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            Log.d(TAG, "Cache directory does not exist. Creating: ${cacheDir.absolutePath}")
            cacheDir.mkdirs()
        } else {
            Log.d(TAG, "Using existing cache directory: ${cacheDir.absolutePath}")
        }
        return cacheDir
    }

    // Generate a file name based on the URL.
    private fun generateFileName(url: String): String {
        val hash = url.hashCode().toString()
        val extension = url.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) "$hash.$extension" else hash
    }

    // Get the File object corresponding to this URL in the cache.
    private fun getFileForUrl(context: Context, url: String): File {
        val file = File(getCacheDir(context), generateFileName(url))
        Log.d(TAG, "Computed cache file path: ${file.absolutePath} for URL: $url")
        return file
    }

    /**
     * Returns a Uri pointing to the locally cached file.
     * If the file does not exist, it attempts to download it.
     * If the download fails, it falls back to the original remote URL.
     */
    suspend fun getMediaUri(context: Context, url: String): Uri {
        val file = getFileForUrl(context, url)
        if (file.exists()) {
            Log.d(TAG, "File exists in cache: ${file.absolutePath}. Using cached version.")
            return Uri.fromFile(file)
        } else {
            Log.d(TAG, "File does not exist in cache. Starting download for URL: $url")
            return try {
                withContext(Dispatchers.IO) {
                    downloadFile(url, file)
                }
                if (file.exists()) {
                    Log.d(TAG, "Download succeeded. File cached at: ${file.absolutePath}")
                    Uri.fromFile(file)
                } else {
                    Log.d(
                        TAG,
                        "Download completed but file not found. Falling back to original URL."
                    )
                    Uri.parse(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for URL: $url with error: ${e.localizedMessage}", e)
                Uri.parse(url)
            }
        }
    }

    /**
     * Downloads the file from the given URL and writes it to the destination file.
     * Throws an IOException if the download fails.
     */
    @Throws(IOException::class)
    private fun downloadFile(urlString: String, destFile: File) {
        Log.d(TAG, "Starting download from: $urlString")
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.connect()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            val errorMsg =
                "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}"
            Log.e(TAG, errorMsg)
            throw IOException(errorMsg)
        }
        destFile.outputStream().use { output ->
            connection.inputStream.use { input ->
                input.copyTo(output)
            }
        }
        connection.disconnect()
        Log.d(TAG, "Download finished. File saved to: ${destFile.absolutePath}")
    }
}