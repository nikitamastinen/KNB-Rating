package com.example.crossandnulls

import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.pow

// Database
val myRef: DatabaseReference = FirebaseDatabase.getInstance().reference

//Current context
var CONTEXT: Context? = null

//Get players name
fun username(context: Context? = CONTEXT): String? {
    val prefs = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    return prefs?.getString("username", "")
}

//rating history
var HISTORY: List<Int> = listOf()

//
var ratingAtStart = 1500

//Game code in firebase
fun encodeGame(a: String, b: String): String {
    if (a < b) {
        return "$a $b"
    }
    return "$b $a"
}

//Rating change formula
fun updateRating(u0: Int, u1: Int, result: Double) : Pair<Int, Int> {
    val koff = 4
    val seed0 = 1.0 / (1.0 + 10.0.pow((u0 - u1).toDouble() / 400.0)) + 1.0
    val seed1 = 1.0 / (1.0 + 10.0.pow((u1 - u0).toDouble() / 400.0)) + 1.0
    val geom0 = (seed0 * (2 - result)).pow(0.5)
    var geom1 = (seed1 * (result + 1)).pow(0.5)
    var l = 0.0
    var r = 1e5
    for (i in 0..20) {
        val m = (r + l) / 2
        if (geom0 > 1.0 / (1.0 + 10.0.pow((m - u1).toDouble() / 400.0)) + 1.0) {
            r = m
        } else {
            l = m
        }
    }
    val r0 = (u0 + (l - u0) / koff).toInt()
    l = 0.0
    r = 1e5
    for (i in 0..20) {
        val m = (r + l) / 2
        if (geom1 > 1.0 / (1.0 + 10.0.pow((m - u0).toDouble() / 400.0)) + 1.0) {
            r = m
        } else {
            l = m
        }
    }
    val r1 = (u1 + (l - u1) / koff).toInt()
    Log.w("RRRR", "$r0 $r1")
    return Pair(r0, r1)
}