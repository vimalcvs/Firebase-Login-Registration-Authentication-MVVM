package com.vimal.margh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vimal.margh.databinding.ActivityProfileBinding
import com.vimal.margh.model.AuthViewModel
import com.vimal.margh.model.ModelUser

class ActivityProfile : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(this, ActivityLogin::class.java))
         //   finish()
        }
    }

    private fun observeViewModel() {
        authViewModel.firebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser == null) {
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            } else {
                authViewModel.getData(firebaseUser.uid)
            }
        }
        authViewModel.userData.observe(this) { modelUser ->
            modelUser?.let {
                showUserData(it)
            }
        }
        authViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                Log.e("ActivityProfile", "Error: $it")
            }
        }
    }

    private fun showUserData(model: ModelUser) {
        binding.profileName.text = model.name
        binding.profileEmail.text = model.email
        binding.profileInstitute.text = model.institute
    }
}
