package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    // Use a fake dataSource to be injected into the viewmodel
    private lateinit var localDataSource: FakeAndroidDataSource

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        localDataSource = FakeAndroidDataSource()
        remindersListViewModel = RemindersListViewModel(getApplicationContext(), localDataSource)
        ServiceLocator.remindersDataSource = localDataSource
    }


    @Test
    fun clickAddReminderButton_navigateToAddReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the add screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun noData_DisplayedInUi() = runBlockingTest{
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - No data are displayed on the screen
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun reminders_displayedInUiAndNotNoData() = runBlockingTest{
        // GIVEN - On the home screen add a reminder
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 2.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 3.0, 4.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 5.0, 6.0)
        localDataSource.addReminders(reminder1, reminder2, reminder3)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - No data are NOT displayed on the screen, reminders are
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(IsNot(ViewMatchers.isDisplayed())))
        for( i in 1..3) {
            onView(withIndex(withId(R.id.title),i-1))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(withIndex(withId(R.id.description),i-1))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(withIndex(withId(R.id.location), i-1))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(withIndex(withId(R.id.title), i-1))
                .check(ViewAssertions.matches(ViewMatchers.withText("Title$i")))
            onView(withIndex(withId(R.id.description), i-1))
                .check(ViewAssertions.matches(ViewMatchers.withText("Description$i")))
            onView(withIndex(withId(R.id.location), i-1))
                .check(ViewAssertions.matches(ViewMatchers.withText( "Location$i")))
        }
    }

    @Test
    fun snackBar_DisplayedInUi() = runBlockingTest{
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN there is an error
        localDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        // THEN - The error snackbar is displayed on the screen
        onView(withText("Reminder not found!"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}

//https://stackoverflow.com/questions/29378552/in-espresso-how-to-avoid-ambiguousviewmatcherexception-when-multiple-views-matc
fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View?): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}