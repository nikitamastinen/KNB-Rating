package com.example.crossandnulls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class LoadingBeforePlayActivity : AppCompatActivity() {

    private var listener: ValueEventListener? = null
    private var childListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_before_play)
        val username = username() as String

        listener = myRef.child("wait-list").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {
                    myRef.child("wait-list").child(username).setValue(username)
                } else if (snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        if (i.value != username) {
                            val opponentname = i.value as String
                            myRef.child("wait-list").child(i.value as String).removeValue()
                            myRef.child("users").child(username).child("games").child(opponentname).setValue("0")
                            myRef.child("users").child(opponentname).child("games").child(username).setValue("0")
                            myRef.child("games").child(encodeGame(username, opponentname)).setValue("0")
                        }
                    }
                }
            }
        })

        childListener = myRef.child("users").child(username).child("games").addChildEventListener(object: ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Toast.makeText(this@LoadingBeforePlayActivity, "epoiq", Toast.LENGTH_LONG).show()
                if (snapshot.exists()) {
                    myRef.child("users").child(username).child("games").removeEventListener(this)
                    myRef.child("wait-list").removeEventListener(listener!!)
                    myRef.child("users").child(username).child("games").removeValue()
                    val intent = Intent(this@LoadingBeforePlayActivity, CanvasActivity::class.java)
                    intent.putExtra("opponentname", snapshot.key.toString())
                    startActivity(intent)
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        childListener?.let { myRef.child("users").child(username(CONTEXT)!!).child("games").removeEventListener(it) }
        listener?.let { myRef.child("wait-list").removeEventListener(it) }
        myRef.child("wait-list").child(username(CONTEXT)!!).removeValue()
        myRef.child("users").child(username(CONTEXT)!!).child("games").removeValue()
        finish()
    }
}
