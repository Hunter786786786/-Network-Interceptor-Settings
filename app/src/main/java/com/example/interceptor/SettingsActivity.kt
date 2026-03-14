package com.example.interceptor

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.interceptor.databinding.ActivitySettingsBinding
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("interceptor_prefs", MODE_PRIVATE)
        
        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        binding.switchMaster.setOnCheckedChangeListener { _, isChecked ->
            saveBoolean("master_switch", isChecked)
            showToast(if (isChecked) "ON" else "OFF")
        }

        binding.seekbarPickup.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val distance = progress / 10.0f
                binding.tvPickupValue.text = String.format("%.1fkm", distance)
                if (fromUser) saveFloat("max_pickup_distance", distance)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarDelivery.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val distance = progress / 10.0f
                binding.tvDeliveryValue.text = String.format("%.1fkm", distance)
                if (fromUser) saveFloat("max_delivery_distance", distance)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnSave.setOnClickListener {
            makeWorldReadable()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        val masterOn = prefs.getBoolean("master_switch", false)
        binding.switchMaster.isChecked = masterOn

        val pickupDist = prefs.getFloat("max_pickup_distance", 5.0f)
        binding.seekbarPickup.progress = (pickupDist * 10).toInt()
        binding.tvPickupValue.text = String.format("%.1fkm", pickupDist)

        val deliveryDist = prefs.getFloat("max_delivery_distance", 10.0f)
        binding.seekbarDelivery.progress = (deliveryDist * 10).toInt()
        binding.tvDeliveryValue.text = String.format("%.1fkm", deliveryDist)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        makeWorldReadable()
    }

    private fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
        makeWorldReadable()
    }

    private fun makeWorldReadable() {
        try {
            val prefsFile = File(filesDir.parent, "shared_prefs/interceptor_prefs.xml")
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false)
                prefsFile.parentFile?.setExecutable(true, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
