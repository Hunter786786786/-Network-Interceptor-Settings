Log.i("XposedHook", "Criteria Check:")
        Log.i("XposedHook", "  Pickup: ${order.pickupDistanceInKm}km <= ${maxPickup}km = $pickupOk")
        Log.i("XposedHook", "  Delivery: ${order.deliveryDistanceInKm}km <= ${maxDelivery}km = $deliveryOk")

        return pickupOk && deliveryOk
    }
}

data class OrderData(
    val id: String,
    val pickupDistanceInKm: Double,
    val deliveryDistanceInKm: Double,
    val restaurantName: String,
    val payout: Double,
    val timestamp: Long = System.currentTimeMillis()
)
