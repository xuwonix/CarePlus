package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.DiagnosticCenter
import com.example.data.model.NearbyFacility
import com.example.data.model.Specialist
import com.example.data.repository.HealthcareCatalog

object MapsNavigationHelper {

    /**
     * Launches Google Maps Directions using the official Google Maps Platform Maps URLs
     * Directions format (`https://www.google.com/maps/dir/?api=1&destination=...&travelmode=driving`).
     * Prefers the native Google Maps app when installed and gracefully falls back to browser/maps.
     */
    fun openDirections(
        context: Context,
        placeName: String,
        address: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        val destinationQuery = buildString {
            if (placeName.isNotBlank()) append(placeName.trim())
            if (address.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(address.trim())
            }
        }
        val destinationParam = if (latitude != 0.0 && longitude != 0.0 && destinationQuery.isBlank()) {
            "$latitude,$longitude"
        } else if (destinationQuery.isNotBlank()) {
            destinationQuery
        } else {
            "Tezpur, Assam"
        }

        val mapsDirectionsUri = Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .appendPath("maps")
            .appendPath("dir")
            .appendPath("")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("destination", destinationParam)
            .appendQueryParameter("travelmode", "driving")
            .build()

        // 1. Try native Google Maps app directly for turn-by-turn directions
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, mapsDirectionsUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(googleMapsIntent)
            return
        } catch (_: ActivityNotFoundException) {
            // Fall through to generic Maps / Web browser handler
        }

        // 2. Fallback to geo: URI or HTTPS Google Maps URL in browser
        val fallbackIntent = Intent(Intent.ACTION_VIEW, mapsDirectionsUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(fallbackIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No Maps app or browser found to open directions for $placeName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openFacilityDirections(context: Context, facility: NearbyFacility) {
        openDirections(
            context = context,
            placeName = facility.name,
            address = facility.address,
            latitude = facility.latitude,
            longitude = facility.longitude
        )
    }

    fun openDiagnosticCenterDirections(context: Context, center: DiagnosticCenter) {
        openDirections(
            context = context,
            placeName = center.name,
            address = center.address,
            latitude = center.latitude,
            longitude = center.longitude
        )
    }

    fun openClinicDirections(context: Context, specialist: Specialist) {
        val linkedFacility = HealthcareCatalog
            .getNearbyHospitalsAndCenters()
            .firstOrNull { it.id == specialist.hospitalId }
        openDirections(
            context = context,
            placeName = specialist.clinicHospital,
            address = specialist.address,
            latitude = linkedFacility?.latitude ?: 0.0,
            longitude = linkedFacility?.longitude ?: 0.0
        )
    }
}
