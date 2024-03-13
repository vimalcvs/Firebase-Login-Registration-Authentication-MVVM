package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityProfile : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var binding: ActivityProfileBinding? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        if (sharedPreferences.contains("name")) {
            val name = sharedPreferences.getString("name", "")
            val email = sharedPreferences.getString("email", "")
            val institute = sharedPreferences.getString("institute", "")
            binding!!.profileName.text = name
            binding!!.profileEmail.text = email
            binding!!.profileInstitute.text = institute

        } else {
            showAllUserData()
        }

        binding!!.logoutButton.setOnClickListener {
            auth!!.signOut()
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(this, ActivityLogin::class.java))
            finish()
        }

        showLoadAllUserData()
    }

    private fun showAllUserData() {
        val intent = intent
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val institute = intent.getStringExtra("institute")
        binding!!.profileName.text = name
        binding!!.profileEmail.text = email
        binding!!.profileInstitute.text = institute

        with(sharedPreferences.edit()) {
            putString("name", name)
            putString("email", email)
            putString("institute", institute)
            apply()
        }
    }

    private fun showLoadAllUserData() {

        binding!!.editButton.setOnClickListener {
            val intents = Intent(this, ActivityEditProfile::class.java)
            intents.putExtra("name", binding!!.profileName.text.toString())
            intents.putExtra("institute", binding!!.profileInstitute.text.toString())
            startActivity(intents)
            finish()
        }

        val currentUser = auth!!.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        val institute = snapshot.child("institute").getValue(String::class.java)
                        binding!!.profileName.text = name
                        binding!!.profileEmail.text = email
                        binding!!.profileInstitute.text = institute

                        with(sharedPreferences.edit()) {
                            putString("name", name)
                            putString("email", email)
                            putString("institute", institute)
                            apply()
                        }
                    } else {
                        Log.d("ProfileActivity", "User data not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ProfileActivity", "Failed to fetch user data")
                }
            })
        }
    }
}
