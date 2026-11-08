package dev.euryperez.later.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    val category: Category,
    val dueDate: LocalDate,
    val postponeCount: Int = 0,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "postponements",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class Postponement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val timestamp: Long = System.currentTimeMillis(),
)
