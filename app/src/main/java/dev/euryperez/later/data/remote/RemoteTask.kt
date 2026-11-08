package dev.euryperez.later.data.remote

import com.google.gson.annotations.SerializedName

// Parsed reflectively by Gson. @SerializedName keeps the JSON keys stable after R8
// renames the fields; Gson's bundled keep rules retain fields annotated with it.
data class RemoteTask(
    @SerializedName("title") val title: String,
    @SerializedName("notes") val notes: String,
    @SerializedName("category") val category: String,
    @SerializedName("dueInDays") val dueInDays: Int,
)

data class RemoteTaskResponse(
    @SerializedName("tasks") val tasks: List<RemoteTask>,
)
