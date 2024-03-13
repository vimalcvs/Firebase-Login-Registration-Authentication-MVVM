package com.example.myapplication

import android.app.Application
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {

    private var auth: FirebaseAuth? = null

    override fun onCreate() {
        super.onCreate()

        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser == null) {
            startActivity(Intent(this, ActivityLogin::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
