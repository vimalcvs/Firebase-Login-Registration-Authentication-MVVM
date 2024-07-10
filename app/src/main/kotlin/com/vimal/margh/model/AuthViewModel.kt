package com.vimal.margh.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vimal.margh.R
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val userRef = FirebaseDatabase.getInstance().getReference("users")
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userData = MutableLiveData<ModelUser?>()
    val userData: LiveData<ModelUser?> = _userData

    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    private val _resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken>()
    val resendToken: LiveData<PhoneAuthProvider.ForceResendingToken> = _resendToken

    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> = _authResult

    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        _firebaseUser.value = auth.currentUser
        loadUserDataFromPreferences()
        configureGoogleSignIn(application)
    }

    // Google Sign-In methods
    private fun configureGoogleSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleGoogleSignInResult(data: Intent) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            _error.postValue(e.message)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) return
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                val modelUser = ModelUser(
                    uid = firebaseUser?.uid ?: "",
                    email = account.email ?: "",
                    name = account.displayName ?: "",
                    institute = ""
                )
                handleSuccessfulLogin(modelUser)
            } else {
                _error.postValue(task.exception?.message)
            }
        }
    }

    private fun handleSuccessfulLogin(modelUser: ModelUser) {
        auth.currentUser?.uid?.let { uid ->
            getData(uid)
            _firebaseUser.postValue(auth.currentUser)
            saveData(modelUser)
        }
    }

    // Email/Password authentication methods
    fun login(email: String, password: String) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                handleSuccessfulLogin()
            } else {
                _error.postValue(task.exception?.message)
            }
        }
    }

    private fun handleSuccessfulLogin() {
        auth.currentUser?.uid?.let { uid ->
            getData(uid)
            _firebaseUser.postValue(auth.currentUser)
        }
    }

    fun register(modelUser: ModelUser, password: String) {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(modelUser.email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    handleSuccessfulRegistration(modelUser)
                } else {
                    _error.postValue(task.exception?.message ?: "Something went wrong...")
                }
            }
    }

    private fun handleSuccessfulRegistration(modelUser: ModelUser) {
        auth.currentUser?.uid?.let { uid ->
            val newUser = modelUser.copy(uid = uid)
            _firebaseUser.postValue(auth.currentUser)
            saveData(newUser)
        }
    }

    // Phone authentication methods
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _isLoading.value = true
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendCode(
        phoneNumber: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        _isLoading.value = true
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String, storedVerificationId: String) {
        _isLoading.value = true
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(storedVerificationId, code))
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _isLoading.value = false
            _error.value = e.message
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _isLoading.value = false
            _verificationId.value = verificationId
            _resendToken.value = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                _authResult.value = true
                _firebaseUser.value = auth.currentUser
                val firebaseUser = auth.currentUser
                val modelUser = ModelUser(
                    uid = firebaseUser?.uid ?: "",
                    email = firebaseUser?.email ?: "",
                    name = "",
                    institute = ""
                )
                handleSuccessfulLogin(modelUser)
            } else {
                _authResult.value = false
                _error.value = task.exception?.message
            }
        }
    }

    // Data persistence methods
    fun getData(uid: String) {
        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(ModelUser::class.java)
                userData?.let {
                    _userData.postValue(it)
                    saveUserDataToPreferences(it)
                } ?: run {
                    _error.postValue("User data not found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                _error.postValue(databaseError.message)
            }
        })
    }

    private fun saveData(modelUser: ModelUser) {
        userRef.child(modelUser.uid).setValue(modelUser).addOnSuccessListener {
            _userData.postValue(modelUser)
            saveUserDataToPreferences(modelUser)
        }.addOnFailureListener { e ->
            _error.postValue(e.message)
        }
    }

    // SharedPreferences methods
    private fun saveUserDataToPreferences(user: ModelUser) {
        with(sharedPreferences.edit()) {
            putString("uid", user.uid)
            putString("email", user.email)
            putString("name", user.name)
            putString("institute", user.institute)
            apply()
        }
    }

    private fun loadUserDataFromPreferences() {
        val uid = sharedPreferences.getString("uid", null)
        val email = sharedPreferences.getString("email", null)
        val name = sharedPreferences.getString("name", null)
        val institute = sharedPreferences.getString("institute", null)
        if (uid != null && email != null && name != null && institute != null) {
            val user = ModelUser(uid, email, name, institute)
            _userData.postValue(user)
        }
    }

    private fun clearUserDataFromPreferences() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    // Logout method
    fun logout() {
        auth.signOut()
        googleSignInClient.signOut()
        _firebaseUser.postValue(null)
        clearUserDataFromPreferences()
    }

    // Password reset method
    fun resetPassword(email: String) {
        _isLoading.value = true
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                _error.postValue("Password reset email sent.")
            } else {
                _error.postValue(task.exception?.message ?: "Failed to send reset email.")
            }
        }
    }
}

