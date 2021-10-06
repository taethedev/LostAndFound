package com.example.lostandfound

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// This code is for creating an account
class AccountCreateActivity : AppCompatActivity() {

    lateinit var createButton: Button
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var nameText: EditText
    private lateinit var phoneNumberText: EditText
    private lateinit var databaseUsers: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    // Codes from Firebase Docs: https://firebase.google.com/docs/auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        databaseUsers = FirebaseDatabase.getInstance().getReference("users")
        createButton = findViewById(R.id.create_button)
        emailText = findViewById(R.id.create_email)
        passwordText = findViewById(R.id.create_password)
        nameText = findViewById(R.id.create_name)
        phoneNumberText = findViewById(R.id.create_number)

        auth = FirebaseAuth.getInstance()

        // Create Account Button
        createButton.setOnClickListener {
            if (validInputs()) { // Proceed if inputs are valid
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                val name = nameText.text.toString()
                val number = phoneNumberText.text.toString()
                val userID = email.removeSuffix("@terpmail.umd.edu").removeSuffix("@umd.edu")


                Log.i(TAG, "email: "+ email)
                Log.i(TAG, "password: "+ password)

                auth
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { // Account Created
                        // Send Email Verification
                        val currentUser = auth.currentUser
                        currentUser!!.sendEmailVerification()
                        Log.i("TAG", "Email Verification Sent")

                        uid = currentUser.uid
                        databaseUsers = databaseUsers.child(userID)
                        databaseUsers.child("uid").setValue(uid)
                        databaseUsers.child("email").setValue(email)
                        databaseUsers.child("name").setValue(name)
                        databaseUsers.child("phone_number").setValue(number)

                        Toast.makeText(
                            this,
                            "Account Successfully Created!!\nPlease Verify Your Email",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.i("TAG", "Account Created")
                        finish()
                    }
                    .addOnFailureListener {
                        // Failed
                        Toast.makeText(
                            this,
                            "Could Not Create An Account.\nTry Different Email/Password",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.i("TAG", "Account Create Failed")
                    }
            }
            else { // Invalid Inputs
                Toast.makeText(this, "Please Enter Correct Values", Toast.LENGTH_LONG).show()
                Log.i("TAG", "Invalid Inputs")
            }
        }
    }
    // Check if the inputs are valid
    private fun validInputs(): Boolean {
        // Null Check
        if (nameText == null ||
                phoneNumberText == null ||
                emailText == null ||
                passwordText == null) {
            Log.i(TAG, "invalid input, return false")
            return false
        }
        // Blank Check
        if (nameText.text.isBlank() ||
                phoneNumberText.text.isBlank() ||
                emailText.text.isBlank() ||
                passwordText.text.isBlank()) {
            Log.i(TAG, "invalid input, return false")
            return false
        }
        // Check for @terpmail.umd.edu or @umd.edu
        if (!emailText.text.contains("@terpmail.umd.edu") &&
            !emailText.text.contains("@umd.edu")) {
            return false
        }
        Log.i(TAG, "Valid input, return true")
        return true
    }

    companion object {
        const val TAG = "Lost&Found"
    }
}