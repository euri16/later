package dev.euryperez.later.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.euryperez.later.data.Task
import dev.euryperez.later.data.TaskRepository
import dev.euryperez.later.ui.repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, TODAY, OVERDUE, DONE }

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface TaskListEvent {
    data class Completed(val taskId: Long, val title: String) : TaskListEvent
    data class Synced(val count: Int) : TaskListEvent
}

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val syncing = MutableStateFlow(false)
    private val _events = Channel<TaskListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state: StateFlow<TaskListState> =
        combine(repository.tasks, filter, syncing) { tasks, filter, syncing ->
            TaskListState(tasks = tasks, filter = filter, isSyncing = syncing, isLoading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListState())

    fun setFilter(value: TaskFilter) = filter.update { value }

    fun postpone(id: Long) {
        viewModelScope.launch { repository.postpone(id) }
    }

    fun complete(task: Task) {
        viewModelScope.launch {
            repository.setDone(task.id, true)
            _events.send(TaskListEvent.Completed(task.id, task.title))
        }
    }

    fun undoComplete(id: Long) {
        viewModelScope.launch { repository.setDone(id, false) }
    }

    fun sync() {
        if (syncing.value) return
        viewModelScope.launch {
            syncing.value = true
            try {
                val count = repository.sync()
                _events.send(TaskListEvent.Synced(count))
            } finally {
                syncing.value = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TaskListViewModel(repository) }
        }
    }
}
