package com.equativ.kotlinsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.equativ.displaysdk.ad.banner.SASBannerView
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.kotlinsample.databinding.BannerActivityBinding

/**
 * The purpose of this Activity is to display a simple banner.
 *
 * The banner will be resized programmatically depending on the aspect ratio of the ad loaded.
 */
class BannerActivity : AppCompatActivity(), SASBannerView.BannerListener {

    private val binding by lazy { BannerActivityBinding.inflate(layoutInflater) }
    private val bannerView by lazy { binding.bannerView }

    private var currentBannerAspectRatio: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Setup load button
        binding.reloadButton.setOnClickListener {
            loadBanner()
        }

        // Set a global layout listener on the root view to always resize the banner view when the layout is updated
        binding.root.viewTreeObserver.addOnGlobalLayoutListener { updateBannerHeight() }

        // Set the banner listener to the SASBannerView
        bannerView.bannerListener = this

        loadBanner()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Once you are done using your SASBannerView instance, make sure to call
        // `onDestroy` to let it release its resources.
        bannerView.onDestroy()
    }

    private fun loadBanner() {
        // Create the ad placement
        val adPlacement = SASAdPlacement(
            Constants.Placements.Banner.SITE_ID,
            Constants.Placements.Banner.PAGE_ID,
            Constants.Placements.Banner.FORMAT_ID,
            Constants.Placements.Banner.KEYWORD_TARGETING_STRING
        )

        // You can also use a test placement during development (a placement that will always
        // deliver an ad from a chosen format).

        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_BANNER_HTML
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_BANNER_MRAID_EXPAND
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_BANNER_VIDEO

        // If you are an inventory reseller, you must provide a Supply Chain Object.
        // More info here: https://help.smartadserver.com/s/article/Sellers-json-and-SupplyChain-Object
        // adPlacement.supplyChainObjectString = "1.0,1!exchange1.com,1234,1,publisher,publisher.com"

        // Load the placement in the banner using the `loadAd` method.
        bannerView.loadAd(adPlacement)
    }

    private fun updateBannerHeight() {
        // Note: In case of currentAspectRatio is null, you can use a default
        // aspect ratio in this case. We recommend '320/50' for a standard banner.
        val aspectRatio = currentBannerAspectRatio ?: (350.0 / 50.0)

        val height = if (aspectRatio != 0.0) {
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

    companion object {
        private const val TAG = "BannerActivity"
    }

}