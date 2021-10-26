package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.RunWith
import kotlin.random.Random

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder = ReminderDTO(
            "title", "description", "location",
            Random.nextDouble(-90.0, 90.0), Random.nextDouble(-180.0, 180.0)
        )
        localRepository.saveReminder(reminder)

        // WHEN  - reminder retrieved by ID
        val result = localRepository.getReminder(reminder.id)

        // THEN - Same reminder is returned
        Assert.assertThat(result.succeeded, `is`(true))
        result as Result.Success
        MatcherAssert.assertThat(result.data.id, `is`(reminder.id))
        MatcherAssert.assertThat(result.data.title, `is`(reminder.title))
        MatcherAssert.assertThat(result.data.description, `is`(reminder.description))
        MatcherAssert.assertThat(result.data.location, `is`(reminder.location))
        MatcherAssert.assertThat(result.data.latitude, `is`(reminder.latitude))
        MatcherAssert.assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminders_retrievesRemindersAndDeleteReminders() = runBlocking {
        // GIVEN - 100 reminders saved in the database
        val reminders = mutableListOf<ReminderDTO>()
        for (i in 1..100) {
            val reminder = ReminderDTO(
                "title $i", "description $i", "location $i",
                Random.nextDouble(-90.0, 90.0), Random.nextDouble(-180.0, 180.0)
            )
            reminders.add(reminder)
            localRepository.saveReminder(reminder)
        }
        // WHEN  - reminder retrieved by ID
        val result = localRepository.getReminders()

        // THEN - Same reminder is returned
        Assert.assertThat(result.succeeded, `is`(true))
        result as Result.Success
        for (i in 1..100) {
            val resultData = result.data.filter { it.title == "title $i" }
            val listData = reminders.filter { it.title == "title $i" }
            MatcherAssert.assertThat(resultData.first().id, `is`(listData.first().id))
            MatcherAssert.assertThat(resultData.first().title, `is`(listData.first().title))
            MatcherAssert.assertThat(resultData.first().description, `is`(listData.first().description))
            MatcherAssert.assertThat(resultData.first().location, `is`(listData.first().location))
            MatcherAssert.assertThat(resultData.first().latitude, `is`(listData.first().latitude))
            MatcherAssert.assertThat(resultData.first().longitude, `is`(listData.first().longitude))
        }

        // WHEN  - delete all and retrieve reminders
        localRepository.deleteAllReminders()
        val result2 = localRepository.getReminders()
        Assert.assertThat(result2.succeeded, `is`(true))
        result2 as Result.Success
        MatcherAssert.assertThat(result2.data, `is`(emptyList()))
    }

}