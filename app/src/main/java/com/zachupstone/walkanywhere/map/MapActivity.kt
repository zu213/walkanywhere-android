package com.zachupstone.walkanywhere.map

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.MapViewModel
import timber.log.Timber

@Composable
fun MainRoute(mapViewModel: MapViewModel) {
    // Default camera positon
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val openAlertDialog = remember { mutableStateOf(false) }
    var firstMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    var secondMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    val route by mapViewModel.routePolyline

    val errorDialogMessage by mapViewModel.directionsError

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        onMapClick = { latLng ->
            // Captures where you click on the map
            if(firstMarkerPosition == null) {
                firstMarkerPosition = latLng
            } else if(secondMarkerPosition == null) {
                secondMarkerPosition = latLng
                mapViewModel.fetchDirections(context, firstMarkerPosition!!, secondMarkerPosition!!)
            } else {
                openAlertDialog.value = true
            }
        },
        cameraPositionState = cameraPositionState

    ) {
        firstMarkerPosition?.let { marker ->
            Marker(
                state = MarkerState(position = marker),
                title = "Start of route",
            )
            cameraPositionState.position = CameraPosition.fromLatLngZoom(marker, 10f)
        }
        secondMarkerPosition?.let { marker ->
            Marker(
                state = MarkerState(position = marker),
                title = "End of route",
            )
            cameraPositionState.position = CameraPosition.fromLatLngZoom(marker, 10f)
        }
        route?.let { routePoints ->
            Polyline(
                points = routePoints
            )
        }

        when {
            openAlertDialog.value -> {
                AlertDialog(
                    onDismissRequest = { openAlertDialog.value = false },
                    onConfirmation = {
                        openAlertDialog.value = false
                        firstMarkerPosition = null
                        secondMarkerPosition = null
                        mapViewModel.clearDirections()
                    },
                    dialogTitle = "Delete existing route",
                    dialogText = "Delete existing route",
                    icon = Icons.Default.Info
                )
            }
        }

        errorDialogMessage?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = {
                    firstMarkerPosition = null
                    secondMarkerPosition = null
                    mapViewModel.clearDirections()
                    mapViewModel.clearDirectionsError()
                },
                onConfirmation = null,
                dialogTitle = "Error",
                dialogText = "e $errorMessage ss",
                icon = Icons.Default.Build
            )
        }
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Fetch the user's location and update the camera if permission is granted
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        } else {
            // Handle the case when permission is denied
            Timber.e("Location permission was denied by the user.")
        }
    }

// Request the location permission when the composable is launched
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            // Check if the location permission is already granted
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Fetch the user's location and update the camera
                mapViewModel.fetchUserLocation(context, fusedLocationClient)
            }
            else -> {
                // Request the location permission if it has not been granted
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val mapViewModel = MapViewModel()
        MainRoute(mapViewModel)
    }
}