package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivitySignup : AppCompatActivity() {

    private var binding: ActivitySignupBinding? = null
    private var auth: FirebaseAuth? = null
    private var reference: DatabaseReference? = null
    private var isPolicyChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().getReference()

        if (auth!!.currentUser != null) {
            startActivity(Intent(this, ActivityProfile::class.java))
            finish()
        }

        binding!!.buttonSignup.setOnClickListener { signUp() }

        binding!!.signInLayout.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
            finish()
        }

        binding!!.policyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isPolicyChecked = isChecked
            binding!!.buttonSignup.isEnabled = isChecked
        }
    }

    private fun signUp() {


        val name = binding!!.editTextName.text.toString().trim()
        val email = binding!!.editTextEmail.text.toString().trim()
        val institute = binding!!.editTextInstitute.text.toString().trim()
        val password = binding!!.editTextPassword.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            binding!!.nameTextInputLayout.error = "Please enter your name"
            return
        }
        if (TextUtils.isEmpty(email)) {
            binding!!.emailTextInputLayout.error = "Please enter your email"
            return
        }
        if (TextUtils.isEmpty(institute)) {
            binding!!.instituteTextInputLayout.error = "Please enter your institute / organisation"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding!!.passwordTextInputLayout.error = "Please enter your password"
            return
        }

        if (!isPolicyChecked) {
            Toast.makeText(this, "Please accept the policy", Toast.LENGTH_SHORT).show()
            return
        }

        binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.VISIBLE
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth!!.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(email)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { task1: Task<Void?> ->
                                if (task1.isSuccessful) {
                                    Log.i("TAG", "User profile updated.")
                                }
                            }
                        val userData = User(email, name, institute, password)
                        reference!!.child("users").child(user.uid).setValue(userData)
                    }
                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ActivityLogin::class.java)
                    intent.putExtra("email", user)
                    intent.putExtra("name", name)
                    intent.putExtra("institute", institute)
                    startActivity(intent)
                    finish()
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "This email already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Please check your internet and try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
