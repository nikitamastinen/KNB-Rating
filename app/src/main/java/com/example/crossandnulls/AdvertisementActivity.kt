package com.example.crossandnulls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class AdvertismentActivity : AppCompatActivity() {

    //межстраничное обЪявление

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advertisment)
        if (mInterstitialAd != null && mInterstitialAd!!.isLoaded) {
            mInterstitialAd?.show()
            mInterstitialAd?.adListener = object: AdListener() {                       //прописывание функций
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    //Код, который будет выполнен после завершения загрузки объявления.
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                    //Код, который будет выполняться при сбое рекламного запроса..
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }

                override fun onAdOpened() {
                    // Code to be executed when the ad is displayed.
                    //Код, который будет выполнен при показе объявления
                }

                override fun onAdClicked() {        //для норм пацанов функция
                    // Code to be executed when the user clicks on an ad.
                    //Код, который будет выполняться, когда пользователь нажимает на объявление.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    //Код, который будет выполнен, когда пользователь покинет приложение
                }

                override fun onAdClosed() {
                    // Code to be executed when the interstitial ad is closed.
                    //Код, который будет выполняться при закрытии интерстициального объявления.
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }
}
