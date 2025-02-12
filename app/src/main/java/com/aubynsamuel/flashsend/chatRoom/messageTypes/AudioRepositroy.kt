package com.aubynsamuel.flashsend.chatRoom.messageTypes
//
//import android.content.Context
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import java.io.File
//
//class AudioRepository(private val context: Context) {
//    private val cacheDir = context.cacheDir
//
//    // Check if an audio file is already cached
//    fun getCachedAudio(url: String): File? {
//        val fileName = url.hashCode().toString()
//        val file = File(cacheDir, fileName)
//        return if (file.exists()) file else null
//    }
//
//    // Download the audio file using OkHttp and save it to the cache
//    fun downloadAudio(
//        url: String,
//        onSuccess: (File) -> Unit,
//        onError: (String) -> Unit
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val client = OkHttpClient()
//                val request = Request.Builder().url(url).build()
//                val response = client.newCall(request).execute()
//
//                if (!response.isSuccessful) {
//                    withContext(Dispatchers.Main) {
//                        onError("Download failed: ${response.code}")
//                    }
//                    return@launch
//                }
//
//                val responseBody = response.body
//                if (responseBody == null) {
//                    withContext(Dispatchers.Main) {
//                        onError("Download failed: Empty response body")
//                    }
//                    return@launch
//                }
//
//                val fileName = url.hashCode().toString()
//                val file = File(cacheDir, fileName)
//
//                // Write the downloaded data into the cache file
//                file.outputStream().use { fileOut ->
//                    responseBody.byteStream().use { input ->
//                        input.copyTo(fileOut)
//                    }
//                }
//
//                withContext(Dispatchers.Main) {
//                    onSuccess(file)
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    onError("Download failed: ${e.message}")
//                }
//            }
//        }
//    }
//}