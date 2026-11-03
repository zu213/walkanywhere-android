package com.zachupstone.walkanywhere.ui.routes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
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
import com.zachupstone.walkanywhere.ui.map.AlertDialog
import com.zachupstone.walkanywhere.ui.map.SearchBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AddRoute(addRouteViewModel: AddRouteViewModel, onClose: () -> Unit) {
    // Default camera positon
    val greenwich = LatLng(51.4779, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(greenwich, 0f)
    }

    var firstMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    var secondMarkerPosition: LatLng? by remember {mutableStateOf(null)}
    val route by addRouteViewModel.routePolyline

    val openAlertDialog = remember { mutableStateOf(false) }
    val errorDialogMessage by addRouteViewModel.directionsError

    val cameraLocation by addRouteViewModel.cameraLocation

    LaunchedEffect(cameraLocation) {
        val bounds = cameraLocation

        if(bounds != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(bounds, 12f)
            cameraPositionState.animate(cameraUpdate)
            if(firstMarkerPosition == null) {
                firstMarkerPosition = bounds
            } else if(secondMarkerPosition == null) {
                secondMarkerPosition = bounds
                addRouteViewModel.addRoute(
                    firstMarkerPosition!!,
                    secondMarkerPosition!!
                )
            }
        }
    }

    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    var searchCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(searchCount) {          // bump this when a search finishes
        if (searchCount == 0) return@LaunchedEffect

        launch {
            scale.animateTo(1.15f, tween(200))
            delay(1000.milliseconds)
            scale.animateTo(1f, tween(200))
        }
        val angles = listOf(-5f, 5f, -4f, 4f, -4f, 4f, -3f, 3f, 0f)
        for (angle in angles) {
            rotation.animateTo(angle, tween(durationMillis = 150, easing = FastOutSlowInEasing))
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
                val noRoutesFoundError = errorMessage == "No routes found (ZERO_RESULTS)"
                AlertDialog(
                    onDismissRequest = {
                        firstMarkerPosition = null
                        secondMarkerPosition = null
                        addRouteViewModel.clearDirections()
                        addRouteViewModel.clearDirectionsError()
                    },
                    onConfirmation = null,
                    dialogTitle = if(noRoutesFoundError) "No Routes Found" else "Error",
                    dialogText = if(noRoutesFoundError) "Please pick a different route" else errorMessage,
                    icon = Icons.Default.Build
                )
            }
        }

        Column {
            SearchBar(
                onPlaceSelected = { place ->
                    // When a place is selected from the search bar, update the selected location
                    addRouteViewModel.selectLocation(place)
                    searchCount++
                },
            )

            Spacer(Modifier.weight(1f))

            if (route != null) {
                Button({
                    addRouteViewModel.saveRoute()
                    onClose()
                }, Modifier.fillMaxWidth()) {
                    Text("Save")
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        rotationZ = rotation.value
                    }
                    .shadow(8.dp, RoundedCornerShape(16.dp))                                       // lifts it off the map
                    .background(MaterialTheme.colorScheme.primaryContainer,RoundedCornerShape(16.dp))
                    .padding(10.dp)

            ) {
                Text(
                    "Tap on the map to set two points",
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(12.dp))
        }


    }
}