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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel

@Composable
fun Steps(stepsViewModel: StepsViewModel) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllSteps(context)
    }
    val days by stepsViewModel.days

    Column {
        Text("Step History:")
        if(days != null && days!!.isNotEmpty()) {
            LazyColumn {
                // Add a single item
                for (day in days) {
                    item {
                        Text(day.date.toString())
                        LazyRow {
                            for (step in day.steps) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .background(
                                                color = Color.Blue,
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
             Text("No history recorded yet.")
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