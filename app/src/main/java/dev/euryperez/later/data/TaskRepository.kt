package dev.euryperez.later.data

import dev.euryperez.later.data.remote.RemoteTaskSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskRepository(
    private val dao: TaskDao,
    private val remote: RemoteTaskSource,
) {
    val tasks: Flow<List<Task>> = dao.observeAll()

    fun task(id: Long): Flow<Task?> = dao.observe(id)

    fun postponements(taskId: Long): Flow<List<Postponement>> = dao.observePostponements(taskId)

    fun postponementsSince(since: Long): Flow<List<Postponement>> = dao.observePostponementsSince(since)

    suspend fun save(task: Task): Long =
        if (task.id == 0L) {
            dao.insert(task)
        } else {
            dao.update(task)
            task.id
        }

    // Pushes the task back one week so it stops nagging you until next sprint.
    suspend fun postpone(id: Long) {
        val task = dao.get(id) ?: return
        val newDate = maxOf(task.dueDate, LocalDate.now()).plusDays(1)
        dao.update(task.copy(dueDate = newDate, postponeCount = task.postponeCount + 1))
        dao.insertPostponement(Postponement(taskId = id, fromDate = task.dueDate, toDate = newDate))
    }

    suspend fun setDone(id: Long, done: Boolean) {
        val task = dao.get(id) ?: return
        dao.update(task.copy(isDone = done))
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun sync(): Int {
        val remoteTasks = remote.fetch()
        val today = LocalDate.now()
        dao.insertAll(
            remoteTasks.map {
                Task(
                    title = it.title,
                    notes = it.notes,
                    category = Category.valueOf(it.category),
                    dueDate = today.plusDays(it.dueInDays.toLong()),
                )
            },
        )
        return remoteTasks.size
    }

    suspend fun isEmpty(): Boolean = dao.count() == 0

    fun lastPostponement(taskId: Long): Postponement? = dao.lastPostponement(taskId)

    suspend fun insertSampleData(tasks: List<Task>) {
        val ids = dao.insertAll(tasks)
        dao.insertPostponements(
            tasks.zip(ids).flatMap { (task, id) -> SampleTasks.history(id, task) },
        )
    }
}
