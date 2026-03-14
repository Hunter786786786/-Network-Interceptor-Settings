pickupDistanceInKm = payload.optDouble("pickupDistanceInKm", 0.0),
                deliveryDistanceInKm = payload.optDouble("deliveryDistanceInKm", 0.0),
                restaurantName = payload.optString("restaurantName", ""),
                payout = payload.optDouble("payout", 0.0),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            YLog.e("Failed to parse order data: ${e.message}")
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
            YLog.i("Master switch is OFF")
            return false
        }

        // Get distance limits
        val maxPickup = prefs.getFloat("max_pickup_distance", 10.0f).toDouble()
        val maxDelivery = prefs.getFloat("max_delivery_distance", 15.0f).toDouble()

        // Check criteria - FIX: Explicit Double comparison
        val pickupOk = order.pickupDistanceInKm <= maxPickup
        val deliveryOk = order.deliveryDistanceInKm <= maxDelivery

        YLog.i("Criteria Check:")
        YLog.i("  Pickup: ${order.pickupDistanceInKm}km <= ${maxPickup}km → $pickupOk")
        YLog.i("  Delivery: ${order.deliveryDistanceInKm}km <= ${maxDelivery}km → $deliveryOk")

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
