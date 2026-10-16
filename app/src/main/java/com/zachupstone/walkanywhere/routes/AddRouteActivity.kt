package com.zachupstone.walkanywhere.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.zachupstone.walkanywhere.map.AlertDialog
import com.zachupstone.walkanywhere.map.SearchBar
import com.zachupstone.walkanywhere.ui.theme.Grey80
import com.zachupstone.walkanywhere.viewmodel.AddRouteViewModel

@Composable
fun AddRoute(addRouteViewModel: AddRouteViewModel, onClose: () -> Unit) {
    // Default camera positon
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 15f)
    }

    val context = LocalContext.current

    var firstMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    var secondMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    val route by addRouteViewModel.routePolyline

    val openAlertDialog = remember { mutableStateOf(false) }
    val errorDialogMessage by addRouteViewModel.directionsError

    val cameraLocation by addRouteViewModel.cameraLocation

    LaunchedEffect(cameraLocation) {
        val bounds = cameraLocation

        if(bounds != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(bounds, 15f)
            cameraPositionState.animate(cameraUpdate)
        }
    }

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
                    state = rememberUpdatedMarkerState(position = marker),
                    title = "Start of route",
                )
            }
            secondMarkerPosition?.let { marker ->
                Marker(
                    state = rememberUpdatedMarkerState(position = marker),
                    title = "End of route",
                )
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

        Column {
            SearchBar(
                onPlaceSelected = { place ->
                    // When a place is selected from the search bar, update the selected location
                    addRouteViewModel.selectLocation(place, context)
                }
            )

            Spacer(Modifier.weight(1f))

            if (route != null) {
                Button({
                    addRouteViewModel.saveRoute(context)
                    onClose()
                }, Modifier.fillMaxWidth()) {
                    Text("Save")
                }
            }

            Row(Modifier.fillMaxWidth().padding(5.dp).background(Grey80,RoundedCornerShape(16.dp)).padding(10.dp)) {
                Text("Tap on the map to set two points", fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(12.dp))
        }


    }
}