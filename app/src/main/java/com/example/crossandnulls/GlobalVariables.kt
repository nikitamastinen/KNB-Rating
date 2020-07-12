package com.example.crossandnulls

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.lang.Math.abs
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

//межстраничное объявление
var mInterstitialAd: InterstitialAd? = null

// Database
val myRef: DatabaseReference = FirebaseDatabase.getInstance().reference

//Stupid players (non-alive)
var StupidPlayers: MutableList<String> = mutableListOf("Кротик", "Антип", "Гугл", "Петуш", "ASASASASA", "DOMINO", "Art", "winner03", "tourist")

//Current context
var CONTEXT: Context? = null

//Get players name
fun username(context: Context? = CONTEXT): String? {
    val prefs = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    return prefs?.getString("username", "")
}

fun putList(lst: List<Int>, context: Context?) {
    val prefs = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    val editor = prefs?.edit()
    for (i in lst.indices) {
        editor?.putString("rating/$i", lst[i].toString())
        editor?.apply()
    }
}

fun putListItem(position: Int, value: Int, context: Context?) {
    val prefs = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    val editor = prefs?.edit()
    HISTORY.add(value)
    editor?.putString("rating/$position", value.toString())
    editor?.apply()
}

fun getList(context: Context?): List<Int> {
    val prefs = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    val emLst = mutableListOf<Int>()
    var cnt = 0
    while (prefs?.getString("rating/$cnt", "") != "") {
        prefs?.getString("rating/$cnt", "")?.toInt()?.let { emLst.add(it) }
        cnt++
    }
    HISTORY = emLst
    return emLst
}

//rating history
var HISTORY: MutableList<Int> = mutableListOf()

// rating at start
var ratingAtStart = 1000

// Timer for non-alive users
var helpTimer = Timer(true)

//Game code in firebase
fun encodeGame(a: String, b: String): String {
    if (a < b) {
        return "$a $b"
    }
    return "$b $a"
}

//Color List
var colorList: List<Int> = listOf(0, 700, 1000, 1300, 1600, 1900, 2200, 2500, 1000000)

//Colors
var colorsRating: List<Int> = listOf(
    Color.rgb(0, 0, 0),
    Color.rgb(200, 200, 200),
    Color.rgb(180, 200, 200),
    Color.rgb(220, 160, 220),
    Color.rgb(100, 200, 250),
    Color.GREEN,
    Color.rgb(255, 180, 5),
    Color.rgb(255, 0, 196),
    Color.RED)


//get color by rating
fun colorByRating(r: Int): Int {
    for (i in 0..8) {
        if (r < colorList[i]) {
            return colorsRating[i]
        }
    }
    return colorsRating.last()
}
//Rating color
@ExperimentalStdlibApi
fun updateGraphColors(graph: GraphView, xMax: Double, yMax: Double) {
    var cnt = 0
    var a: MutableList<Int> = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
    for (i in colorList.reversed()) {
        if (i > yMax) {
            a.removeLast()
            continue
        }
    }
    a.add(a.last() + 1)
    for (i in a.reversed()) {
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf(
            DataPoint(0.0, colorList[i].toDouble()),
            DataPoint(xMax, colorList[i].toDouble())
        ))
        series.thickness = 0
        series.backgroundColor = colorsRating[i]
        series.isDrawBackground = true
        graph.addSeries(series)
    }
}

//Rating change formula
fun updateRating(u0: Int, u1: Int, result: Double) : Pair<Int, Int> {
    val koff = 2.5 * kotlin.math.abs((u1.toDouble() + u0) / 2.0) /  kotlin.math.abs(u1.toDouble() * u0).pow(0.5)

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
    val r0 = (u0.toDouble() + (l - u0) / koff)
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
    val r1 = (u1.toDouble() + (l - u1) / koff)
    Log.w("RRRR", "$r0 $r1")
    return Pair((r0 * (u0 + u1) / (r0 + r1)).roundToInt(), (r1 * (u0 + u1) / (r0 + r1)).roundToInt())
}