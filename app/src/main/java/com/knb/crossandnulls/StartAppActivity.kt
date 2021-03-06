package com.knb.crossandnulls

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_start_app.*


public var ADMOB_APP_ID: String = "ca-app-pub-8137188857901546~5130759449"

class StartAppActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        CONTEXT = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)

        MobileAds.initialize(this,ADMOB_APP_ID)

        if (username() != null && username()!!.length  >= 3) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            editText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (v?.text == null || v.text?.length!! < 3) {
                        Toast.makeText(this@StartAppActivity, "Слишком короткое имя", Toast.LENGTH_LONG).show()
                    } else {
                        if (v.text.length > 12) {
                            Toast.makeText(this@StartAppActivity, "Слишком длинное имя", Toast.LENGTH_LONG).show()
                        } else if (v.text.toString().contains(' ')) {
                            Toast.makeText(this@StartAppActivity, "Имя не должно содержать пробелы", Toast.LENGTH_LONG).show()
                        } else {
                            myRef.child("users").child(v.text.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            Toast.makeText(this@StartAppActivity, "Данное имя занято", Toast.LENGTH_LONG).show()
                                        } else {
                                            myRef.child("users").child(v.text.toString()).child("name").setValue(v.text.toString())
                                            val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                            val editor = prefs.edit()
                                            editor.putString("username", v.text.toString())
                                            editor.apply()
                                            val intent = Intent(this@StartAppActivity, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }
                                })
                        }
                    }
                }
                true
            }
            button.setOnClickListener {
                if (editText.text == null || editText.text .length!! < 3) {
                    Toast.makeText(this@StartAppActivity, "Слишком короткое имя", Toast.LENGTH_LONG).show()
                } else {
                    if (editText.text.length > 12) {
                        Toast.makeText(this@StartAppActivity, "Слишком длинное имя", Toast.LENGTH_LONG).show()
                    } else if (editText.text.toString().contains(' ')) {
                        Toast.makeText(this@StartAppActivity, "Имя не должно содержать пробелы", Toast.LENGTH_LONG).show()
                    } else {
                        myRef.child("users").child(editText.text.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(this@StartAppActivity, "Данное имя занято", Toast.LENGTH_LONG).show()
                                } else {
                                    myRef.child("users").child(editText.text.toString()).child("name").setValue(editText.text.toString())
                                    val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                    val editor = prefs.edit()
                                    editor.putString("username", editText.text.toString())
                                    editor.apply()
                                    val intent = Intent(this@StartAppActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
