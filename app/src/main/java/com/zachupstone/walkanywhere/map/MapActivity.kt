package com.zachupstone.walkanywhere.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.zachupstone.walkanywhere.data.RouteWithSteps
import com.zachupstone.walkanywhere.ui.theme.Grey40
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.MapViewModel

@Composable
fun MainRoute(mapViewModel: MapViewModel, routeData: RouteWithSteps?) {
    // Default camera positon
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    val context = LocalContext.current

    val firstMarkerPosition by mapViewModel.origin
    val secondMarkerPosition by mapViewModel.destination
    val userLocation by mapViewModel.userLocation
    val route by mapViewModel.routePolyline
    val cameraLocation by mapViewModel.cameraLocation
    val routeId by mapViewModel.routeId
    val stepsSoFar by mapViewModel.stepsSoFar
    val percentWalked by mapViewModel.routePercent

    LaunchedEffect(Unit) {
        if(routeData == null) {
            mapViewModel.setRoute(context)
        } else {
            mapViewModel.setRoute(routeData)
        }
    }

    LaunchedEffect(cameraLocation) {
        if(cameraLocation != null) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(cameraLocation!!, 100))
        } else if(firstMarkerPosition != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(firstMarkerPosition!!, 10f)
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
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

            userLocation?.let { marker ->
                Marker(
                    state = rememberUpdatedMarkerState(position = marker),
                    title = "User location",
                )
            }
            route?.let { routePoints ->
                Polyline(
                    points = routePoints
                )
            }
        }

        Column {
            Spacer(Modifier.weight(1f))

            if(userLocation != null && secondMarkerPosition != null && userLocation == secondMarkerPosition && routeId != null) {
                Button({
                    mapViewModel.resetRoute(context, routeId!!)
                }, Modifier.fillMaxWidth()) {
                    Text("Reset route")
                }
            }

            if(route != null) {
                Row(
                    Modifier.fillMaxWidth().padding(5.dp).background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    ).padding(15.dp)
                ) {
                    Column(Modifier.padding(1.dp)) {
                        if (percentWalked != null) {

                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    "ROUTE $routeId",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            LinearProgressIndicator(
                                progress = { percentWalked!! },
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 17.dp, bottom = 1.dp).height(16.dp),
                                color = Color.Blue,
                                trackColor = Color.LightGray
                            )

                            stepsSoFar?.let {
                                val totalSteps = (it / percentWalked!!).toInt()
                                Row(Modifier.fillMaxWidth()) {
                                    if (stepsSoFar == 0) {
                                        Text("No Steps Tracked Yet!", fontSize = 8.sp)
                                    } else {
                                        Text(
                                            "$stepsSoFar/$totalSteps Steps Walked",
                                            fontSize = 8.sp
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text("%.2f%%".format(percentWalked!! * 100), fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(5.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val mapViewModel: MapViewModel = viewModel()
        MainRoute(mapViewModel, null)
    }
}