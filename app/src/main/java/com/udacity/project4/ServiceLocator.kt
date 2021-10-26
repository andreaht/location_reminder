package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


/**
 * A Service Locator for the [RemindersLocalRepository]. This is the prod version, with a
 * the "real" [ReminderDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null
    @Volatile
    var RemindersDatabase: RemindersDatabase? = null
        @VisibleForTesting set

    fun provideRemindersRepository(context: Context): RemindersLocalRepository {
        synchronized(this) {
            return RemindersDatabase ?: createRemindersDatabase(context)
        }
    }

    private fun createRemindersDatabase(context: Context): RemindersDatabase {
        val newRepo = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        RemindersDatabase = newRepo
        return newRepo
    }

    private fun createTaskLocalDataSource(context: Context): TasksDataSource {
        val database = database ?: createDataBase(context)
        return TasksLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): ToDoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                TasksRemoteDataSource.deleteAllTasks()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            RemindersDatabase = null
        }
    }
}