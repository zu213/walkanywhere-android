package com.zachupstone.walkanywhere.steps

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel

@Composable
fun Steps(stepsViewModel: StepsViewModel) {

    val context = LocalContext.current
    val routes by stepsViewModel.routes
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllRoutes(context)
    }

    LazyColumn {
    // Add a single item
        if (routes != null) {
            for (route in routes) {
                route.route.routeId
            }
        }
        // Add another single item
        item {
            Text(text = "Last item")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val stepViewModel = StepsViewModel()
        Steps(stepViewModel)
    }
}