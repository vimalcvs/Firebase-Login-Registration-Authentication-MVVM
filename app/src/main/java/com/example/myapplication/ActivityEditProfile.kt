package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityEditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var binding: ActivityEditProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        showAllUserData()

        binding!!.buttonSignup.setOnClickListener {
            val name = binding!!.editTextName.text.toString()
            val institute = binding!!.editTextInstitute.text.toString()
            updateUserProfile(name, institute)
        }
    }

    private fun updateUserProfile(name: String, institute: String) {
        val user = auth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val userReference = database.child(userId)

            binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.VISIBLE

            userReference.child("name").setValue(name)
            userReference.child("institute").setValue(institute)

            binding!!.loadingLayout.loadingRootConstraintLayout.visibility = View.GONE
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            val intents = Intent(this, ActivityProfile::class.java)
            intents.putExtra("name", name)
            intents.putExtra("institute", institute)
            startActivity(intents)

            finish()
        }
    }

    private fun showAllUserData() {
        val intent = intent
        val name = intent.getStringExtra("name")
        val institute = intent.getStringExtra("institute")
        binding!!.editTextName.setText(name)
        binding!!.editTextInstitute.setText(institute)
    }
}
