package com.example.crossandnulls

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ValueEventListener
import java.util.*


var isLoading = false

class LoadingBeforePlayActivity : AppCompatActivity() {

    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        isLoading = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_before_play)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val username = username() as String

        var timerCnt = 3

        helpTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (timerCnt == 0) {
                        myRef.child("wait-list").child(username).removeValue()
                        val stupidName = StupidPlayers[(System.currentTimeMillis() % StupidPlayers.size).toInt()]
                        myRef.child("users").child(username).child("games").child(stupidName).setValue("0")
                        myRef.child("games").child(encodeGame(stupidName, username)).child(stupidName).setValue(
                            System.currentTimeMillis() % 3 + 1)
                    }
                    timerCnt--
                }
            }
        }, 0, 1000L)

        myRef.child("wait-list").child(username).setValue(username)
    }

    override fun onPause() {
        super.onPause()
        isLoading = false
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
