package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityLogin : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    private var auth: FirebaseAuth? = null
    private var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().getReference()

        if (auth!!.currentUser != null) {
            startActivity(Intent(this, ActivityProfile::class.java))
            finish()
        }

        binding!!.logInButton.setOnClickListener { signIn() }


        binding!!.forgotPassword.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivityPassword::class.java
                )
            )
        }
        binding!!.signUpLayout.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivitySignup::class.java
                )
            )
        }

        binding!!.googleLoginButton.setOnClickListener {

        }
    }

    private fun signIn() {
        val email = binding!!.editTextEmail.getText().toString().trim { it <= ' ' }
        val password = binding!!.editTextPassword.getText().toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(email)) {
            binding!!.emailTextInputLayout.error = "Please enter your email"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding!!.passwordTextInputLayout.error = "Please enter your password"
            return
        }
        binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.VISIBLE
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.GONE
                if (task.isSuccessful) {
                    val intent = Intent(this, ActivityProfile::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (task.exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Account not found.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
