package dev.euryperez.later.data.remote

import com.google.gson.annotations.SerializedName

// Parsed reflectively by Gson. @SerializedName keeps the JSON keys stable after R8
// renames the fields. Default values give each class a no-args constructor, which
// Gson's bundled keep rules retain, so Gson never falls back to Unsafe.
data class RemoteTask(
    @SerializedName("title") val title: String = "",
    @SerializedName("notes") val notes: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("dueInDays") val dueInDays: Int = 0,
)

data class RemoteTaskResponse(
    @SerializedName("tasks") val tasks: List<RemoteTask> = emptyList(),
)
