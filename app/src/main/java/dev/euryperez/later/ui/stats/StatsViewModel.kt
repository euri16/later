package dev.euryperez.later.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.euryperez.later.data.Category
import dev.euryperez.later.data.Task
import dev.euryperez.later.data.TaskRepository
import dev.euryperez.later.ui.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

data class CategoryScore(val category: Category, val postponements: Int)

data class StatsState(
    val champion: Task? = null,
    val postponedThisWeek: Int = 0,
    val ranking: List<CategoryScore> = emptyList(),
    val pending: Int = 0,
    val isLoading: Boolean = true,
)

class StatsViewModel(repository: TaskRepository) : ViewModel() {

    private val weekStart: Long = LocalDate.now()
        .with(DayOfWeek.MONDAY)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val state: StateFlow<StatsState> =
        combine(repository.tasks, repository.postponementsSince(weekStart)) { tasks, week ->
            val pending = tasks.filterNot { it.isDone }
            StatsState(
                champion = pending.maxByOrNull { it.postponeCount },
                postponedThisWeek = week.size,
                ranking = pending
                    .groupBy { it.category }
                    .map { (category, list) -> CategoryScore(category, list.sumOf { it.postponeCount }) }
                    .sortedByDescending { it.postponements },
                pending = pending.size,
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsState())

    companion object {
        val Factory = viewModelFactory {
            initializer { StatsViewModel(repository) }
        }
    }
}
