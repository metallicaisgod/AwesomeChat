package com.kirillm.awesomechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UsersList : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var usersArrayList: ArrayList<User>

    lateinit var usersDatabaseReference: DatabaseReference
     var usersChildEventListener: ChildEventListener? = null


    lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

//        val bundle: Bundle? = intent.extras
        userName = "Default User"
//        bundle?.let {
//            bundle.apply {
//                userName = getString("userName").toString()
//            }
//        }

        buildRecyclerView()
        usersDatabaseReference =
            Firebase.database("https://awesomechat-dc627-default-rtdb.europe-west1.firebasedatabase.app")
                .reference.child("users")

        attachUserDatabaseReferenceListener()
    }

    fun buildRecyclerView(){
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        usersRecyclerView.setHasFixedSize(true)

        usersArrayList = arrayListOf<User>()
        userAdapter = UserAdapter(usersArrayList)
        userAdapter.setOnClickUserListener(object : UserAdapter.OnClickUserListener{
            override fun onUserClick(position: Int) {
                val intent = Intent(this@UsersList, ChatActivity::class.java)
                intent.putExtra("recipientUserId", usersArrayList[position].id)
                intent.putExtra("recipientUserName", usersArrayList[position].name)
                intent.putExtra("userName", userName)
                startActivity(intent)
            }

        })
        usersRecyclerView.layoutManager = LinearLayoutManager (this)
        usersRecyclerView.adapter = userAdapter
    }

    fun attachUserDatabaseReferenceListener(){
        usersDatabaseReference =
            Firebase.database("https://awesomechat-dc627-default-rtdb.europe-west1.firebasedatabase.app")
                .reference.child("users")
        if(usersChildEventListener == null) {
            usersChildEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        if(user.id != Firebase.auth.currentUser?.uid) {
                            usersArrayList.add(user)
                            userAdapter.notifyItemInserted(0)
                        } else {
                            userName = user.name
                        }
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    // TODO("Not yet implemented")
                }

            }

            usersDatabaseReference.addChildEventListener(usersChildEventListener!!)
        }
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