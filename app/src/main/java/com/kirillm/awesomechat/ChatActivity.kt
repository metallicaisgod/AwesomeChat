package com.kirillm.awesomechat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ChatActivity : AppCompatActivity() {

    var RC_IMAGE_PICKER = 123

    lateinit var messageListView: ListView
    lateinit var adapter: AwesomeMessageAdapter
    lateinit var progressBar: ProgressBar
    lateinit var imageButton: ImageButton
    lateinit var sendButton: Button
    lateinit var messageEditText: EditText

    lateinit var userName: String
    lateinit var recipientUserId: String
    lateinit var recipientUserName: String

    lateinit var database: FirebaseDatabase
    lateinit var messagesDatabaseReference: DatabaseReference
    lateinit var messagesChildEventListener: ChildEventListener
//    lateinit var usersDatabaseReference: DatabaseReference
//    lateinit var usersChildEventListener: ChildEventListener

    lateinit var storage: FirebaseStorage
    lateinit var imagesStorageReference: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        database =
            Firebase.database("https://awesomechat-dc627-default-rtdb.europe-west1.firebasedatabase.app")
        messagesDatabaseReference = database.reference.child("messages")
      // usersDatabaseReference = database.reference.child("users")

        storage = Firebase.storage
        imagesStorageReference = storage.reference.child("images")

        val bundle: Bundle? = intent.extras
        userName = "Default User"
        bundle?.let {
            bundle.apply {
                userName = getString("userName").toString()
                recipientUserId = getString("recipientUserId").toString()
                recipientUserName = getString("recipientUserName").toString()
            }
        }

        title = "Chat with $recipientUserName"

        messageListView = findViewById(R.id.messageListView)
        progressBar = findViewById(R.id.progressBar)
        imageButton = findViewById(R.id.insertImage)
        sendButton = findViewById(R.id.sendMessageButton)
        messageEditText = findViewById(R.id.messageEditText)

        val awesomeMessages = mutableListOf<AwesomeMessage>()
        adapter = AwesomeMessageAdapter(this, 0, awesomeMessages)
        messageListView.adapter = adapter


        progressBar.visibility = ProgressBar.INVISIBLE

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        messageEditText.filters = arrayOf(InputFilter.LengthFilter(500))

        sendButton.setOnClickListener {
            val message = AwesomeMessage(
                messageEditText.text.toString(),
                userName,
                "",
                Firebase.auth.currentUser?.uid.toString(),
                recipientUserId
            )

            messagesDatabaseReference.push().setValue(message)

            messageEditText.setText("")
            //sendButton.isEnabled = false
        }

        val getImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val selectedImageUri = data.data
                        if (selectedImageUri != null) {
                            val imageReference = imagesStorageReference
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
                                    val message = AwesomeMessage()
                                    message.imageUrl = downloadUri.toString()
                                    message.name = userName
                                    message.sender = Firebase.auth.currentUser!!.uid
                                    message.recipient = recipientUserId
                                    messagesDatabaseReference.push().setValue(message)
                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }
                        }
                    }
                }
            }

        imageButton.setOnClickListener {
            val intentGetContent = Intent(Intent.ACTION_GET_CONTENT)
            intentGetContent.type = "image/*"
            intentGetContent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intentGetContent.putExtra("request_code", RC_IMAGE_PICKER)
            getImageLauncher.launch(Intent.createChooser(intentGetContent, "Choose an image"))
        }

        messagesChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(AwesomeMessage::class.java)
                if (message != null) {
                    if (message.sender == Firebase.auth.currentUser!!.uid
                        && message.recipient == recipientUserId
                    ) {
                        message.isMine = true
                        adapter.add(message)
                    } else if (message.sender == recipientUserId
                                && message.recipient == Firebase.auth.currentUser!!.uid
                    ) {
                        message.isMine = false
                        adapter.add(message)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }
        messagesDatabaseReference.addChildEventListener(messagesChildEventListener)

//        usersChildEventListener = object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val user = snapshot.getValue(User::class.java)
//                if (user != null)
//                    if (user.id == Firebase.auth.currentUser!!.uid) {
//                        userName = user.name
//                    }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        }
//        usersDatabaseReference.addChildEventListener(usersChildEventListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                Firebase.auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}