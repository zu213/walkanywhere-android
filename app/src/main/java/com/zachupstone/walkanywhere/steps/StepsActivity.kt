package com.zachupstone.walkanywhere.steps

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllSteps(context)
    }
    val days by stepsViewModel.days

    days?.let { processedDays ->
        LazyColumn {
        // Add a single item
            for (day in processedDays) {
                item {
                    Text(day.date.toString())
                    LazyRow {
                        for (step in day.steps) {
                            item {
                                Text("${step.routeId}: ${step.steps}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val stepsViewModel = StepsViewModel()
        Steps(stepsViewModel = stepsViewModel)
    }
}