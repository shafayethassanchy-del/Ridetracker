package com.ridefuel.tracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ridefuel.tracker.databinding.ActivitySummaryBinding
import com.ridefuel.tracker.service.TrackingService
import com.ridefuel.tracker.viewmodel.Trip
import com.ridefuel.tracker.viewmodel.TripViewModel

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding
    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        displayStats()
        setupSaveButton()
    }

    private fun displayStats() {
        val distanceMeters = TrackingService.totalDistance.value ?: 0f
        val distanceKm = distanceMeters / 1000.0
        
        binding.tvTotalDistance.text = String.format("%.2f km", distanceKm)
        
        // Initial calculation with default fuel values
        updateCalculations(distanceKm)
    }

    private fun updateCalculations(distanceKm: Double) {
        val liters = binding.etLiters.text.toString().toDoubleOrNull() ?: 0.0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        
        val (totalCost, costPerKm) = viewModel.calculateMetrics(liters, price, distanceKm)
        
        binding.tvTotalCost.text = String.format("৳%.2f", totalCost)
        binding.tvCostPerKm.text = String.format("৳%.2f/km", costPerKm)
    }

    private fun setupSaveButton() {
        binding.btnSaveTrip.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val distanceKm = (TrackingService.totalDistance.value ?: 0f) / 1000.0
            val liters = binding.etLiters.text.toString().toDoubleOrNull() ?: 0.0
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val (totalCost, costPerKm) = viewModel.calculateMetrics(liters, price, distanceKm)

            // Convert path points to a list of maps for Firestore
            val pathPoints = TrackingService.pathPoints.value?.map {
                mapOf("lat" to it.latitude, "lng" to it.longitude)
            } ?: emptyList()

            val trip = Trip(
                userId = userId,
                distanceKm = distanceKm,
                fuelLiters = liters,
                fuelPrice = price,
                totalCost = totalCost,
                costPerKm = costPerKm,
                polyline = pathPoints
            )

            viewModel.saveTrip(trip) { success ->
                if (success) {
                    Toast.makeText(this, "Trip Saved Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
