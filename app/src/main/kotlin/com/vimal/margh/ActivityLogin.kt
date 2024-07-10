package com.vimal.margh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vimal.margh.databinding.ActivityLoginBinding
import com.vimal.margh.model.AuthViewModel

class ActivityLogin : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    private var googleSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                authViewModel.handleGoogleSignInResult(result.data!!)
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.firebaseUser.observe(this) { user ->
            user?.let {
                startActivity(Intent(this, ActivityProfile::class.java))
                finish()
            }
        }

        authViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.loadingRootConstraintLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.logInButton.isEnabled = !isLoading
        }

        binding.singUp.setOnClickListener {
            startActivity(Intent(this, ActivitySignup::class.java))
        }

        binding.logInButton.setOnClickListener { signIn() }

        binding.googleLoginButton.setOnClickListener {
            googleSignInLauncher.launch(authViewModel.getGoogleSignInIntent())
        }
    }


    private fun signIn() {
        with(binding) {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                emailTextInputLayout.error = "Please enter your email"
                return
            }
            if (password.isEmpty()) {
                passwordTextInputLayout.error = "Please enter your password"
                return
            }

            loadingLayout.loadingRootConstraintLayout.visibility = View.VISIBLE
            authViewModel.login(email, password)
        }
    }
}