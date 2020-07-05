package com.example.crossandnulls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_canvas.*

class CanvasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)

        val username: String = username()!!
        val opponentname: String = intent.getStringExtra("opponentname")!!

        val positionData = myRef.child("games").child(encodeGame(username, opponentname))
        var pressed = false
        cam.setOnClickListener {
            if (!pressed) {
                positionData.child(username).setValue("3")
                pressed = true
            }
        }
        bum.setOnClickListener {
            if (!pressed) {
                positionData.child(username).setValue("1")
                pressed = true
            }
        }
        noz.setOnClickListener {
            if (!pressed) {
                positionData.child(username).setValue("2")
                pressed = true
            }
        }
        positionData.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount.toInt() == 2) {
                    val res: String
                    Log.w("YYY", snapshot.child(opponentname).value.toString())
                    val x = snapshot.child(username).value.toString().toInt()
                    val y = snapshot.child(opponentname).value.toString().toInt()
                    val r: Double
                    res = if (x == y) {
                        "Ничья:|"
                    } else if ((x == 3 && y == 2) || (x == 2 && y == 1) || (x == 1) && (y == 3)) {
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
                                        HISTORY.last(),
                                        snapshot.child((n - 1).toString()).value.toString().toInt(),
                                        r
                                    ).second
                                )
                            } else {
                                myRef.child("users").child(opponentname).child("rating")
                                    .child(n.toString()).setValue(
                                        updateRating(
                                            if (HISTORY.isEmpty()) ratingAtStart else HISTORY.last(),
                                            ratingAtStart,
                                            r
                                        ).second
                                    )
                            }
                        }
                    })
                    Log.w("HISTORY", HISTORY.toString())
                    positionData.removeEventListener(this)
                    positionData.removeValue()
                    Toast.makeText(this@CanvasActivity, res, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
