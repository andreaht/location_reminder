package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
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
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @ExperimentalCoroutinesApi
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
    private lateinit var remindersListViewModel: RemindersListViewModel

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

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), dataSource
        )

    }


    @Test
    fun loadReminders_showNoDataNotVisible() {

        // Given Datasource contains 3 items

        //When
        remindersListViewModel.loadReminders()

        // Then the "showNoData" action is hidden
        MatcherAssert.assertThat(
            remindersListViewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_showNoDataVisibleAndSnackbar() {

        //Given datasource empty
        dataSource.removeReminders()

        //When
        remindersListViewModel.loadReminders()

        // Then the "showNoData" action is visible
        MatcherAssert.assertThat(
            remindersListViewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        // The snackbar is updated
        val snackbarText = remindersListViewModel.showSnackBar.getOrAwaitValue()
        MatcherAssert.assertThat(
            snackbarText, CoreMatchers.`is`("Reminder not found!")
        )
    }

}