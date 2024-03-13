package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityPassword : AppCompatActivity() {
    private var binding: ActivityPasswordBinding? = null
    private var auth: FirebaseAuth? = null
    private var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.getRoot())
        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().getReference()
    }


}
