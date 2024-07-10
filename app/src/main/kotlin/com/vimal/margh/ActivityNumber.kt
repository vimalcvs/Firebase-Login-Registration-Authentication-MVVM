package com.vimal.margh


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vimal.margh.databinding.ActivityNumberBinding
import com.vimal.margh.model.AuthViewModel

class ActivityNumber : AppCompatActivity() {

    private lateinit var binding: ActivityNumberBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)


        authViewModel.userData.observe(this) { userData ->
            if (userData != null) {
                startActivity(Intent(this, ActivityProfile::class.java))
                finish()
            } else {
                startActivity(Intent(this, ActivityEditProfile::class.java))
                finish()
            }
        }

        initViews()
        setupObservers()
    }

    private fun initViews() {
        binding.progressbar.visibility = View.GONE

        binding.btnSignIn.setOnClickListener {
            val phoneNumber =
                binding.etCountryCode.text.toString() + binding.etNumber.text.toString()
            authViewModel.sendVerificationCode(phoneNumber, this)
        }

        binding.btnSubmit.setOnClickListener {
            val code = binding.etSmsCode.text.toString()
            if (code.length == 6) {
                authViewModel.verificationId.value?.let { verificationId ->
                    authViewModel.verifyCode(code, verificationId)
                }
            }
        }

        binding.tvResend.setOnClickListener {
            val phoneNumber =
                binding.etCountryCode.text.toString() + binding.etNumber.text.toString()
            authViewModel.resendToken.value?.let { token ->
                authViewModel.resendCode(phoneNumber, this, token)
            }
        }
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        authViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.verificationId.observe(this) { verificationId ->
            binding.llSignIn.visibility = View.GONE
            binding.llSmsCode.visibility = View.VISIBLE
        }

        authViewModel.authResult.observe(this) { isSuccess ->
            if (isSuccess) {
                startActivity(Intent(this, ActivityProfile::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
