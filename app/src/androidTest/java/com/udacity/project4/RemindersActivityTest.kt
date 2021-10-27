package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    ServiceLocator.provideReminderDataSource(appContext)
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    ServiceLocator.provideReminderDataSource(appContext)
                )
            }
            single { ServiceLocator.provideReminderDataSource(appContext) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /** login in firebase */
    @Before
    fun firebaseLogin() {
        FirebaseAuth.getInstance().signInAnonymously()
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun createOneReminder() {

        // start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //wait login
        sleep(2000)
        // Add reminder
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"), ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"))
        Espresso.closeSoftKeyboard()

        //add location on maps fragment
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.map)).perform(ViewActions.longClick())
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveButton)).perform(ViewActions.click())

        //save reminder
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        //verify reminder is saved and displayed
        Espresso.onView(withId(R.id.title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.location))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Title")))
        Espresso.onView(withId(R.id.description))
            .check(ViewAssertions.matches(ViewMatchers.withText("Description")))
        Espresso.onView(withId(R.id.location))
            .check(ViewAssertions.matches(ViewMatchers.withText( R.string.dropped_pin)))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun createOneReminderErrors() {

        // start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //wait login
        sleep(2000)

        // Add reminder
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        //save reminder
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        //check first error: title
        Espresso.onView(ViewMatchers.withText(R.string.err_enter_title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //insert title and description
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"), ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"))
        Espresso.closeSoftKeyboard()
        //save reminder
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        //check second error: location
        Espresso.onView(ViewMatchers.withText(R.string.err_select_location))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }
}
