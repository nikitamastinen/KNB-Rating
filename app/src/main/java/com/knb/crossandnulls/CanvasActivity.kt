package com.knb.crossandnulls

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_canvas.*
import kotlinx.android.synthetic.main.dialog_win.*
import java.util.*

class CanvasActivity : AppCompatActivity() {
    private var username: String = String()
    private var opponentname: String = String()
    private var usernameRating = ratingAtStart
    private var opponentnameRating = ratingAtStart
    private var finished = false
    private var resultDialog: Dialog? = null

    override fun onBackPressed() {
        super.onBackPressed()
        resultDialog = null
        resultDialog?.dismiss()
        val intent = Intent(this, AdvertismentActivity::class.java)
        overridePendingTransition(0, 0)
        startActivity(intent)
        finish()
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

        cam.setBackgroundResource(R.drawable.k)
        noz.setBackgroundResource(R.drawable.n)
        bum.setBackgroundResource(R.drawable.b)

        resultDialog = Dialog(this@CanvasActivity)
        var timerCnt = 10
        var pressed = false
        username = username()!!
        opponentname = intent.getStringExtra("opponentname")!!

        opponentnameCanvas.text = username
        usernameCanvas.text = opponentname

        val positionData = myRef.child("games").child(encodeGame(username, opponentname))
        timer.scheduleAtFixedRate(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                runOnUiThread {
                    if (timerCnt == 0) {
                        this.cancel()
                        if (!pressed) {
                            cam.isClickable  = false
                            noz.isClickable  = false
                            bum.isClickable  = false
                            positionData.removeValue()
                            finished = true
                            resultDialog?.setCancelable(false)
                            resultDialog?.setCanceledOnTouchOutside(true)
                            resultDialog?.setContentView(R.layout.dialog_win)
                            resultDialog?.result_win?.text = "Игра прервана"
                            val showRes = 0
                            resultDialog?.score_win?.text = ("") + showRes.toString()
                            resultDialog?.exit_win?.setOnClickListener {
                                resultDialog?.dismiss()
                            }
                            resultDialog?.show()
                        }
                    }
                    timeCanvas.text = timerCnt.toString()
                    timerCnt--
                }
            }
        }, 0, 1000L)
        myRef.child("users").child(opponentname).child("current-rating").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                usernameRating = if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last()
                opponentnameRating = if (snapshot.exists()) {
                    snapshot.value.toString().toInt()
                } else {
                    ratingAtStart
                }
                opponentnameCanvas.text = username + " (" + (if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last()).toString() + ")"
                opponentnameCanvas.setTextColor(colorByRating(if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last()))


                usernameCanvas.text = "$opponentname (" + if (snapshot.exists()) {
                    snapshot.value.toString().toInt()
                } else {
                    ratingAtStart
                }.toString() + ")"

                usernameCanvas.setTextColor(
                    colorByRating(if (snapshot.exists()) {
                    snapshot.value.toString().toInt()
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
        val listener = positionData.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            @SuppressLint("SetTextI18n")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                timer.cancel()
                cam.isClickable  = false
                noz.isClickable  = false
                bum.isClickable  = false
                positionData.removeValue()
                resultDialog?.setCancelable(false)
                resultDialog?.setCanceledOnTouchOutside(true)
                resultDialog?.setContentView(R.layout.dialog_win)
                resultDialog?.result_win?.text = "Игра прервана"
                val showRes = 0
                resultDialog?.score_win?.text = ("") + showRes.toString()
                resultDialog?.show()
                resultDialog?.exit_win?.setOnClickListener {
                    resultDialog?.dismiss()
                }
                positionData.removeEventListener(this)
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })
        Log.w("HHHHH", "onCreate")
        positionData.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount.toInt() == 2 && !finished) {
                    finished = true
                    timer.cancel()
                    positionData.removeEventListener(this)
                    positionData.removeEventListener(listener)
                    cam.isClickable  = false
                    noz.isClickable  = false
                    bum.isClickable  = false
                    val res: String
                    Log.w("YYY", snapshot.child(opponentname).value.toString())
                    val x = snapshot.child(username).value.toString().toInt()
                    val y = snapshot.child(opponentname).value.toString().toInt()
                    val r: Double
                    res = if (x == y) {
                        "Ничья:|"
                    } else if ((x == 3 && y == 2) || (x == 2 && y == 1) || (x == 1 && y == 3)) {
                        "Победа:)"
                    } else {
                        "Поражение:("
                    }
                    r = if (x == y) {
                        0.5
                    } else if ((x == 3 && y == 2) || (x == 2 && y == 1) || (x == 1 && y == 3)) {
                        1.0
                    } else {
                        0.0
                    }
                    putListItem(HISTORY.size, updateRating(usernameRating, opponentnameRating, r).first, this@CanvasActivity)
                    myRef.child("users").child(username).child("current-rating").setValue(updateRating(usernameRating, opponentnameRating, r).first)
                    if (StupidPlayers.contains(opponentname)) {
                        myRef.child("users").child(opponentname).child("current-rating").setValue(updateRating(usernameRating, opponentnameRating, r).second)
                    }
                    positionData.removeValue()
                    resultDialog?.setCancelable(false)
                    resultDialog?.setCanceledOnTouchOutside(true)
                    resultDialog?.setContentView(R.layout.dialog_win)
                    resultDialog?.result_win?.text = res
                    val showRes = updateRating(usernameRating, opponentnameRating, r).first - usernameRating
                    resultDialog?.score_win?.text = (if (showRes > 0) "+" else "") + showRes.toString()
                    resultDialog?.exit_win?.setOnClickListener {
                        resultDialog?.dismiss()
                    }
                    resultDialog?.show()
                    Log.w("HHHHH", HISTORY.toString())
                    Log.w("HHHHH", finished.toString())
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        resultDialog = null
        opponentnameCanvas.text = username
        usernameCanvas.text = opponentname
        myRef.child("games").child(encodeGame(username, opponentname)).removeValue()
    }

    override fun onStop() {
        super.onStop()
        resultDialog  = null
    }


}
