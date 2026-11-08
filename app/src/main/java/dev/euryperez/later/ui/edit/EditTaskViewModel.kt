package dev.euryperez.later.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.euryperez.later.data.Category
import dev.euryperez.later.data.Task
import dev.euryperez.later.data.TaskRepository
import dev.euryperez.later.ui.repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditTaskState(
    val title: String = "",
    val notes: String = "",
    val category: Category = Category.WORK,
    val dueDate: LocalDate = LocalDate.now(),
    val isNew: Boolean = true,
) {
    val canSave: Boolean get() = title.isNotBlank()
}

class EditTaskViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {

    private val taskId: Long = savedStateHandle["taskId"] ?: 0L
    private var original: Task? = null

    private val _state = MutableStateFlow(EditTaskState())
    val state: StateFlow<EditTaskState> = _state.asStateFlow()

    init {
        if (taskId != 0L) {
            viewModelScope.launch {
                val task = repository.task(taskId).first() ?: return@launch
                original = task
                _state.value = EditTaskState(task.title, task.notes, task.category, task.dueDate, isNew = false)
            }
        }
    }

    fun setTitle(value: String) = _state.update { it.copy(title = value) }
    fun setNotes(value: String) = _state.update { it.copy(notes = value) }
    fun setCategory(value: Category) = _state.update { it.copy(category = value) }
    fun setDueDate(value: LocalDate) = _state.update { it.copy(dueDate = value) }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return
        viewModelScope.launch {
            val base = original ?: Task(title = "", notes = "", category = current.category, dueDate = current.dueDate)
            repository.save(
                base.copy(
                    title = current.title.trim(),
                    notes = current.notes.trim(),
                    category = current.category,
                    dueDate = current.dueDate,
                ),
            )
            onSaved()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { EditTaskViewModel(createSavedStateHandle(), repository) }
        }
    }
}
