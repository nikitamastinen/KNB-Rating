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
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max


class MainActivity : AppCompatActivity() {

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
                    graph.viewport.setMaxX(HISTORY.size.toDouble())
                    graph.viewport.setMinY(0.0)
                    var mxY = 0
                    for (i in HISTORY) mxY = max(mxY, i)
                    mxY.toDouble().let { graph.viewport.setMaxY(it + 300.0) }
                    graph.viewport.isYAxisBoundsManual = true
                    graph.viewport.isXAxisBoundsManual = true
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