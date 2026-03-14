val content = readUtf8Method.invoke(clonedBuffer) as String
            
            if (content.isNotEmpty()) {
                YLog.i("Response Body Preview: ${content.take(500)}")
                parseAndLogJson(content)
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    private fun parseAndLogJson(content: String) {
        try {
            val json = JSONObject(content)
            val keys = json.keys()
            
            YLog.i("JSON Structure:")
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                val type = value?.javaClass?.simpleName ?: "null"
                YLog.i("  $key: $type")
            }
            
            if (json.has("payload") || json.has("id")) {
                processOrderData(json)
            }
        } catch (e: Exception) {
            // Not valid JSON
        }
    }

    private fun processOrderData(json: JSONObject) {
        val startTime = System.currentTimeMillis()
        
        try {
            val orderData = extractOrderData(json)
            
            orderData?.let { data ->
                YLog.i("Order Detected:")
                YLog.i("ID: ${data.id}")
                YLog.i("Restaurant: ${data.restaurantName}")
                YLog.i("Payout: ${data.payout}")
                YLog.i("Pickup: ${data.pickupDistanceInKm}km")
                YLog.i("Delivery: ${data.deliveryDistanceInKm}km")
                
                if (shouldAcceptOrder(data)) {
                    YLog.i("Order meets criteria: ${data.id}")
                } else {
                    YLog.i("Order does not meet criteria")
                }
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            YLog.i("Processing time: ${executionTime}ms")
            
        } catch (e: Exception) {
            YLog.e("Error processing message: ${e.message}")
        }
    }

    private fun extractOrderData(json: JSONObject): OrderData? {
        return try {
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
            YLog.e("Failed to parse order data: ${e.message}")
            null
        }
    }

    private fun shouldAcceptOrder(order: OrderData): Boolean {
        val currentPrefs = prefs ?: return false
        currentPrefs.reload()
        
        val masterSwitch = currentPrefs.getBoolean("master_switch", false)
        if (!masterSwitch) {
            YLog.i("Master switch is OFF")
            return false
        }

        val maxPickup = currentPrefs.getFloat("max_pickup_distance", 10.0f).toDouble()
        val maxDelivery = currentPrefs.getFloat("max_delivery_distance", 15.0f).toDouble()

        val pickupOk = order.pickupDistanceInKm <= maxPickup
        val deliveryOk = order.deliveryDistanceInKm <= maxDelivery

        YLog.i("Criteria Check:")
        YLog.i("  Pickup: ${order.pickupDistanceInKm}km <= ${maxPickup}km = $pickupOk")
        YLog.i("  Delivery: ${order.deliveryDistanceInKm}km <= ${maxDelivery}km = $deliveryOk")

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
