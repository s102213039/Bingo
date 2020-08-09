package com.yanli.bingo

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, View.OnClickListener {
    private lateinit var mUser: FirebaseUser

    companion object {
        val TAG = MainActivity::class.java.simpleName
        val RC_SIGN_IN = 100
        val auth = FirebaseAuth.getInstance()
        val avatarIds = intArrayOf(
            R.drawable.avatar_0,
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        nickname.setOnClickListener {
            showNicknameDialog()
        }
        avatar.setOnClickListener {
            group_avatar.visibility = if (group_avatar.isVisible) View.GONE else View.VISIBLE
        }

        avatar_0.setOnClickListener(this)
        avatar_1.setOnClickListener(this)
        avatar_2.setOnClickListener(this)
        avatar_3.setOnClickListener(this)
        avatar_4.setOnClickListener(this)
        avatar_5.setOnClickListener(this)
        avatar_6.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        auth.currentUser?.let { user ->
            this.mUser = user
            Log.d(TAG, "onAuthStateChanged: ${user.uid}")
            user.displayName?.run {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(user.uid)
                    .child("displayName")
                    .setValue(this)
            }

            //
            FirebaseDatabase.getInstance().getReference("users")
                .child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val member = snapshot.getValue(Member::class.java)
                        member?.let {
                            if (!TextUtils.isEmpty(member.nickname)) {
                                showNicknameDialog()
                            } else {
                                nickname.text = member.nickname
                            }
                            avatar.setImageResource(avatarIds[it.avatarId])
                        }
                    }
                })

//            FirebaseDatabase.getInstance().getReference("users")
//                .child(user.uid)
//                .child("nickname")
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(error: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        snapshot.value?.let {
//                            Log.d(TAG, "onDataChange: $it")
//                        } ?: showNicknameDialog(user)
//                    }
//
//                })

        } ?: run {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
//                            .setIsSmartLockEnabled(false)
                    .build()
                , RC_SIGN_IN
            )
        }
    }

    private fun showNicknameDialog() {
        val editText = EditText(this)
        editText.setText(mUser.displayName)
        AlertDialog.Builder(this)
            .setTitle("Nickname")
            .setMessage("Your nickname")
            .setView(editText)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                FirebaseDatabase.getInstance().getReference("users")
                    .child(mUser.uid)
                    .child("nickname")
                    .setValue(editText.text.toString())
            })
    }

    override fun onClick(view: View?) {
        val selectedId = when (view?.id) {
            R.id.avatar_0 -> 0
            R.id.avatar_1 -> 1
            R.id.avatar_2 -> 2
            R.id.avatar_3 -> 3
            R.id.avatar_4 -> 4
            R.id.avatar_5 -> 5
            R.id.avatar_6 -> 6
            else -> 0
        }
        FirebaseDatabase.getInstance().getReference("users")
            .child(auth.currentUser!!.uid)
            .child("avatarId")
            .setValue(selectedId)
        group_avatar.visibility = View.GONE
    }
}