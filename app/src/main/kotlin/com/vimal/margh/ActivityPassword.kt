package com.vimal.margh

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vimal.margh.databinding.ActivityPasswordBinding
import com.vimal.margh.model.AuthViewModel

class ActivityPassword : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())


        authViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.textView2.setOnClickListener {
            authViewModel.resetPassword(binding.editTextEmail.getText().toString())
            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
        }
    }
}
