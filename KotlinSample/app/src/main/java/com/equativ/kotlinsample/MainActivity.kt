package com.equativ.kotlinsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.equativ.displaysdk.util.SASConfiguration
import com.equativ.kotlinsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val activityClass = when (index) {
                2 -> BannerInListActivity::class
                3 -> InterstitialActivity::class
                else -> BannerActivity::class
            }

            val intent = Intent(this, activityClass.java)

            startActivity(intent)
        }
    }
}