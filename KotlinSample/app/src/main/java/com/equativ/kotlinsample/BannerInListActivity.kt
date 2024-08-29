package com.equativ.kotlinsample

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.equativ.displaysdk.ad.banner.SASBannerView
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.displaysdk.model.SASParallaxMargins
import com.equativ.kotlinsample.databinding.BannerInListActivityBinding
import com.equativ.kotlinsample.databinding.ListBannerHolderBinding
import com.equativ.kotlinsample.databinding.ListItemBinding
import com.equativ.kotlinsample.viewholder.BannerViewHolder
import com.equativ.kotlinsample.viewholder.ListItemViewHolder

class BannerInListActivity : AppCompatActivity(), SASBannerView.BannerListener  {

    private val binding by lazy { BannerInListActivityBinding.inflate(layoutInflater) }

    // Definition of the SASBannerView
    private val bannerView by lazy { SASBannerView(this) }

    private var currentBannerAspectRatio: Double? = null

    // Implementation of RecyclerView.Adapter class, used by the RecycleView.
    private val recyclerViewAdapter = object : RecyclerView.Adapter<ViewHolder>() {

        private val viewTypeContent = 0
        private val viewTypeAd = 1

        override fun getItemViewType(position: Int): Int {
            return if (position == AD_POSITION) { viewTypeAd } else { viewTypeContent }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return if (viewType == viewTypeAd) {
                // Create banner view holder
                BannerViewHolder(ListBannerHolderBinding.inflate(inflater, parent, false))
            } else {
                // Create content view holder
                ListItemViewHolder(ListItemBinding.inflate(inflater, parent, false))
            }
        }

        override fun getItemCount(): Int = 25

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder is ListItemViewHolder) {
                holder.setIndex(position)
            } else if (holder is BannerViewHolder) {
                holder.binding.bannerContainer.addView(bannerView)
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            if (holder is BannerViewHolder) {
                holder.binding.bannerContainer.removeView(bannerView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Setup swipe to refresh
        binding.root.setOnRefreshListener {
            loadBanner()
            binding.root.isRefreshing = false
        }

        // Setup recyclerview
        binding.recyclerView.adapter = recyclerViewAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        // Set a global layout listener on the root view to always resize the banner view when the layout is updated
        binding.root.viewTreeObserver.addOnGlobalLayoutListener { updateBannerHeight() }

        // Set the banner listener to the SASBannerView
        bannerView.bannerListener = this

        // Define LayoutParam to the SASBannerView
        bannerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)

        // In this integration, the placement used is a parallax placement.
        // To properly setup parallax, please check if you need to add margin to the SASBannerView to avoid having the parallax
        // ad being rendered below any UI element, such as the navBar for instance.

        // bannerView.parallaxMargins = SASParallaxMargins(LEFT_MARGIN, TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN)

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
            Constants.Placements.BannerInList.SITE_ID,
            Constants.Placements.BannerInList.PAGE_ID,
            Constants.Placements.BannerInList.FORMAT_ID,
            Constants.Placements.BannerInList.KEYWORD_TARGETING_STRING
        )

        // You can also use a test placement during development (a placement that will always
        // deliver an ad from a chosen format).

        // You can test other parallax placements by using
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_BANNER_PARALLAX_HTML
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_BANNER_PARALLAX_VIDEO

        // Or any other banner format test placements:
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
        Log.i(TAG, "Banner ad request close.")
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
        private const val TAG = "BannerInListActivity"

        private const val AD_POSITION = 12
    }
}