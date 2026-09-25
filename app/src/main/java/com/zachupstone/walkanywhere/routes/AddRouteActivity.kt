package com.zachupstone.walkanywhere.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.zachupstone.walkanywhere.map.AlertDialog
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.AddRouteViewModel

@Composable
fun AddRoute(addRouteViewModel: AddRouteViewModel, onClose: () -> Unit) {
    // Default camera positon
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    val context = LocalContext.current

    var firstMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    var secondMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    val route by addRouteViewModel.routePolyline

    val openAlertDialog = remember { mutableStateOf(false) }
    val errorDialogMessage by addRouteViewModel.directionsError

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapClick = { latLng ->
                // Captures where you click on the map
                if (firstMarkerPosition == null) {
                    firstMarkerPosition = latLng
                } else if (secondMarkerPosition == null) {
                    secondMarkerPosition = latLng
                    addRouteViewModel.addRoute(
                        context,
                        firstMarkerPosition!!,
                        secondMarkerPosition!!
                    )
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
                            addRouteViewModel.clearDirections()
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
                        addRouteViewModel.clearDirections()
                        addRouteViewModel.clearDirectionsError()
                    },
                    onConfirmation = null,
                    dialogTitle = "Error",
                    dialogText = "e $errorMessage ss",
                    icon = Icons.Default.Build
                )
            }
        }

        if (route != null) {
            Button({
                addRouteViewModel.saveRoute(context)
                onClose()
            }) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddRoutePreview() {
    WalkAnywhereTheme {
        val addRouteViewModel = AddRouteViewModel()
        AddRoute(addRouteViewModel, {})
    }
}