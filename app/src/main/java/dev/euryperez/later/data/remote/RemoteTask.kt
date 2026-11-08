package dev.euryperez.later.data.remote

data class RemoteTask(
    val title: String,
    val notes: String,
    val category: String,
    val dueInDays: Int,
)

data class RemoteTaskResponse(
    val tasks: List<RemoteTask>,
)
