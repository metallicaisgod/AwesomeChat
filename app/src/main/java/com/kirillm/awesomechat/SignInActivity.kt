package com.kirillm.awesomechat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class SignInActivity : AppCompatActivity() {

    private var TAG: String = "SignInActivity"

    private lateinit var auth: FirebaseAuth

    lateinit var signUpButton: Button
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var confirmPasswordEditText: EditText
    lateinit var nameEditText: EditText
    lateinit var toggleTextView: TextView
    lateinit var avatarImageView: ImageView

    private var loginModeActive = false

    lateinit var database: FirebaseDatabase
    lateinit var usersDatabaseReference: DatabaseReference
    lateinit var avatarsStorageReference: StorageReference
    var avatarUri: String = "https://firebasestorage.googleapis.com/v0/b/awesomechat-dc627.appspot.com/o/avatars%2Fno_avatar.jpg?alt=media&token=dce8d43f-36aa-4064-b226-af2d55d24d70"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth
        database = Firebase.database("https://awesomechat-dc627-default-rtdb.europe-west1.firebasedatabase.app")
        usersDatabaseReference = database.reference.child("users")
        avatarsStorageReference = Firebase.storage.reference.child("avatars")

        signUpButton = findViewById(R.id.loginSignUpButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        toggleTextView = findViewById(R.id.toggleLoginSignUpTextView)
        avatarImageView = findViewById(R.id.avatarImageView)

        signUpButton.setOnClickListener {
            loginSignUpUser(
                emailEditText.text.toString().trim(),
                passwordEditText.text.toString().trim()
            )
        }


        val getAvatarLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val selectedImageUri = data.data
                        if (selectedImageUri != null) {
                            val imageReference = avatarsStorageReference
                                .child(selectedImageUri.lastPathSegment.toString())
                            val uploadTask = imageReference.putFile(selectedImageUri)

                            val urlTask = uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                imageReference.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val downloadUri = task.result
                                    avatarUri = downloadUri.toString()
                                    Glide.with(avatarImageView.context)
                                        .load(avatarUri)
                                        .circleCrop()
                                        .into(avatarImageView)
                                   // messagesDatabaseReference.push().setValue(message)
                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }
                        }
                    }
                }
            }

        avatarImageView.setOnClickListener {
            val intentGetContent = Intent(Intent.ACTION_GET_CONTENT)
            intentGetContent.type = "image/*"
            intentGetContent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            getAvatarLauncher.launch(Intent.createChooser(intentGetContent, "Choose an avatar"))
        }

        if (auth.currentUser != null) {
            startActivity(Intent(this, UsersList::class.java))
        }
    }

    private fun loginSignUpUser(email: String, password: String) {
        if (loginModeActive) {
            if (passwordEditText.text.toString().trim().length < 6) {
                Toast.makeText(
                    baseContext, "Password's length must be over 6 symbols",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (emailEditText.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    baseContext, "Email can't be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            startActivity(Intent(this, UsersList::class.java))
                            // updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            //  updateUI(null)
                        }
                    }
                }
            }
            else {
                if (passwordEditText.text.toString().trim() != passwordEditText.text.toString()
                        .trim()
                ) {
                    Toast.makeText(
                        baseContext, "Passwords mismatch",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (passwordEditText.text.toString().trim().length < 6) {
                    Toast.makeText(
                        baseContext, "Password's length must be over 6 symbols",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (emailEditText.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        baseContext, "Email can't be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                createUser(user)
                                val intent = Intent(this, UsersList::class.java)
                                intent.putExtra("userName", nameEditText.text.toString().trim())
                                startActivity(intent)
                                //updateUI(user)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //updateUI(null)
                            }
                        }
                }
            }
        }

    private fun createUser(fireBaseUser: FirebaseUser?) {
        val user = User()
        if (fireBaseUser != null) {
            user.id = fireBaseUser.uid
            user.email = fireBaseUser.email.toString()
            user.name = nameEditText.text.toString().trim()
            user.avatarResource = avatarUri
            usersDatabaseReference.push().setValue(user)
        }


    }

    fun toggleLoginMode(view: View) {
            if (loginModeActive) {
                loginModeActive = false
                signUpButton.text = "Sign Up"
                toggleTextView.text = "Or, log in"
                confirmPasswordEditText.visibility = View.VISIBLE
                nameEditText.visibility = View.VISIBLE
                avatarImageView.visibility = View.VISIBLE
            } else {
                loginModeActive = true
                signUpButton.text = "Log In"
                toggleTextView.text = "Or, sign Up"
                confirmPasswordEditText.visibility = View.GONE
                avatarImageView.visibility = View.GONE
                nameEditText.visibility = View.GONE
            }
        }
    }