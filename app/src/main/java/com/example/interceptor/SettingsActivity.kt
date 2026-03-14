package com.example.interceptor

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.interceptor.databinding.ActivitySettingsBinding

/**
 * Settings Activity for Network Interceptor
 * 
 * Provides UI for:
 * - Master switch (enable/disable interception)
 * - Max pickup distance slider
 * - Max delivery distance slider
 * - Save settings button
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize preferences with world-readable mode for Xposed
        prefs = getSharedPreferences("interceptor_prefs", MODE_WORLD_READABLE)
        
        setupUI()
        loadSettings()
    }

    /**
     * Setup UI listeners
     */
    private fun setupUI() {
        // Master switch toggle
        binding.switchMaster.setOnCheckedChangeListener { _, isChecked ->
            saveBoolean("master_switch", isChecked)
            showToast(if (isChecked) "✅ Interceptor ON" else "❌ Interceptor OFF")
        }

        // Pickup distance slider
        binding.seekbarPickup.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val distance = progress / 10.0f  // 0.1km precision
                binding.tvPickupValue.text = String.format("%.1fkm", distance)
                saveFloat("max_pickup_distance", distance)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Delivery distance slider
        binding.seekbarDelivery.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val distance = progress / 10.0f
                binding.tvDeliveryValue.text = String.format("%.1fkm", distance)
                saveFloat("max_delivery_distance", distance)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save button
        binding.btnSave.setOnClickListener {
            makeWorldReadable()
            Toast.makeText(this, "✅ Settings Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Load saved settings into UI
     */
    private fun loadSettings() {
        // Master switch (default: OFF)
        val masterOn = prefs.getBoolean("master_switch", false)
        binding.switchMaster.isChecked = masterOn

        // Pickup distance (default: 5km, range: 0-10km)
        val pickupDist = prefs.getFloat("max_pickup_distance", 5.0f)
        binding.seekbarPickup.progress = (pickupDist * 10).toInt()
        binding.tvPickupValue.text = String.format("%.1fkm", pickupDist)

        // Delivery distance (default: 10km, range: 0-15km)
        val deliveryDist = prefs.getFloat("max_delivery_distance", 10.0f)
        binding.seekbarDelivery.progress = (deliveryDist * 10).toInt()
        binding.tvDeliveryValue.text = String.format("%.1fkm", deliveryDist)
    }

    /**
     * Save boolean preference
     */
    private fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        makeWorldReadable()
    }

    /**
     * Save float preference
     */
    private fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
        makeWorldReadable()
    }

    /**
     * Make preferences file world-readable for Xposed module access
     * This is required for XSharedPreferences to work
     */
    private fun makeWorldReadable() {
        try {
            val dataDir = applicationInfo.dataDir
            val prefsDir = java.io.File(dataDir, "shared_prefs")
            val prefsFile = java.io.File(prefsDir, "interceptor_prefs.xml")
            
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false)
                prefsDir.setExecutable(true, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
