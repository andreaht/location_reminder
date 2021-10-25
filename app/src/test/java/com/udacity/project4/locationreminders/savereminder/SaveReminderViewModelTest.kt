package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {@ExperimentalCoroutinesApi
val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    // Use a fake dataSource to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        // We initialise the tasks to 3, with one active and two completed
        dataSource = FakeDataSource()
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 2.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 3.0, 4.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 5.0, 6.0)
        dataSource.addReminders(reminder1, reminder2, reminder3)

        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), dataSource
        )

    }

    @Test
    fun saveReminder_loading() {
        // Pause dispatcher so we can verify initial values
        testDispatcher.pauseDispatcher()

        // Load the task in the viewmodel
        saveReminderViewModel.saveReminder(
            ReminderDataItem("Title1", "Description1", "Location1", 1.0, 2.0))

        // Then progress indicator is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        // Execute pending coroutines actions
        testDispatcher.resumeDispatcher()

        // Then progress indicator is hidden
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun validateEnteredData_invalidTitle() {

        // Load the task in the viewmodel
        saveReminderViewModel.validateEnteredData(
            ReminderDataItem(null, "Description1", "Location1", 1.0, 2.0))

        // The snackbar is updated
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun validateEnteredData_invalidLocation() {

        // Load the task in the viewmodel
        saveReminderViewModel.validateEnteredData(
            ReminderDataItem("Title1", "Description1", "", 1.0, 2.0))

        // The snackbar is updated
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_select_location)
        )
    }

}