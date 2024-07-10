package com.vimal.margh

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vimal.margh.databinding.ActivitySplashBinding
import com.vimal.margh.model.AuthViewModel


class ActivitySplash : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.firebaseUser.observe(this) { user ->
            if (user != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToActivity(ActivityProfile::class.java)
                }, 500)
            } else {
                navigateToActivity(ActivityLogin::class.java)
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
}

