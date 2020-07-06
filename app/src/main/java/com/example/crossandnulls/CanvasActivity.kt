package com.example.crossandnulls

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_canvas.*
import java.util.*
import kotlin.concurrent.timer

class CanvasActivity : AppCompatActivity() {

    private var username: String = String()
    private var opponentname: String = String()
    private var finished = false

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        overridePendingTransition(0, 0)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        CONTEXT = this
    }

    private val timer = Timer(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)

        var timerCnt = 10
        var pressed = false
        username = username()!!
        opponentname = intent.getStringExtra("opponentname")!!
        val positionData = myRef.child("games").child(encodeGame(username, opponentname))
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (timerCnt == 0) {
                        this.cancel()
                        if (!pressed) {
                            cam.isClickable  = false
                            noz.isClickable  = false
                            bum.isClickable  = false
                            positionData.child(username).setValue("4")
                            finished = true
                        }
                    }
                    timeCanvas.text = timerCnt.toString()
                    timerCnt--
                }
            }
        }, 0, 1000L)
        myRef.child("users").child(opponentname).child("rating").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                opponentnameCanvas.text = username + " (" + (if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last()).toString() + ")"
                opponentnameCanvas.setTextColor(colorByRating(if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last()))
                val n = snapshot.childrenCount
                usernameCanvas.text = "$opponentname (" + if (n > 0) {
                    snapshot.child((n - 1).toString()).value.toString().toInt()
                } else {
                    ratingAtStart
                }.toString() + ")"

                usernameCanvas.setTextColor(
                    colorByRating(if (n > 0) {
                    snapshot.child((n - 1).toString()).value.toString().toInt()
                } else {
                    ratingAtStart
                }))
            }
        })

        cam.setOnClickListener {
            timer.cancel()
            if (!pressed) {
                positionData.child(username).setValue("3")
                pressed = true
            }
        }
        bum.setOnClickListener {
            timer.cancel()
            if (!pressed) {
                positionData.child(username).setValue("1")
                pressed = true
            }
        }
        noz.setOnClickListener {
            timer.cancel()
            if (!pressed) {
                positionData.child(username).setValue("2")
                pressed = true
            }
        }
        positionData.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount.toInt() == 2) {
                    cam.isClickable  = false
                    noz.isClickable  = false
                    bum.isClickable  = false
                    finished = true
                    val res: String
                    Log.w("YYY", snapshot.child(opponentname).value.toString())
                    val x = snapshot.child(username).value.toString().toInt()
                    val y = snapshot.child(opponentname).value.toString().toInt()
                    val r: Double
                    res = if (x == y) {
                        "Ничья:|"
                    } else if (y == 4 || (x == 3 && y == 2) || (x == 2 && y == 1) || (x == 1) && (y == 3)) {
                        "Победа:)"
                    } else {
                        "Поражение:("
                    }
                    r = if (x == y) {
                        0.5
                    } else if ((x == 3 && y == 2) || (x == 2 && y == 1) || (x == 1) && (y == 3)) {
                        1.0
                    } else {
                        0.0
                    }
                    myRef.child("users").child(opponentname).child("rating").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val n = snapshot.childrenCount
                            if (n > 0) {
                                myRef.child("users").child(opponentname).child("rating")
                                    .child(n.toString()).setValue(
                                        updateRating(
                                            if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                            snapshot.child((n - 1).toString()).value.toString()
                                                .toInt(),
                                            r
                                        ).second
                                    )
                                myRef.child("users").child(opponentname).child("current-rating")
                                    .setValue(
                                        updateRating(
                                            if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                            snapshot.child((n - 1).toString()).value.toString()
                                                .toInt(),
                                            r
                                        ).second
                                    )
                                if (StupidPlayers.contains(opponentname)) {
                                    myRef.child("users").child(username).child("rating").addListenerForSingleValueEvent(object: ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {}
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val n1 = snapshot.childrenCount
                                            myRef.child("users").child(username).child("rating")
                                                .child(n1.toString()).setValue(
                                                    updateRating(
                                                        if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                                        snapshot.child((n1 - 1).toString()).value.toString()
                                                            .toInt(),
                                                        r
                                                    ).first
                                                )
                                            myRef.child("users").child(username).child("current-rating")
                                                .setValue(
                                                    updateRating(
                                                        if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                                        snapshot.child((n1 - 1).toString()).value.toString()
                                                            .toInt(),
                                                        r
                                                    ).first
                                                )
                                        }
                                    })
                                }
                            } else {
                                myRef.child("users").child(opponentname).child("rating")
                                    .child(n.toString()).setValue(
                                        updateRating(
                                            if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                            ratingAtStart,
                                            r
                                        ).second
                                    )
                                myRef.child("users").child(opponentname).child("current-rating").setValue(
                                    updateRating(
                                        if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                        ratingAtStart,
                                        r
                                    ).second
                                )
                                if (StupidPlayers.contains(opponentname)) {
                                    myRef.child("users").child(username).child("rating").addListenerForSingleValueEvent(object: ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {}
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val n1 = snapshot.childrenCount
                                            myRef.child("users").child(username).child("rating")
                                                .child(n1.toString()).setValue(
                                                    updateRating(
                                                        if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                                        ratingAtStart,
                                                        r
                                                    ).first
                                                )

                                            myRef.child("users").child(username).child("current-rating")
                                                .setValue(
                                                    updateRating(
                                                        if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                                        ratingAtStart,
                                                        r
                                                    ).first
                                                )
                                        }
                                    })
                                }
                            }
                        }
                    })
                    positionData.removeEventListener(this)
                    positionData.removeValue()
                    Toast.makeText(this@CanvasActivity, res, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        username = username()!!
        opponentname = intent.getStringExtra("opponentname")!!
        if (!finished) {
            myRef.child("games").child(encodeGame(username, opponentname)).child(username).setValue(4)
        }
    }
}
