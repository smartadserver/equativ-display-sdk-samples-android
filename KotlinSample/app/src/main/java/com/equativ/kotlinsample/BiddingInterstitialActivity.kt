package com.equativ.kotlinsample

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.equativ.displaysdk.ad.interstitial.SASInterstitialManager
import com.equativ.displaysdk.bidding.SASBiddingAdFormatType
import com.equativ.displaysdk.bidding.SASBiddingAdResponse
import com.equativ.displaysdk.bidding.SASBiddingCurrency
import com.equativ.displaysdk.bidding.SASBiddingManager
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.displaysdk.model.SASAdStatus
import com.equativ.kotlinsample.databinding.BiddingInterstitialActivityBinding
import com.equativ.kotlinsample.databinding.InterstitialActivityBinding

/**
 *  The purpose of this Activity is to display a simple interstitial.
 *
 *  This interstitial is loaded and displayed in 2 steps. It automatically covers the whole
 *  app screen when displayed.
 */
class BiddingInterstitialActivity : AppCompatActivity(),
    SASInterstitialManager.InterstitialManagerListener,
    SASBiddingManager.BiddingManagerListener {

    private val binding by lazy { BiddingInterstitialActivityBinding.inflate(layoutInflater) }

    private var interstitialManager: SASInterstitialManager? = null

    // Manager object that will handle all bidding ad calls.
    private val biddingManager by lazy {

        // Create the bidding manager with appropriate Context, SASAdPlacement, Format Type, Currency and Listener
        SASBiddingManager(
            this,
            SASAdPlacement.TEST_PLACEMENT_INAPP_BIDDING_INTERSTITIAL,
            SASBiddingAdFormatType.INTERSTITIAL,
            SASBiddingCurrency.USD,
        ).apply { biddingManagerListener = this@BiddingInterstitialActivity }
    }

    private var isBiddingManagerLoading = false

    private var biddingAdResponse: SASBiddingAdResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        updateUI()

        // Setup load button behavior
        binding.loadAndRenderBiddingAdButton.setOnClickListener {
            biddingManager.loadAd()
        }

        // Setup show button behavior
        binding.showBiddingAdButton.setOnClickListener {
            // Once an interstitial bidding ad is loaded and render in the interstitial manager, you can show it.
            interstitialManager?.show()

            // Note that you can check if an ad is actually available using the interstitial
            // manager's delegate methods or using the `adStatus` property.
            // Attempting to show the interstitial if no ad is ready will triggers an error!
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Once you are done using your SASInterstitialManager instance, make sure to call
        // `onDestroy` to let it release its resources.
        interstitialManager?.onDestroy()
    }


    /// SASInterstitialManager.InterstitialManagerListener implementation

    override fun onInterstitialAdLoaded(adInfo: SASAdInfo) {
        isBiddingManagerLoading = false
        updateUI()
        Log.i(TAG, "Interstitial ad loaded with info: $adInfo")
    }

    override fun onInterstitialAdFailedToLoad(exception: SASException) {
        isBiddingManagerLoading = false
        updateUI()
        Log.i(TAG, "Interstitial ad failed to load with exception: $exception")
    }

    override fun onInterstitialAdShown() {
        biddingAdResponse = null
        updateUI()
        Log.i(TAG, "Interstitial ad was shown")
    }

    override fun onInterstitialAdFailedToShow(exception: SASException) {
        updateUI()
        Log.i(TAG, "Interstitial ad failed to show with exception: $exception")
    }

    override fun onInterstitialAdClosed() {
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
        private const val TAG = "BiddingInterstitialActivity"
    }

    override fun onBiddingManagerAdFailedToLoad(e: SASException) {
        isBiddingManagerLoading = false
        this.biddingAdResponse = null

        Log.i("Sample", "Fail to load a bidding ad response: ${e.message}")
        updateUI()
    }

    override fun onBiddingManagerAdLoaded(biddingAdResponse: SASBiddingAdResponse) {

        this.biddingAdResponse = biddingAdResponse

        // A bidding ad response has been received, try to render it in an interstitial manager
        interstitialManager = SASInterstitialManager(this, biddingAdResponse).apply {
            interstitialManagerListener = this@BiddingInterstitialActivity
            loadAd()
        }

        Log.i("Sample", "A bidding ad response has been loaded: $biddingAdResponse.")
    }

    private fun updateUI() {

        window.decorView.post {
            // Buttons
            binding.showBiddingAdButton.isEnabled = interstitialManager?.getAdStatus() == SASAdStatus.READY
            binding.loadAndRenderBiddingAdButton.isEnabled = !isBiddingManagerLoading && !binding.showBiddingAdButton.isEnabled

            // Status textview
            binding.statusTextView.text = when {
                isBiddingManagerLoading -> "loading a bidding ad…"
                biddingAdResponse != null -> biddingAdResponse?.let {
                    "bidding response: ${it.price.cpm} ${it.price.currency}"
                }
                else -> "(no bidding ad response loaded)"
            }
        }
    }
}