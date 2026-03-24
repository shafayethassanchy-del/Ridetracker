package com.ridefuel.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: Date = Date(),
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val fuelLiters: Double = 0.0,
    val fuelPrice: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerKm: Double = 0.0,
    val polyline: List<Map<String, Double>> = emptyList()
)

data class UserProfile(
    val uid: String = "",
    val bikeName: String = "",
    val mileage: Double = 0.0
)

class TripViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Saves the completed trip to Firebase Firestore.
     */
    fun saveTrip(trip: Trip, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            db.collection("trips")
                .document(trip.id)
                .set(trip)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    /**
     * Saves the user's bike profile to Firestore.
     */
    fun saveProfile(profile: UserProfile, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            db.collection("users")
                .document(profile.uid)
                .set(profile)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    /**
     * Fetches the user's bike profile from Firestore.
     */
    fun getProfile(uid: String, onResult: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    onResult(document.toObject(UserProfile::class.java))
                }
                .addOnFailureListener { onResult(null) }
        }
    }

    /**
     * Calculates the fuel cost metrics.
     */
    fun calculateMetrics(liters: Double, price: Double, distanceKm: Double): Pair<Double, Double> {
        val totalCost = liters * price
        val costPerKm = if (distanceKm > 0) totalCost / distanceKm else 0.0
        return Pair(totalCost, costPerKm)
    }

    /**
     * Checks if fuel is low based on mileage and distance.
     * @param initialLiters Fuel at start
     * @param distanceKm Distance traveled
     * @param mileageKmPerLiter Bike's mileage
     * @param thresholdLiters Alert threshold (default 1.0L)
     * @return True if estimated remaining fuel is below threshold
     */
    fun isFuelLow(
        initialLiters: Double,
        distanceKm: Double,
        mileageKmPerLiter: Double,
        thresholdLiters: Double = 1.0
    ): Boolean {
        if (mileageKmPerLiter <= 0) return false
        val fuelConsumed = distanceKm / mileageKmPerLiter
        val remainingFuel = initialLiters - fuelConsumed
        return remainingFuel < thresholdLiters
    }
}
