package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidDataSource : ReminderDataSource {

    private var remindersList = mutableListOf<ReminderDTO>()

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (remindersList.isNotEmpty() && !shouldReturnError) {
            Result.Success(remindersList)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remindersList.filter { it.id == id }
        return if (reminder.isNotEmpty() && !shouldReturnError) {
            Result.Success(reminder.first())
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            remindersList.add(reminder)
        }
    }

    fun removeReminders() {
        remindersList.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

}