package com.ridefuel.tracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.ridefuel.tracker.R
import com.ridefuel.tracker.databinding.ActivityMapBinding
import com.ridefuel.tracker.service.TrackingService

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private var map: GoogleMap? = null
    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupClickListeners()
        observeTrackingData()
        requestPermissions()
    }

    private fun setupClickListeners() {
        binding.btnStartStop.setOnClickListener {
            if (isTracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }
    }

    private fun startTracking() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = "ACTION_START_TRACKING"
        }
        startService(intent)
        binding.btnStartStop.text = "Stop Trip"
        binding.btnStartStop.setBackgroundColor(Color.RED)
    }

    private fun stopTracking() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = "ACTION_STOP_TRACKING"
        }
        startService(intent)
        binding.btnStartStop.text = "Start Trip"
        binding.btnStartStop.setBackgroundColor(Color.parseColor("#FFC107"))
        
        // Navigate to Summary
        startActivity(Intent(this, SummaryActivity::class.java))
    }

    private fun observeTrackingData() {
        TrackingService.isTracking.observe(this) { tracking ->
            isTracking = tracking
        }

        TrackingService.pathPoints.observe(this) { points ->
            if (points.isNotEmpty()) {
                val latLngs = points.map { LatLng(it.latitude, it.longitude) }
                drawPolyline(latLngs)
                
                // Move camera to latest position
                val latest = latLngs.last()
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latest, 15f))
            }
        }

        TrackingService.totalDistance.observe(this) { distance ->
            val km = distance / 1000f
            binding.tvDistance.text = String.format("%.2f km", km)
        }
    }

    private fun drawPolyline(points: List<LatLng>) {
        map?.clear()
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .color(Color.parseColor("#FFC107"))
            .width(12f)
        map?.addPolyline(polylineOptions)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
    }
}
