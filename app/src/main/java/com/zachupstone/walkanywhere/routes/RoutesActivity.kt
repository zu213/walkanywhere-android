package com.zachupstone.walkanywhere.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zachupstone.walkanywhere.data.RouteWithSteps
import com.zachupstone.walkanywhere.map.MainRoute
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.AddRouteViewModel
import com.zachupstone.walkanywhere.viewmodel.MapViewModel
import com.zachupstone.walkanywhere.viewmodel.RoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Routes(routesViewModel: RoutesViewModel) {

    val context = LocalContext.current
    val routes by routesViewModel.routes
    val showNewRouteModal = remember { mutableStateOf(false) }
    val addRouteViewModel = AddRouteViewModel()

    val mapViewModel = MapViewModel()
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
                    Text("Routes")
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
            if (routes != null) {
                for (route in routes) {
                    item {
                        Row(Modifier.fillMaxSize()) {
                            Button({
                                routesViewModel.favouriteRoute(context, route.route.routeId)
                            }) {
                                Icon(
                                    imageVector = if (route.route.selected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (route.route.selected) "unfavourite" else "favourite"
                                )
                            }

                            Button(
                                onClick = {
                                    selectedRouteToView.value = route
                                },
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary))
                            {
                                Text(text = "Route ${route.route.routeId}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewRouteModal.value) {
        Dialog(onDismissRequest = ::onDismissNewRouteModal) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
                Column(Modifier.padding(24.dp)) {
                    Text("Create a Route", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    AddRoute(addRouteViewModel, ::onDismissNewRouteModal)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = ::onDismissNewRouteModal) { Text("Close") }
                }
            }
        }
    }

    if (selectedRouteToView.value != null) {
        Dialog( { selectedRouteToView.value = null }) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
                Column(Modifier.padding(24.dp)) {
                    Row() {
                        Text(
                            "Route ${selectedRouteToView.value?.route?.routeId ?: "Unknown"}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            {
                                selectedRouteToView?.value?.route?.routeId?.let {
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
                    Spacer(Modifier.height(12.dp))
                    MainRoute(mapViewModel, selectedRouteToView.value)
                    Spacer(Modifier.height(16.dp))
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
        val routesViewModel = RoutesViewModel()
        Routes(routesViewModel)
    }
}