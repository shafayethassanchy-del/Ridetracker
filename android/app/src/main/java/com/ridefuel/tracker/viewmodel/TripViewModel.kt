package com.ridefuel.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val fuelLiters: Double = 0.0,
    val fuelPrice: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerKm: Double = 0.0
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
     * Calculates the fuel cost metrics.
     */
    fun calculateMetrics(liters: Double, price: Double, distanceKm: Double): Pair<Double, Double> {
        val totalCost = liters * price
        val costPerKm = if (distanceKm > 0) totalCost / distanceKm else 0.0
        return Pair(totalCost, costPerKm)
    }
}
