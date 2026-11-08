package dev.euryperez.later.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class, Postponement::class], version = 1)
@TypeConverters(Converters::class)
abstract class LaterDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        fun create(context: Context): LaterDatabase =
            Room.databaseBuilder(context, LaterDatabase::class.java, "later.db").build()
    }
}
