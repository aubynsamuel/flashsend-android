package com.aubynsamuel.flashsend.core.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object MediaCacheManager {
    private const val TAG = "MediaCacheManager"
    private const val CACHE_DIR_NAME = "media_cache"
    private const val MAX_CACHE_SIZE: Long = 300L * 1024 * 1024

    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    private fun generateFileName(url: String): String {
        return url.hashCode().toString()
    }

    fun getFileForUrl(context: Context, url: String): File {
        val file = File(getCacheDir(context), generateFileName(url))
        return file
    }

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
                    evictCacheIfNeeded(getCacheDir(context))
                }
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    Log.d(
                        TAG,
                        "Download completed but file not found. Falling back to original URL."
                    )
                    url.toUri()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for URL: $url with error: ${e.localizedMessage}", e)
                url.toUri()
            }
        }
    }

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

    private fun evictCacheIfNeeded(cacheDir: File) {
        val files = cacheDir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= MAX_CACHE_SIZE) {
            return
        }
        val sortedFiles = files.sortedBy { it.lastModified() }
        for (file in sortedFiles) {
            if (totalSize <= MAX_CACHE_SIZE) break
            val fileSize = file.length()
            if (file.delete()) {
                totalSize -= fileSize
            }
        }
    }
}