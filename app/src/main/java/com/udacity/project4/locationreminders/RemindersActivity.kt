package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        const val TAG = "RemindersActivity"
        const val SIGN_IN_REQUEST_CODE = 1001
        const val SIGN_OUT_REQUEST_CODE = 1002
    }

    private lateinit var binding: ActivityRemindersBinding
    private val viewModel by viewModels<RemindersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthenticationState()

        //launch login activity
        val intent = Intent(this,AuthenticationActivity::class.java)
            .putExtra("requestCode", SIGN_IN_REQUEST_CODE)
        startActivityForResult(intent, SIGN_IN_REQUEST_CODE)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val navHostFragment = findNavController(R.id.nav_host_fragment)
                navHostFragment.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                RemindersViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG, "Sign in successful")
                }
                else -> {
                    Log.i(TAG, "Sign out successful")
                }
            }
        })
    }
}
