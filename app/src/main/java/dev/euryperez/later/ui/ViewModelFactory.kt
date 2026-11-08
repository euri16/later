package dev.euryperez.later.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import dev.euryperez.later.LaterApp
import dev.euryperez.later.data.TaskRepository

val CreationExtras.repository: TaskRepository
    get() = (this[APPLICATION_KEY] as LaterApp).repository
