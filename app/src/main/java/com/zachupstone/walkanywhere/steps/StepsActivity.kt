package com.zachupstone.walkanywhere.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Steps(stepsViewModel: StepsViewModel) {

    val context = LocalContext.current
    var fetchingStepsData by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllSteps(context)
        fetchingStepsData = false
    }
    val days by stepsViewModel.days
    val colourMap by stepsViewModel.routeColourMap.collectAsStateWithLifecycle()
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Steps")
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if(days != null && days!!.isNotEmpty()) {
            LazyColumn(
                Modifier.padding(innerPadding)
                    .padding(20.dp)
            ) {
                for ((i, day) in days!!.withIndex()) {
                    item {
                        Column {
                            if(i > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            Text(day.date.toInstant().atZone(ZoneId.systemDefault()).format(formatter).toString(),
                                Modifier.padding(4.dp),
                                fontSize = 16.sp)

                            LazyRow {
                                for (step in day.steps) {
                                    item {
                                        Box(
                                            Modifier.padding(4.dp, 0.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = colourMap.getOrDefault(
                                                            step.parentRouteId,
                                                            Color.Transparent
                                                        ),
                                                        RoundedCornerShape(20.dp)
                                                    )
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .padding(8.dp, 2.dp)

                                            ) {
                                                Text(
                                                    "Route ${step.parentRouteId}: ${step.steps}",
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
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
        val stepsViewModel: StepsViewModel = viewModel()
        Steps(stepsViewModel = stepsViewModel)
    }
}