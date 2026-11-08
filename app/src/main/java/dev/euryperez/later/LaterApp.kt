package dev.euryperez.later

import android.app.Application
import dev.euryperez.later.data.LaterDatabase
import dev.euryperez.later.data.SampleTasks
import dev.euryperez.later.data.TaskRepository
import dev.euryperez.later.data.remote.RemoteTaskSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LaterApp : Application() {

    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = LaterDatabase.create(this)
        repository = TaskRepository(database.taskDao(), RemoteTaskSource(this))

        runBlocking {
            if (repository.isEmpty()) {
                repository.insertSampleData(SampleTasks.generate(count = 5_000))
            }
            // Warm up the task list so the first screen is never empty.
            repository.tasks.first()
        }
    }
}
