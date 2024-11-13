package com.equativ.kotlinsample

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.equativ.displaysdk.ad.interstitial.SASInterstitialManager
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.displaysdk.model.SASAdStatus
import com.equativ.kotlinsample.databinding.InterstitialActivityBinding

/**
 *  The purpose of this Activity is to display a simple interstitial.
 *
 *  This interstitial is loaded and displayed in 2 steps. It automatically covers the whole
 *  app screen when displayed.
 */
class InterstitialActivity : AppCompatActivity(), SASInterstitialManager.InterstitialManagerListener {

    private val binding by lazy { InterstitialActivityBinding.inflate(layoutInflater) }

    // Definition of the SASInterstitialManager, object in charge of loading and showing the interstitial ad.
    private val interstitialManager: SASInterstitialManager by lazy {
        // Creation of the ad placement first
        val adPlacement = SASAdPlacement(
            Constants.Placements.Interstitial.SITE_ID,
            Constants.Placements.Interstitial.PAGE_ID,
            Constants.Placements.Interstitial.FORMAT_ID,
            Constants.Placements.Interstitial.KEYWORD_TARGETING_STRING
        )

        // You can also use a test placement during development (a placement that will always
        // deliver an ad from a chosen format).

        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_INTERSTITIAL_HTML
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_INTERSTITIAL_VIDEO

        // If you are an inventory reseller, you must provide a Supply Chain Object.
        // More info here: https://help.smartadserver.com/s/article/Sellers-json-and-SupplyChain-Object
        // adPlacement.supplyChainObjectString = "1.0,1!exchange1.com,1234,1,publisher,publisher.com"

        // Creation of the SASInterstitialManager. It is initialized with the previously created ad placement.
        SASInterstitialManager(this, adPlacement)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        updateUI()

        // Set the listener on the SASInterstitialManager
        interstitialManager.interstitialManagerListener = this

        // Setup load button behavior
        binding.loadButton.setOnClickListener {
            // The interstitial manager must first be used to load an ad.
            // Loading an ad will not automatically display it, check the method below for more info…
            interstitialManager.loadAd()
        }

        // Setup show button behavior
        binding.showButton.setOnClickListener {
            // Once an interstitial ad is loaded, you can show it using the interstitial manager.
            interstitialManager.show()

            // Note that you can check if an ad is actually available using the interstitial
            // manager's delegate methods or using the `adStatus` property.
            // Attempting to show the interstitial if no ad is ready will triggers an error!
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Once you are done using your SASInterstitialManager instance, make sure to call
        // `onDestroy` to let it release its resources.
        interstitialManager.onDestroy()
    }

    private fun updateUI() {
        // Using the `adStatus` property to assess if the interstitial is ready to be displayed…
        binding.showButton.isEnabled = interstitialManager.getAdStatus() == SASAdStatus.READY
    }

    /// SASInterstitialManager.InterstitialManagerListener implementation

    override fun onInterstitialAdLoaded(adInfo: SASAdInfo) {
        updateUI()
        Log.i(TAG, "Interstitial ad loaded with info: $adInfo")
    }

    override fun onInterstitialAdFailedToLoad(exception: SASException) {
        updateUI()
        Log.i(TAG, "Interstitial ad failed to load with exception: $exception")
    }

    override fun onInterstitialAdShown() {
        updateUI()
        Log.i(TAG, "Interstitial ad was shown")
    }

    override fun onInterstitialAdFailedToShow(exception: SASException) {
        updateUI()
        Log.i(TAG, "Interstitial ad failed to show with exception: $exception")
    }

    override fun onInterstitialAdClosed() {
        updateUI()
        Log.i(TAG, "Interstitial ad was closed")
    }
    
    override fun onInterstitialAdClicked() {
        Log.i(TAG, "Interstitial ad was clicked")
    }

    override fun onInterstitialAdAudioStart() {
        Log.i(TAG, "Interstitial video ad will start to play audio")

        // Equativ Display SDK is notifying your app that it will play audio.
        // You could optionally pause music depending on your apps design.
    }

    override fun onInterstitialAdAudioStop() {
        Log.i(TAG, "Interstitial video ad did stop to play audio")

        // Equativ Display SDK is notifying your app that it has stopped playing audio.
        // Depending on your apps design, you could resume music here.
    }
    

    companion object {
        private const val TAG = "InterstitialActivity"
    }
}