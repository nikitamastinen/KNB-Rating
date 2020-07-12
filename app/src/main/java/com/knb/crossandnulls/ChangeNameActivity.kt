package com.knb.crossandnulls

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_start_app.*

class ChangeNameActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        CONTEXT = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)

        val username = username()
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (v?.text == null || v.text?.length!! < 3) {
                    Toast.makeText(this@ChangeNameActivity, "Слишком короткое имя", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, username.toString() + " " + v.text, Toast.LENGTH_LONG).show()
                    if (v.text.toString() == username) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (v.text.length > 12) {
                        Toast.makeText(this@ChangeNameActivity, "Слишком длинное имя", Toast.LENGTH_LONG).show()
                    } else if (v.text.toString().contains(' ')) {
                        Toast.makeText(this@ChangeNameActivity, "Имя не должно содержать пробелы", Toast.LENGTH_LONG).show()
                    } else {
                        myRef.child("users").child(v.text.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(this@ChangeNameActivity, "Данное имя занято", Toast.LENGTH_LONG).show()
                                } else {
                                    myRef.child("users").child(v.text.toString()).child("name").setValue(v.text.toString())
                                    if (HISTORY.isNotEmpty()) myRef.child("users").child(v.text.toString()).child("current-rating").setValue(
                                        HISTORY.last())
                                    myRef.child("users").child(username.toString()).removeValue()
                                    val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                    val editor = prefs.edit()
                                    editor.putString("username", v.text.toString())
                                    editor.apply()
                                    val intent = Intent(this@ChangeNameActivity, MainActivity::class.java)
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
                Toast.makeText(this@ChangeNameActivity, "Слишком короткое имя", Toast.LENGTH_LONG).show()
            } else {
                if (editText.text.toString() == username) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (editText.text.length > 12) {
                    Toast.makeText(this@ChangeNameActivity, "Слишком длинное имя", Toast.LENGTH_LONG).show()
                } else if (editText.text.toString().contains(' ')) {
                    Toast.makeText(this@ChangeNameActivity, "Имя не должно содержать пробелы", Toast.LENGTH_LONG).show()
                } else {
                    myRef.child("users").child(editText.text.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(this@ChangeNameActivity, "Данное имя занято", Toast.LENGTH_LONG).show()
                            } else {
                                myRef.child("users").child(editText.text.toString()).child("name").setValue(editText.text.toString())
                                if (HISTORY.isNotEmpty()) myRef.child("users").child(editText.text.toString()).child("current-rating").setValue(
                                    HISTORY.last())
                                myRef.child("users").child(username.toString()).removeValue()
                                val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                val editor = prefs.edit()
                                editor.putString("username", editText.text.toString())
                                editor.apply()
                                val intent = Intent(this@ChangeNameActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    })
                }
            }
        }
    }
}
