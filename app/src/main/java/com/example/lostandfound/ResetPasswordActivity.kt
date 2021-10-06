package com.example.lostandfound

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// This code is for password reset
class ResetPasswordActivity : AppCompatActivity() {

    lateinit var resetPassword: Button
    private lateinit var email: EditText

    // Codes from Firebase Docs: https://firebase.google.com/docs/auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)

        resetPassword = findViewById(R.id.pw_reset_button)
        email = findViewById(R.id.pw_reset_email)

        // Reset Password Button
        resetPassword.setOnClickListener {
            val userEmail = email.text.toString()
            Log.i(LoginActivity.TAG, "Sent Password Reset Email")
            Firebase.auth.sendPasswordResetEmail(userEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) { // On Success
                        Toast.makeText(this, "Password Reset Email Sent!\nPlease Check Your Email!", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "Password Reset Sent")
                    }
                    else { // On Fail
                        Toast.makeText(this, "Email Not Sent!\nPlease Check The Entered Email!", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "Password Reset Could Not Send")
                    }
                }
        }
    }

    companion object {
        const val TAG = "Lost&Found"
    }
}