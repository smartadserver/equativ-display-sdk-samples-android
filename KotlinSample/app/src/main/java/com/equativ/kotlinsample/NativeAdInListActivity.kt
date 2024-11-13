package com.equativ.kotlinsample

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.equativ.displaysdk.ad.nativead.SASNativeAdView
import com.equativ.displaysdk.ad.nativead.SASNativeAdViewBinder
import com.equativ.displaysdk.exception.SASException
import com.equativ.displaysdk.model.SASAdInfo
import com.equativ.displaysdk.model.SASAdPlacement
import com.equativ.displaysdk.model.SASNativeAdAssets
import com.equativ.kotlinsample.databinding.AdInListActivityBinding
import com.equativ.kotlinsample.databinding.ListAdHolderBinding
import com.equativ.kotlinsample.databinding.ListItemBinding
import com.equativ.kotlinsample.viewholder.AdViewHolder
import com.equativ.kotlinsample.viewholder.ListItemViewHolder

class NativeAdInListActivity : AppCompatActivity(), SASNativeAdView.NativeAdListener  {

    private val binding by lazy { AdInListActivityBinding.inflate(layoutInflater) }

    // Definition of the SASNativeAdView
    private val nativeAdView by lazy { SASNativeAdView(this) }

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
                // Create ad view holder
                AdViewHolder(ListAdHolderBinding.inflate(inflater, parent, false))
            } else {
                // Create content view holder
                ListItemViewHolder(ListItemBinding.inflate(inflater, parent, false), R.string.activity_native_ad_in_list_header_instructions)
            }
        }

        override fun getItemCount(): Int = 25

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder is ListItemViewHolder) {
                holder.setIndex(position)
            } else if (holder is AdViewHolder) {
                holder.binding.adContainer.addView(nativeAdView)
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            if (holder is AdViewHolder) {
                holder.binding.adContainer.removeView(nativeAdView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Setup swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadNativeAd()
            binding.swipeRefresh.isRefreshing = false
        }

        // Setup recyclerview
        binding.recyclerView.adapter = recyclerViewAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        // Set the native-ad listener to the SASNativeAdView
        nativeAdView.nativeAdListener = this

        // Define LayoutParam to the SASNativeAdView, we suggest you to use WRAP_CONTENT as height layout param to let
        // the view take the right space.
        nativeAdView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        loadNativeAd()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Once you are done using your SASNativeAdView instance, make sure to call
        // `onDestroy` to let it release its resources.
        nativeAdView.onDestroy()
    }

    private fun loadNativeAd() {
        // Create the ad placement
        val adPlacement = SASAdPlacement(
            Constants.Placements.NativeAdWithIconAndCover.SITE_ID,
            Constants.Placements.NativeAdWithIconAndCover.PAGE_ID,
            Constants.Placements.NativeAdWithIconAndCover.FORMAT_ID,
            Constants.Placements.NativeAdWithIconAndCover.KEYWORD_TARGETING_STRING
        )

        // You can also use a test placement during development (a placement that will always
        // deliver an ad from a chosen format).

        // You can test other native-ad placements by using
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_NATIVE_AD_ICON
        // val adPlacement = SASAdPlacement.TEST_PLACEMENT_NATIVE_AD_ICON_AND_COVER

        // If you are an inventory reseller, you must provide a Supply Chain Object.
        // More info here: https://help.smartadserver.com/s/article/Sellers-json-and-SupplyChain-Object
        // adPlacement.supplyChainObjectString = "1.0,1!exchange1.com,1234,1,publisher,publisher.com"

        // Load the placement in the native-ad view using the `loadAd` method.
        nativeAdView.loadAd(adPlacement)
    }

    /// SASNativeAdView.NativeAdListener implementation

    override fun onNativeAdClicked() {
        Log.i(TAG, "Native ad clicked")
    }

    override fun onNativeAdFailedToLoad(exception: SASException) {
        Log.i(TAG, "Native ad failed to load with exception: $exception")
    }

    override fun onNativeAdLoaded(adInfo: SASAdInfo, nativeAdAssets: SASNativeAdAssets) {
        Log.i(TAG, "Native ad loaded with info: $adInfo - and assets: $nativeAdAssets")

        /// You have nothing to do especially as the rendering part is done by the Equativ Display SDK.
        /// The SDK will automatically choose between several default layout to find the best suitable
        /// view in which render the native-ad assets.

        /// However, if you want to render the native-ad in your custom layout, you can do it by implementing
        /// optional method 'onNativeAdViewBinderRequested'. You can found implementation examples below.
    }

    override fun onNativeAdRequestClose() {
        Log.i(TAG, "Native ad request close")
    }

    override fun onNativeAdViewBinderRequested(nativeAdAssets: SASNativeAdAssets): SASNativeAdViewBinder? {
        /// This method is called when the Equativ Display SDK needs a layout in which render the native-ad assets.
        /// We suggest you several implementation examples:

        /// By using a Layout ID
//        val viewBinder = SASNativeAdViewBinder.Builder(R.layout.custom_native_ad)
//            .setTitleTextViewId(R.id.title_textview)
//            .setBodyTextViewId(R.id.body_textview)
//            .setIconContainerViewGroupId(R.id.icon_image_container)
//            .setMainViewContainerViewGroupId(R.id.cover_image_container)
//            .setRatingBarId(R.id.rating_bar)
//            .setAdvertiserTextViewId(R.id.advertiser_textview)
//            .setCallToActionButtonId(R.id.cta_button)
//            .build()
//
//        return viewBinder

        /// By using an already existing view. This is preferable to customize your native-ad layout depending
        /// on the given nativeAdAssets. Note that the Equativ Display SDK will never update the visibility of your
        /// custom layout, so this is your responsability to hide assets when they are empty.

//        val layoutViewGroup = layoutInflater.inflate(R.layout.custom_native_ad, binding.root, false) as ViewGroup
//        val viewBinder = SASNativeAdViewBinder.Builder(layoutViewGroup)
//            .setTitleTextViewId(R.id.title_textview)
//            .setBodyTextViewId(R.id.body_textview)
//            .setIconContainerViewGroupId(R.id.icon_image_container)
//            .setMainViewContainerViewGroupId(R.id.cover_image_container)
//            .setRatingBarId(R.id.rating_bar)
//            .setAdvertiserTextViewId(R.id.advertiser_textview)
//            .setCallToActionButtonId(R.id.cta_button)
//            .build()
//
//        // Hide main view if empty
//        if (nativeAdAssets.mainView == null) {
//            layoutViewGroup.findViewById<ViewGroup>(R.id.cover_image_container).visibility = GONE
//        }
//
//        // Hide icon view if empty
//        if (nativeAdAssets.iconImage == null) {
//            layoutViewGroup.findViewById<ViewGroup>(R.id.icon_image_container).visibility = GONE
//        }
//
//        // Hide rating bar if empty
//        if (nativeAdAssets.rating == null) {
//            layoutViewGroup.findViewById<RatingBar>(R.id.rating_bar).visibility = GONE
//        }
//
//        // Etc.
//
//        return viewBinder

        /// Or you can simply return null, which is the default behavior of this method if you do not implement it.
        /// In that case, the Equativ Display SDK will automatically choose most suitable default layout to render
        /// the native-ad assets.

        return null
    }

    companion object {
        private const val TAG = "NativeAdInListActivity"

        private const val AD_POSITION = 12
    }
}