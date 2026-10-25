package com.zachupstone.walkanywhere.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zachupstone.walkanywhere.R.drawable.ic_directions_walk
import com.zachupstone.walkanywhere.data.RouteWithSteps
import com.zachupstone.walkanywhere.map.MainRoute
import com.zachupstone.walkanywhere.ui.theme.LightSoftBlue
import com.zachupstone.walkanywhere.ui.theme.PaleBlue
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.AddRouteViewModel
import com.zachupstone.walkanywhere.viewmodel.MapViewModel
import com.zachupstone.walkanywhere.viewmodel.RoutesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Routes(routesViewModel: RoutesViewModel) {

    val context = LocalContext.current
    val routes by routesViewModel.routes
    val loading by routesViewModel.loading
    val showNewRouteModal = remember { mutableStateOf(false) }
    val addRouteViewModel: AddRouteViewModel = viewModel()

    val mapViewModel: MapViewModel = viewModel()
    val selectedRouteToView = remember { mutableStateOf<RouteWithSteps?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    fun onDismissNewRouteModal() {
        routesViewModel.fetchAllRoutes(context)
        showNewRouteModal.value = false
    }

    LaunchedEffect(Unit) {
        routesViewModel.fetchAllRoutes(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("My Routes")
                },
                actions = {
                    IconButton(onClick = {
                        showNewRouteModal.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Route"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
         LazyColumn(Modifier.fillMaxSize()
             .padding(innerPadding)
             .padding(16.dp)
         ) {
             if (routes != null && routes!!.isNotEmpty()) {
                 for ((_, routeWithInfo) in routes!!.withIndex()) {
                     item {
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                             Button(
                                 {
                                     routesViewModel.favouriteRoute(context, routeWithInfo.routeWithSteps.route.routeId)
                                 },
                                 colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
                             ) {
                                 Icon(
                                     imageVector = if (routeWithInfo.routeWithSteps.route.selected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                     contentDescription = if (routeWithInfo.routeWithSteps.route.selected) "unfavourite" else "favourite",
                                     modifier = Modifier.size(32.dp),
                                     tint = if(routeWithInfo.routeWithSteps.route.selected) Color.Red else Color.Gray
                                 )
                             }

                             Column {

                                 Button(
                                     onClick = {
                                         selectedRouteToView.value = routeWithInfo.routeWithSteps
                                     },
                                     modifier = Modifier.fillMaxWidth(),
                                     shape = RectangleShape,
                                     contentPadding = PaddingValues(0.dp),
                                     colors = ButtonDefaults.buttonColors(
                                         containerColor = Color.Transparent,
                                         contentColor = MaterialTheme.colorScheme.primary
                                     )
                                 )
                                 {
                                     Text(
                                         text = "ROUTE ${routeWithInfo.routeWithSteps.route.routeId}",
                                         fontSize = 20.sp
                                     )

                                     Spacer(Modifier.weight(1f))

                                     Box(modifier = Modifier.background(PaleBlue,RoundedCornerShape(3.dp)).padding(horizontal = 8.dp)) {
                                         Text(
                                             text = "${(routeWithInfo.percent * 100).roundToInt()}%",
                                             color = LightSoftBlue,
                                             fontSize = 10.sp
                                         )
                                     }
                                 }

                                 LinearProgressIndicator(
                                     progress = { routeWithInfo.percent },
                                     modifier = Modifier.padding(6.dp).height(4.dp).fillMaxWidth(),
                                     color = Color.Blue,
                                     trackColor = Color.LightGray
                                 )
                             }
                        }

                         HorizontalDivider(
                             modifier = Modifier.padding(vertical = 8.dp),
                             thickness = 1.dp,
                             color = MaterialTheme.colorScheme.outlineVariant
                         )
                    }
                }
             } else {
                 item {
                     Column(Modifier.fillParentMaxSize()) {
                         Spacer(Modifier.weight(1f))
                         if(loading) {
//                             Row(
//                                 Modifier.fillMaxWidth(),
//                                 horizontalArrangement = Arrangement.Center
//                             ) {
//                                 CircularProgressIndicator(
//                                     modifier = Modifier.width(64.dp),
//                                     color = MaterialTheme.colorScheme.secondary,
//                                     trackColor = MaterialTheme.colorScheme.surfaceVariant,
//                                 )
//                             }
                         } else {
                             Row(
                                 Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.Center
                             ) {
                                 Icon(
                                     painterResource(ic_directions_walk),
                                     contentDescription = "routes",
                                     modifier = Modifier.size(56.dp)
                                 )
                             }
                             Text(
                                 "No Routes Setup Yet",
                                 fontSize = 24.sp,
                                 textAlign = TextAlign.Center,
                                 modifier = Modifier.fillMaxWidth()
                             )
                             Text(
                                 "Add a Route by pressing the + button in the top right",
                                 fontSize = 16.sp,
                                 textAlign = TextAlign.Center,
                                 modifier = Modifier.fillMaxWidth()
                             )
                         }
                         Spacer(Modifier.weight(1f))
                     }
                 }
             }
        }
    }

    if (showNewRouteModal.value) {
        Dialog(onDismissRequest = ::onDismissNewRouteModal) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Create a Route", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        IconButton(::onDismissNewRouteModal) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    AddRoute(addRouteViewModel, ::onDismissNewRouteModal)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = ::onDismissNewRouteModal) { Text("Close") }
                }
            }
        }
    }

    if (selectedRouteToView.value != null) {
        Dialog( { selectedRouteToView.value = null }) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
                Column(Modifier.padding(4.dp, 24.dp, 4.dp, 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Route ${selectedRouteToView.value?.route?.routeId ?: "Unknown"}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(12.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            {
                                selectedRouteToView.value?.route?.routeId?.let {
                                    routesViewModel.deleteRoute(
                                        context,
                                        it
                                    )
                                    selectedRouteToView.value = null
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Route"
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    MainRoute(mapViewModel, selectedRouteToView.value)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { selectedRouteToView.value = null }) { Text("Close") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val routesViewModel: RoutesViewModel = viewModel()
        Routes(routesViewModel)
    }
}