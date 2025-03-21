package com.equativ.kotlinsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import com.equativ.displaysdk.model.digitalserviceact.SASDigitalServiceActConfig
import com.equativ.displaysdk.model.digitalserviceact.SASDigitalServiceActConfigDSARequired
import com.equativ.displaysdk.model.digitalserviceact.SASDigitalServiceActConfigDataToPub
import com.equativ.displaysdk.model.digitalserviceact.SASDigitalServiceActConfigTransparency
import com.equativ.displaysdk.util.SASConfiguration
import com.equativ.kotlinsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupUI()

        // -----------------------------------------------
        // Equativ Display SDK configuration
        // -----------------------------------------------

        // The Equativ Display SDK must be configured before doing anything else. Otherwise it will crash the app at the first load.
        SASConfiguration.configure(this)

        // Enabling logging can be useful to get informations if ads are not displayed properly.
        // Don't forget to turn logging OFF before submitting your app.
        // SASConfiguration.isLoggingEnabled = true

        // -----------------------------------------------
        // Privacy laws compliancy
        // -----------------------------------------------

        // The SDK is able to handle consent generated through a TCF compliant CMP (for GPP frameworks).
        //
        // If you deploy your app in a country implementing one of these privacy laws, remember to install and setup
        // an IAB compliant CMP!

        // -----------------------------------------------
        // Digital Service Act (DSA)
        // -----------------------------------------------

        // The SDK is able to handle Digital Service Act. You will find more information about it in our documentation.
        //
        // Here is an example of how to set up your DSA configuration for the Equativ Display SDK.
        //
        // SASConfiguration.digitalServiceActConfig = SASDigitalServiceActConfig(
        //     SASDigitalServiceActConfigDSARequired.REQUIRED,
        //     SASDigitalServiceActConfigDataToPub.OPTIONAL_TO_SEND_TRANSPARENCY_DATA,
        //     listOf(SASDigitalServiceActConfigTransparency("https://domain.com", listOf(1, 2)))
        // )
    }

    private fun setupUI() {
        // Setup content view
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create header and footer
        val headerLayout = layoutInflater.inflate(R.layout.activity_main_header, binding.listView, false)
        val footerLayout = layoutInflater.inflate(R.layout.activity_main_footer, binding.listView, false)

        binding.listView.addHeaderView(headerLayout)
        binding.listView.addFooterView(footerLayout)

        // Create list view adapter
        binding.listView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.activity_main_implementations_array)
        )

        binding.listView.setOnItemClickListener { _, _, index, _ ->
            when (index) {
                1 -> BannerActivity::class
                2 -> BiddingBannerActivity::class
                3 -> BannerInListActivity::class
                4 -> InterstitialActivity::class
                5 -> BiddingInterstitialActivity::class
                6 -> NativeAdInListActivity::class
                else -> null
            }?.let { activityClass ->
                val intent = Intent(this, activityClass.java)

                startActivity(intent)
            }
        }
    }
}