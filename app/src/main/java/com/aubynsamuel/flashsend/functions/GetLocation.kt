package com.aubynsamuel.flashsend.functions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

fun getCurrentLocation(
    context: Context,
    onLocationResult: (latitude: Double?, longitude: Double?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onLocationResult(null, null)
        return
    }
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(location.latitude, location.longitude)
            } else {
                onLocationResult(null, null)
            }
        }
        .addOnFailureListener {
            onLocationResult(null, null)
        }
}
