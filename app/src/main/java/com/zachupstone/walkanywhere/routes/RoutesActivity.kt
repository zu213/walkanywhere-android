package com.zachupstone.walkanywhere.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zachupstone.walkanywhere.R
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.AddRouteViewModel
import com.zachupstone.walkanywhere.viewmodel.RoutesViewModel

@Composable
fun Routes(routesViewModel: RoutesViewModel) {

    val context = LocalContext.current
    val routes by routesViewModel.routes
    val showModal = remember { mutableStateOf(false) }
    val addRouteViewModel = AddRouteViewModel()

    LaunchedEffect(Unit) {
        routesViewModel.fetchAllRoutes(context)
    }

    Button({
        showModal.value = true
    }) {
        Text("Add new route")
    }

    if (showModal.value) {
        Dialog(onDismissRequest = { showModal.value = false }) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
                Column(Modifier.padding(24.dp)) {
                    Text("Modal title", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    AddRoute(addRouteViewModel, {showModal.value = false})
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { showModal.value = false }) { Text("Close") }
                }
            }
        }
    }

    LazyColumn {
    // Add a single item
        if (routes != null) {
            for (route in routes) {

                item {
                    Button({
                        routesViewModel.favouriteRoute(context, route.route.routeId)
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_home),
                            contentDescription = "star"
                        )
                    }
                    Text(text = "Route ${route.route.routeId}")
                }
            }
        }
        // Add another single item

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