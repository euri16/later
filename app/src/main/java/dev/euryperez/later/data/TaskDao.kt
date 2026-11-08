package dev.euryperez.later.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): Task?

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Insert
    suspend fun insert(task: Task): Long

    @Insert
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert
    suspend fun insertPostponement(postponement: Postponement)

    @Query("SELECT * FROM postponements WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun observePostponements(taskId: Long): Flow<List<Postponement>>

    @Query("SELECT * FROM postponements WHERE timestamp >= :since")
    fun observePostponementsSince(since: Long): Flow<List<Postponement>>
}
