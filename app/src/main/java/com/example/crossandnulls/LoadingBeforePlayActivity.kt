package com.example.crossandnulls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_canvas.*
import java.time.LocalTime
import java.util.*
import kotlin.random.Random

var isLoading = false

class LoadingBeforePlayActivity : AppCompatActivity() {

    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        isLoading = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_before_play)
        val username = username() as String

        var timerCnt = 3

        helpTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (timerCnt == 0) {
                        myRef.child("wait-list").child(username).removeValue()
                        myRef.child("users").child(username).child("games").child("Кротик").setValue("0")
                        myRef.child("games").child(encodeGame("Кротик", username)).child("Кротик").setValue(
                            System.currentTimeMillis() % 3 + 1)
                    }
                    timerCnt--
                }
            }
        }, 0, 1000L)


        listener = myRef.child("wait-list").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {
                    myRef.child("wait-list").child(username).setValue(username)
                } else if (snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        if (i.value != username) {
                            val opponentname = i.value as String
                            helpTimer.cancel()
                            helpTimer = Timer(true)
                            myRef.child("wait-list").child(i.value as String).removeValue()
                            myRef.child("users").child(username).child("games").child(opponentname).setValue("0")
                            myRef.child("users").child(opponentname).child("games").child(username).setValue("0")
                            myRef.child("wait-list").removeEventListener(this)
                        }
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        isLoading = false
        listener?.let { myRef.child("wait-list").removeEventListener(it) }
        myRef.child("wait-list").child(username(CONTEXT)!!).removeValue()
        myRef.child("users").child(username(CONTEXT)!!).child("games").removeValue()
        finish()
    }

    override fun onResume() {
        super.onResume()
        isLoading = true
        CONTEXT = this
    }
}
