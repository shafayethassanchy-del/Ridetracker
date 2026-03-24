package com.ridefuel.tracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.ridefuel.tracker.databinding.ActivityProfileBinding
import com.ridefuel.tracker.viewmodel.TripViewModel
import com.ridefuel.tracker.viewmodel.UserProfile

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: TripViewModel
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        loadProfile()
        setupSaveButton()
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return
        viewModel.getProfile(userId) { profile ->
            profile?.let {
                binding.etBikeName.setText(it.bikeName)
                binding.etMileage.setText(it.mileage.toString())
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveProfile.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bikeName = binding.etBikeName.text.toString()
            val mileage = binding.etMileage.text.toString().toDoubleOrNull() ?: 0.0

            if (bikeName.isEmpty() || mileage <= 0) {
                Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = UserProfile(uid = userId, bikeName = bikeName, mileage = mileage)
            viewModel.saveProfile(profile) { success ->
                if (success) {
                    Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
