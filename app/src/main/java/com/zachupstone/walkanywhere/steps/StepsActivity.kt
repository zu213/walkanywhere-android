package com.zachupstone.walkanywhere.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel
import kotlin.collections.getValue

@Composable
fun Steps(stepsViewModel: StepsViewModel) {

    val context = LocalContext.current
    var fetchingStepsData by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllSteps(context)
        fetchingStepsData = false
    }
    val days by stepsViewModel.days
    val colourMap = stepsViewModel.routeColourMap

    Column {
        Text("Step History:")
        if(days != null && days!!.isNotEmpty()) {
            LazyColumn {
                for (day in days) {
                    item {
                        Text(day.date.toString())
                        LazyRow {
                            for (step in day.steps) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .background(
                                                color = colourMap.getValue(step.routeId),
                                                RoundedCornerShape(25.dp)
                                            )
                                            .clip(RoundedCornerShape(25.dp))
                                    ) {
                                        Text("Route ${step.routeId}, Steps: ${step.steps}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
         } else {
             if(fetchingStepsData) {
                 Text("Fetching Step data.")
             } else {
                 Text("No history recorded yet.")
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