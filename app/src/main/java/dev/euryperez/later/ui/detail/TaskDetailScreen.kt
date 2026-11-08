package dev.euryperez.later.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.euryperez.later.R
import dev.euryperez.later.ui.theme.Fog
import dev.euryperez.later.ui.theme.Overdue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val task = state.task

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(onClick = { onEdit(task.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.delete(onBack) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading || task == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val isOverdue = !task.isDone && task.dueDate.isBefore(LocalDate.now())
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Image(
                    painter = painterResource(task.category.cover),
                    contentDescription = "Category image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(task.category.label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Due ${task.dueDate.format(dateFormatter)}",
                        color = if (isOverdue) Overdue else MaterialTheme.colorScheme.onSurface,
                    )
                    if (task.notes.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(task.notes, color = Fog)
                    }
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = viewModel::toggleDone, modifier = Modifier.weight(1f)) {
                        Text(if (task.isDone) "Not done" else "Complete")
                    }
                    OutlinedButton(onClick = viewModel::postpone, modifier = Modifier.weight(1f)) {
                        Icon(painterResource(R.drawable.ic_later), contentDescription = null)
                        Text("  Later")
                    }
                }
            }
            item {
                Text(
                    text = "Postponed ${task.postponeCount} times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp),
                )
            }
            if (state.postponements.isEmpty()) {
                item {
                    Text(
                        "No postponements yet. Suspicious.",
                        color = Fog,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
            items(state.postponements) { entry ->
                Text(
                    text = "${entry.fromDate.format(dateFormatter)} → ${entry.toDate.format(dateFormatter)}",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}
