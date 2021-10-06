package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

// The Login Page
class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var createButton: Button
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var resetPassword: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    // Codes from Firebase Docs: https://firebase.google.com/docs/auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.login_button)
        createButton = findViewById(R.id.new_account)
        emailText = findViewById(R.id.email)
        passwordText = findViewById(R.id.password)
        resetPassword = findViewById(R.id.reset_password)

        auth = FirebaseAuth.getInstance()

        val scrollingIntent = Intent(
            this@LoginActivity,
            ScrollingActivity::class.java
        )

        // Login Button
        loginButton.setOnClickListener {
            if (emailText != null && passwordText != null &&
                    !emailText.text.isBlank() && !passwordText.text.isBlank()) { // Null Check
                val email = emailText.text.toString()
                val password = passwordText.text.toString()

                Log.i(TAG, "email: " + email)
                Log.i(TAG, "password: " + password)

                auth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {// Login Success

                        // Check email verification status
                        auth.currentUser?.let { firebaseUser ->
                            // Email Verified
                            if (firebaseUser.isEmailVerified) {

                                uid = firebaseUser.uid

                                Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show()
                                Log.i(TAG, "Login Success")

                                // attaches UID to intent
                                scrollingIntent.putExtra("uid", uid)
                                // Go to next page after login
                                startActivity(scrollingIntent)
                                Log.i(TAG, "Email Verified")

                            } else {
                                // User still needs to verify email
                                Toast.makeText(
                                    this,
                                    "Check Your Email And Verify",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.i(TAG, "Email Verification Needed")
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Login Failed
                        Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "Login Failed")
                    }
            }
            else { // Input Invalid
                Toast.makeText(this, "Please Enter Valid Inputs", Toast.LENGTH_LONG).show()
            }
        }

        // Create Account Button
        createButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, AccountCreateActivity::class.java)
            startActivity(intent)
        }

        // Reset Password Button
        resetPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Set Password Text to blank upon resume
        passwordText = findViewById(R.id.password)
        passwordText.setText("")
        Log.i(TAG, "password text set blank")
    }

    companion object {
        const val TAG = "Lost&Found"
    }
}