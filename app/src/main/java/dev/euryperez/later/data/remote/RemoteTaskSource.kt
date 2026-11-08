package dev.euryperez.later.data.remote

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class RemoteTaskSource(
    private val context: Context,
    private val gson: Gson = Gson(),
) {
    suspend fun fetch(): List<RemoteTask> = withContext(Dispatchers.IO) {
        delay(NETWORK_DELAY_MS)
        val json = context.assets.open("remote_tasks.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, RemoteTaskResponse::class.java).tasks
    }

    private companion object {
        const val NETWORK_DELAY_MS = 1_500L
    }
}
