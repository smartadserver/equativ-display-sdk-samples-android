package com.equativ.kotlinsample

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.equativ.displaysdk.ad.banner.SASBannerView
import com.equativ.displaysdk.bidding.SASBiddingAdFormatType
import com.equativ.displaysdk.bidding.SASBiddingAdResponse
import com.equativ.displaysdk.bidding.SASBiddingCurrency
import com.equativ.displaysdk.bidding.SASBiddingManager
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.kotlinsample.databinding.BiddingBannerActivityBinding

/**
 * The purpose of this Activity is to display a simple banner that renders a SASBiddingAdResponse
 * received through a SASBiddingManager instance
 *
 * The banner will be resized programmatically depending on the aspect ratio of the ad loaded.
 */
class BiddingBannerActivity : AppCompatActivity(), SASBannerView.BannerListener, SASBiddingManager.BiddingManagerListener {

    private val binding by lazy { BiddingBannerActivityBinding.inflate(layoutInflater) }
    private val bannerView by lazy { binding.bannerView }

    // Manager object that will handle all bidding ad calls.
    private val biddingManager by lazy {

        // Create the bidding manager with appropriate Context, SASAdPlacement, Format Type, Currency and Listener
        SASBiddingManager(
            this,
            SASAdPlacement.TEST_PLACEMENT_INAPP_BIDDING_BANNER,
            SASBiddingAdFormatType.BANNER,
            SASBiddingCurrency.USD,
        ).apply { biddingManagerListener = this@BiddingBannerActivity }
    }

    private var isBiddingManagerLoading = false

    private var biddingAdResponse: SASBiddingAdResponse? = null

    private var currentBannerAspectRatio: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Setup load button
        binding.loadBiddingAdButton.setOnClickListener {
            loadBiddingAd()
        }

        // Setup render button
        binding.renderBiddingAdButton.setOnClickListener {
            // Once a banner bidding ad is loaded, render it into the SASBannerView
            biddingAdResponse?.let { bannerView.loadAd(it) }

        }

        // Set the banner listener to the SASBannerView
        bannerView.bannerListener = this

        updateUI()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateBannerHeight()
    }

    private fun loadBiddingAd() {
        if (!isBiddingManagerLoading) {
            isBiddingManagerLoading = true

            biddingManager.loadAd()
            updateUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Once you are done using your SASBannerView instance, make sure to call
        // `onDestroy` to let it release its resources.
        bannerView.onDestroy()
    }

    private fun updateBannerHeight() {
        // Note: In case of currentAspectRatio is null, you can use a default
        // aspect ratio in this case. We recommend '320/50' for a standard banner.
        val aspectRatio = currentBannerAspectRatio ?: (350.0 / 50.0)

        // Note: If the SASAdInfo.aspectRatio stored in currentBannerAspectRatio is -2, that means we
        // recommend you to use the WRAP_CONTENT special value of Android.

        val height = if (aspectRatio < 0.0) {
            LayoutParams.WRAP_CONTENT
        } else if (aspectRatio > 0.0) {
            (resources.displayMetrics.widthPixels / aspectRatio).toInt()
        } else {
            0
        }

        val layoutParams = bannerView.layoutParams
        layoutParams.height = height
        bannerView.layoutParams = layoutParams
    }

    /// SASBannerView.BannerListener implementation

    override fun onBannerAdLoaded(adInfo: SASAdInfo) {
        Log.i(TAG, "Banner ad loaded with info: $adInfo")

        // Store the aspectRatio
        currentBannerAspectRatio = adInfo.aspectRatio

        // Updating the banner aspect ratio
        updateBannerHeight()
    }

    override fun onBannerAdRequestClose() {
        Log.i(TAG, "Banner ad request close")
    }

    override fun onBannerAdFailedToLoad(exception: SASException) {
        Log.i(TAG, "Banner ad failed to load with exception: $exception")

        // Set the ratio to 0 so the banner view is hidden while not displaying ads
        currentBannerAspectRatio = 0.0

        // Update the banner aspect ration
        updateBannerHeight()
    }

    override fun onBannerAdClicked() {
        Log.i(TAG, "Banner ad was clicked")
    }

    override fun onBannerAdCollapsed() {
        Log.i(TAG, "Banner ad was collapsed")
    }

    override fun onBannerAdExpanded() {
        Log.i(TAG, "Banner ad was expanded")
    }

    override fun onBannerAdAudioStart() {
        Log.i(TAG, "Banner video ad will start to play audio")

        // Equativ Display SDK is notifying your app that it will play audio.
        // You could optionally pause music depending on your apps design.
    }

    override fun onBannerAdAudioStop() {
        Log.i(TAG, "Banner video ad did stop to play audio")

        // Equativ Display SDK is notifying your app that it has stopped playing audio.
        // Depending on your apps design, you could resume music here.
    }

    companion object {
        private const val TAG = "BiddingBannerActivity"
    }

    override fun onBiddingManagerAdFailedToLoad(e: SASException) {
        isBiddingManagerLoading = false
        this.biddingAdResponse = null

        Log.i("Sample", "Fail to load a bidding ad response: ${e.message}")
        updateUI()
    }

    override fun onBiddingManagerAdLoaded(biddingAdResponse: SASBiddingAdResponse) {
        isBiddingManagerLoading = false
        this.biddingAdResponse = biddingAdResponse

        // A bidding ad response has been received.
        // You can now load it into an ad view or discard it. See showBiddingAd() for more info.

        Log.i("Sample", "A bidding ad response has been loaded: $biddingAdResponse.")
        updateUI()
    }

    // A bidding ad response is valid only if it has not been consumed already.
    private fun hasValidBiddingAdResponse() = biddingAdResponse?.isConsumed == false

    private fun updateUI() {
        bannerView.post {
            // Buttons
            binding.loadBiddingAdButton.isEnabled = !isBiddingManagerLoading
            binding.renderBiddingAdButton.isEnabled = hasValidBiddingAdResponse()

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