package com.example.crossandnulls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_item.view.*
import java.util.*
import kotlin.math.max

class MainActivity : AppCompatActivity(), RewardedVideoAdListener {
    private lateinit var mRewardedVideoAd: RewardedVideoAd    //рекламный видос
    private var videoPass = false

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.settings -> {
                if (mRewardedVideoAd.isLoaded) {
                    videoPass = true
                    mRewardedVideoAd.show()
                } else {
                    Toast.makeText(this, "Видео не загрузилось", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        CONTEXT = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd?.adUnitId = "ca-app-pub-8137188857901546/6252269429"           //загрузка обЪявления
        mInterstitialAd?.loadAd(AdRequest.Builder().build())
        nul_score.paintFlags = nul_score.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()
        nul_score.setOnClickListener {
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
            else {
                Toast.makeText(this, "Видео не загрузилось", Toast.LENGTH_SHORT).show()
            }
        }
        val username = username().toString()
        myRef.child("users").child(username).child("games").addChildEventListener(object:
            ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists() && previousChildName == null && isLoading) {
                    helpTimer.cancel()
                    helpTimer = Timer(true)
                    myRef.child("users").child(username).child("games").removeValue()
                    val intent = Intent(CONTEXT, CanvasActivity::class.java)
                    intent.putExtra("opponentname", snapshot.key.toString())
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        })
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        window.statusBarColor = colorByRating(Color.BLACK)
        textView.text = username()
        var series: PointsGraphSeries<DataPoint> = PointsGraphSeries(arrayOf())
        var series2: LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf())
        val graph = findViewById<GraphView>(R.id.graph)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(8.0)
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(1600.0)
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true
        updateGraphColors(graph, 8.0, 1600.0)
        graph.addSeries(series2)
        graph.addSeries(series)


        val snapshot = getList(this)
        if (snapshot.isNotEmpty()) {
            HISTORY = snapshot as MutableList<Int>
            val text = username() + " (" + HISTORY.last() + ")"
            textView.setTextColor(colorByRating(HISTORY.last()))
            window.statusBarColor = colorByRating(HISTORY.last())
            supportActionBar?.setBackgroundDrawable(ColorDrawable(colorByRating(HISTORY.last())))
            textView.text = text
            val data: Array<DataPoint> = Array(HISTORY.size) {i -> DataPoint(i.toDouble(), HISTORY[i].toDouble())}
            Log.w("HISTORY", data.toString())
            series = PointsGraphSeries(data)
            series2 = LineGraphSeries(data)
            series.size = 10f
            series.color = Color.BLACK
            series2.color = Color.BLACK
            graph.clearFocus()
            graph.viewport.setMinX(0.0)
            graph.viewport.setMaxX(max(4.0, HISTORY.size.toDouble()))
            graph.viewport.setMinY(0.0)
            var mxY = 0
            for (i in HISTORY) mxY = max(mxY, i)
            updateGraphColors(graph, max(4.0, HISTORY.size.toDouble()), mxY.toDouble() + 300.0)
            graph.addSeries(series2)
            graph.addSeries(series)
            mxY.toDouble().let { graph.viewport.setMaxY(it + 300.0) }
            graph.viewport.isYAxisBoundsManual = true
            graph.viewport.isXAxisBoundsManual = true
            myRef.child("users").child(username()!!).child("current-rating").setValue(HISTORY.last())
        } else {
            val text = username() + " (Не в рейтинге)"
            textView.text = text
        }

        playOnline.setOnClickListener {
            val intent = Intent(this, LoadingBeforePlayActivity::class.java)
            intent.putExtra("username", "")
            startActivity(intent)
            //overridePendingTransition(0, 0)
        }
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-8137188857901546/4556044372",          //TODO зменить на настоящий идентификатор
            AdRequest.Builder().build())
    }
    override fun onRewarded(reward: RewardItem) {
        if (!videoPass) {
            myRef.child("users").child(username(this).toString()).child("current-rating")
                .removeValue()
            val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val name = username()
            prefs.edit().clear().apply()
            HISTORY.clear()
            prefs.edit().putString("username", name).apply()
            val intent = Intent(this, MainActivity::class.java)
            overridePendingTransition(0, 0)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, ChangeNameActivity::class.java)
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
        videoPass = false
    }

    override fun onRewardedVideoAdLeftApplication() {
        videoPass = false
    }

    override fun onRewardedVideoAdClosed() {
        videoPass  = false
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        videoPass = false
    }

    override fun onRewardedVideoAdLoaded() {}

    override fun onRewardedVideoAdOpened() {}

    override fun onRewardedVideoStarted() {}

    override fun onRewardedVideoCompleted() {}

    override fun onResume() {
        super.onResume()
        CONTEXT = this
        val TOP: MutableList<Pair<String, Int>> = mutableListOf()
        username()
        myRef.child("users").orderByChild("current-rating").limitToLast(30).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    if (i.hasChild("current-rating")) {
                        TOP.add(Pair(i.key.toString(), i.child("current-rating").value.toString().toInt()))
                    }
                }
                ratingLinearLayout.removeAllViews()
                for ((id, i) in TOP.reversed().withIndex()) {
                    val view: View =
                        layoutInflater.inflate(R.layout.activity_main_item, ratingLinearLayout, false)
                    view.itemName.text = (id + 1).toString() + ". " + i.first
                    view.itemName.setTextColor(colorByRating(i.second))
                    view.itemRating.text = i.second.toString()
                    ratingLinearLayout.addView(view)
                }
            }
        })
    }
}