package com.example.interceptor

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * Xposed Hook Module for Network Interception
 * 
 * This module demonstrates:
 * - OkHttp WebSocket interception
 * - Socket.io event handling
 * - Background processing with coroutines
 * - XSharedPreferences for configuration
 * 
 * Educational Purpose Only
 */
@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    // Coroutine scope for background operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Shared preferences for settings
    private lateinit var prefs: XSharedPreferences
    
    // Target package name
    private val TARGET_PACKAGE = "delivery.samurai.android"

    override fun onInit() = configs {
        debugLog {
            tag = "NetworkInterceptor"
        }
        isDebug = false
    }

    override fun onHook() = encase {
        // App lifecycle hook - initialize when app starts
        onAppLifecycle {
            attachApplication {
                // Initialize XSharedPreferences
                prefs = XSharedPreferences("com.example.interceptor", "interceptor_prefs")
                prefs.makeWorldReadable()
                YukiHookAPI.loggerI("=== Network Interceptor Loaded ===")
                YukiHookAPI.loggerI("Target: $packageName")
            }
        }

        // Hook 1: OkHttp WebSocket Listener
        // Intercepts WebSocket messages before they reach the UI
        findClass("okhttp3.WebSocketListener").hook {
            method {
                name = "onMessage"
                param("okhttp3.WebSocket", StringClass)
            }.hookAfter { param ->
                val message = param.args[1] as String
                handleWebSocketMessage(message)
            }
        }

        // Hook 2: Socket.io Emitter
        // Intercepts Socket.io events
        findClass("io.socket.emitter.Emitter").hook {
            method {
                name = "on"
                param(StringClass, "io.socket.emitter.Emitter\$Listener")
            }.hookAfter { param ->
                val event = param.args[0] as String
                if (event == "Allocation" || event == "New Order") {
                    YukiHookAPI.loggerI("Socket.io event detected: $event")
                }
            }
        }

        // Hook 3: Alternative - OkHttp Response Body
        // For HTTP responses instead of WebSocket
        findClass("okhttp3.Response").hook {
            method {
                name = "body"
            }.hookAfter { param ->
                // Can intercept HTTP response bodies here
            }
        }
    }

    /**
     * Handle incoming WebSocket message
     * Runs on background thread to avoid blocking UI
     */
    private fun handleWebSocketMessage(message: String) {
        scope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                // Parse JSON message
                val json = JSONObject(message)
                val eventType = json.optString("event", "")
                
                // Check for order events
                if (eventType == "Allocation" || eventType == "New Order") {
                    YukiHookAPI.loggerI("📦 Order event detected: $eventType")
                    
                    // Extract order data
                    val orderData = extractOrderData(json)
                    
                    if (orderData != null) {
                        YukiHookAPI.loggerI("Order ID: ${orderData.id}")
                        YukiHookAPI.loggerI("Pickup: ${orderData.pickupDistanceInKm}km")
                        YukiHookAPI.loggerI("Delivery: ${orderData.deliveryDistanceInKm}km")
                        
                        // Check criteria
                        if (shouldAcceptOrder(orderData)) {
                            YukiHookAPI.loggerI("✅ Order meets criteria: ${orderData.id}")
                            // Educational: In real implementation, accept order here
                        } else {
                            YukiHookAPI.loggerI("❌ Order does not meet criteria")
                        }
                    }
                }
                
                val executionTime = System.currentTimeMillis() - startTime
                YukiHookAPI.loggerI("⏱️ Processing time: ${executionTime}ms")
                
            } catch (e: Exception) {
                YukiHookAPI.loggerE("Error processing message: ${e.message}")
            }
        }
    }

    /**
     * Extract order data from JSON payload
     */
    private fun extractOrderData(json: JSONObject): OrderData? {
        return try {
            // Try to get payload, fallback to root if not present
            val payload = json.optJSONObject("payload") ?: json
            
            OrderData(
                id = payload.getString("id"),
                pickupDistanceInKm = payload.optDouble("pickupDistanceInKm", 0.0),
                deliveryDistanceInKm = payload.optDouble("deliveryDistanceInKm", 0.0),
                restaurantName = payload.optString("restaurantName", ""),
                payout = payload.optDouble("payout", 0.0),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            YukiHookAPI.loggerE("Failed to parse order data: ${e.message}")
            null
        }
    }

    /**
     * Check if order meets user-defined criteria
     */
    private fun shouldAcceptOrder(order: OrderData): Boolean {
        // Reload preferences to get latest values
        prefs.reload()
        
        // Check master switch
        val masterSwitch = prefs.getBoolean("master_switch", false)
        if (!masterSwitch) {
            YukiHookAPI.loggerI("Master switch is OFF")
            return false
        }

        // Get distance limits
        val maxPickup = prefs.getFloat("max_pickup_distance", 10.0f).toDouble()
        val maxDelivery = prefs.getFloat("max_delivery_distance", 15.0f).toDouble()

        // Check criteria
        val pickupOk = order.pickupDistanceInKm <= maxPickup
        val deliveryOk = order.deliveryDistanceInKm <= maxDelivery

        YukiHookAPI.loggerI("Criteria Check:")
        YukiHookAPI.loggerI("  Pickup: ${order.pickupDistanceInKm}km <= ${maxPickup}km → $pickupOk")
        YukiHookAPI.loggerI("  Delivery: ${order.deliveryDistanceInKm}km <= ${maxDelivery}km → $deliveryOk")

        return pickupOk && deliveryOk
    }
}

/**
 * Data class for order information
 */
data class OrderData(
    val id: String,
    val pickupDistanceInKm: Double,
    val deliveryDistanceInKm: Double,
    val restaurantName: String,
    val payout: Double,
    val timestamp: Long = System.currentTimeMillis()
)
