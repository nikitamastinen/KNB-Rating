package com.example.crossandnulls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_item.view.*
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    //TODO too long names
    //TODO Double people in rating list
    //TODO float points in graph (minX = 4)
    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.text = username()
        var series: PointsGraphSeries<DataPoint> = PointsGraphSeries(arrayOf())
        var series2: LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf())
        val graph = findViewById<GraphView>(R.id.graph)
        graph.addSeries(series2)
        graph.addSeries(series)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(8.0)
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(1600.0)
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true
        myRef.child("users").child(username()!!).child("rating").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    HISTORY = snapshot.value as List<Int>
                    val text = username() + " (" + HISTORY.last() + ")"
                    textView.text = text
                    val data: Array<DataPoint> = Array(HISTORY.size) {i -> DataPoint(i.toDouble(), HISTORY[i].toDouble())}
                    Log.w("HISTORY", data.toString())
                    series = PointsGraphSeries(data)
                    series2 = LineGraphSeries(data)
                    series.color = Color.RED
                    series.size = 10f
                    series2.color = Color.RED
                    graph.clearFocus()
                    graph.addSeries(series2)
                    graph.addSeries(series)
                    graph.viewport.setMinX(0.0)
                    graph.viewport.setMaxX(max(4.0, HISTORY.size.toDouble()))
                    graph.viewport.setMinY(0.0)
                    var mxY = 0
                    for (i in HISTORY) mxY = max(mxY, i)
                    mxY.toDouble().let { graph.viewport.setMaxY(it + 300.0) }
                    graph.viewport.isYAxisBoundsManual = true
                    graph.viewport.isXAxisBoundsManual = true
                    myRef.child("users").child(username()!!).child("current-rating").setValue(HISTORY.last())
                } else {
                    val text = username() + " (Не в рейтинге)"
                    textView.text = text
                }
            }
        })

        playOnline.setOnClickListener {
            val intent = Intent(this, LoadingBeforePlayActivity::class.java)
            intent.putExtra("username", "")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        var TOP: MutableList<Pair<String, Int>> = mutableListOf()
        myRef.child("users").orderByChild("current-rating").limitToLast(30).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    if (i.hasChild("rating")) {
                        val n = i.child("rating").childrenCount.toInt() - 1
                        TOP.add(Pair(i.key.toString(), i.child("rating").child("$n").value.toString().toInt()))
                    }
                }
                ratingLinearLayout.removeAllViews()
                for (i in TOP.reversed()) {
                    val view: View =
                        layoutInflater.inflate(R.layout.activity_main_item, ratingLinearLayout, false)
                    view.itemName.text = i.first
                    view.itemRating.text = i.second.toString()
                    ratingLinearLayout.addView(view)
                }
            }
        })
    }
}

class CanvasView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var Xmax: Float = 0f
    var Ymax: Float = 0f
    var paint: Paint = Paint()

    init {
        paint.color = Color.rgb(27, 217, 217)
        paint.strokeWidth = 5f
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        Xmax = width.toFloat()
        Ymax = height.toFloat()
        canvas?.drawLine(50f,0f, 50f, Ymax - 50f, paint)
        canvas?.drawLine(47.5f, Ymax - 50f, Xmax - 50f, Ymax - 50f, paint)
        Log.w("TTTT", "$Xmax $Ymax")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}