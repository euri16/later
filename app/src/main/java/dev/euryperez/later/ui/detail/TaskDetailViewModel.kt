package dev.euryperez.later.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.euryperez.later.data.Postponement
import dev.euryperez.later.data.Task
import dev.euryperez.later.data.TaskRepository
import dev.euryperez.later.ui.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskDetailState(
    val task: Task? = null,
    val postponements: List<Postponement> = emptyList(),
    val isLoading: Boolean = true,
)

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    val state: StateFlow<TaskDetailState> =
        combine(repository.task(taskId), repository.postponements(taskId)) { task, history ->
            TaskDetailState(task = task, postponements = history, isLoading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskDetailState())

    fun postpone() {
        viewModelScope.launch { repository.postpone(taskId) }
    }

    fun toggleDone() {
        val task = state.value.task ?: return
        viewModelScope.launch { repository.setDone(taskId, !task.isDone) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(taskId)
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TaskDetailViewModel(createSavedStateHandle(), repository) }
        }
    }
}
