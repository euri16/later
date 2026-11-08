package dev.euryperez.later.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.euryperez.later.R
import dev.euryperez.later.data.Category
import dev.euryperez.later.data.Task
import dev.euryperez.later.ui.theme.Fog
import dev.euryperez.later.ui.theme.Overdue
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TaskRowModel(
    val id: Long,
    val title: String,
    val notes: String,
    val category: Category,
    val dueText: String,
    val isOverdue: Boolean,
    val isDone: Boolean,
    val postponeCount: Int,
    val tags: List<String>,
    val task: Task,
)

private val dueFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskListEvent.Completed -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Done: ${event.title}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoComplete(event.taskId)
                }
                is TaskListEvent.Synced -> snackbarHostState.showSnackbar("${event.count} tasks synced")
            }
        }
    }

    val today = LocalDate.now()
    val visible = state.tasks
        .filter {
            when (state.filter) {
                TaskFilter.ALL -> !it.isDone
                TaskFilter.TODAY -> !it.isDone && it.dueDate == today
                TaskFilter.OVERDUE -> !it.isDone && it.dueDate.isBefore(today)
                TaskFilter.DONE -> it.isDone
            }
        }
        .sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.postponeCount })
        .map { task ->
            TaskRowModel(
                id = task.id,
                title = task.title,
                notes = task.notes,
                category = task.category,
                dueText = task.dueDate.format(dueFormatter),
                isOverdue = !task.isDone && task.dueDate.isBefore(today),
                isDone = task.isDone,
                postponeCount = task.postponeCount,
                tags = buildList {
                    add(task.category.label)
                    if (task.postponeCount >= 15) add("Chronic")
                },
                task = task,
            )
        }
        .groupBy { it.category }

    val headerElevation = (listState.firstVisibleItemScrollOffset / 12f).coerceAtMost(8f).dp
    val showScrollToTop = listState.firstVisibleItemIndex > 6

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .shadow(headerElevation)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                TopAppBar(
                    title = { Text("Later", fontWeight = FontWeight.Bold) },
                    actions = {
                        if (state.isSyncing) {
                            CircularProgressIndicator(Modifier.size(24.dp).padding(2.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = viewModel::sync) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        }
                    },
                )
                FilterRow(selected = state.filter, onSelect = viewModel::setFilter)
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showScrollToTop) {
                    SmallFloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }
                    Spacer(Modifier.size(12.dp))
                }
                FloatingActionButton(onClick = onAddTask) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visible.forEach { (category, rows) ->
                    item {
                        Text(
                            text = "${category.label} · ${rows.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(rows) { row ->
                        SwipeToCompleteRow(
                            row = row,
                            onClick = { onTaskClick(row.id) },
                            onPostpone = { viewModel.postpone(row.id) },
                            onComplete = { viewModel.complete(row.task) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: TaskFilter, onSelect: (TaskFilter) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FilterIcon(Icons.AutoMirrored.Filled.List, TaskFilter.ALL, selected, onSelect)
        FilterIcon(Icons.Default.DateRange, TaskFilter.TODAY, selected, onSelect)
        FilterIcon(Icons.Default.Warning, TaskFilter.OVERDUE, selected, onSelect)
        FilterIcon(Icons.Default.CheckCircle, TaskFilter.DONE, selected, onSelect)
    }
}

@Composable
private fun FilterIcon(
    icon: ImageVector,
    filter: TaskFilter,
    selected: TaskFilter,
    onSelect: (TaskFilter) -> Unit,
) {
    val isSelected = filter == selected
    IconToggleButton(checked = isSelected, onCheckedChange = { onSelect(filter) }) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(8.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Fog,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToCompleteRow(
    row: TaskRowModel,
    onClick: () -> Unit,
    onPostpone: () -> Unit,
    onComplete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onComplete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        enableDismissFromStartToEnd = !row.isDone,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("Done!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
    ) {
        TaskRow(row, onClick, onPostpone)
    }
}

@Composable
private fun TaskRow(row: TaskRowModel, onClick: () -> Unit, onPostpone: () -> Unit) {
    val titleSize = with(LocalDensity.current) { 16.dp.toSp() }
    val bodySize = with(LocalDensity.current) { 13.dp.toSp() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(row.category.cover),
                contentDescription = "Category image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.title,
                    fontSize = titleSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (row.isDone) TextDecoration.LineThrough else null,
                )
                if (row.notes.isNotEmpty()) {
                    Text(
                        text = row.notes,
                        fontSize = bodySize,
                        color = Fog,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = row.dueText,
                        fontSize = bodySize,
                        color = if (row.isOverdue) Overdue else MaterialTheme.colorScheme.onSurface,
                    )
                    row.tags.drop(1).forEach { tag ->
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = tag,
                            fontSize = bodySize,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            PostponeCounter(count = row.postponeCount, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_later),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onPostpone),
            )
        }
    }
}

@Composable
private fun PostponeCounter(count: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .size(width = 36.dp, height = 24.dp)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${count}×",
                fontSize = with(LocalDensity.current) { 12.dp.toSp() },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
