package com.aubynsamuel.flashsend.functions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

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
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        cancellationTokenSource.token
    )
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
