package com.zachupstone.walkanywhere.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.ui.steps.StepsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.zachupstone.walkanywhere.R
import com.zachupstone.walkanywhere.ui.theme.SoftBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Steps(stepsViewModel: StepsViewModel, onSettingsClick: () -> Unit) {

    var fetchingStepsData by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        stepsViewModel.fetchAllSteps()
        fetchingStepsData = false
    }
    val days by stepsViewModel.days
    val colourMap by stepsViewModel.routeColourMap.collectAsStateWithLifecycle()
    val loading by stepsViewModel.loading
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", LocalLocale.current.platformLocale)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Step History")
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if(i == 0) {
                                    Text(
                                        "Today's Steps",
                                        Modifier.padding(4.dp),
                                        fontSize = 20.sp
                                    )
                                } else {
                                    Text(
                                        day.date.atStartOfDay(ZoneId.systemDefault())
                                            .format(formatter)
                                            .toString(),
                                        Modifier.padding(4.dp),
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(Modifier.weight(1f))

                                if(i != 0) {
                                    Text(
                                        day.totalSteps.toString(),
                                        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                                        color = SoftBlue.copy(alpha = 1f),
                                    )
                                }
                            }

                            if(i == 0) {
                                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(4.dp, 8.dp)) {
                                    Text(
                                        day.totalSteps.toString(),
                                        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                                        color = SoftBlue.copy(alpha = 1f),
                                        fontSize = 34.sp
                                    )
                                    Text(
                                        " steps",
                                        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
                                        color = Color.Gray,
                                    )
                                }
                            }


                            LazyRow {
                                for ((_, parentRouteId, _, steps) in day.steps) {
                                    if(steps > 0) {
                                        item {
                                            Box(
                                                Modifier.padding(4.dp, 0.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = colourMap.getOrDefault(
                                                                parentRouteId,
                                                                Color.Transparent
                                                            ).copy(alpha = 0.3f),
                                                            RoundedCornerShape(20.dp)
                                                        )
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .padding(horizontal = 8.dp)

                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Circle,
                                                            contentDescription = "Solid Circle",
                                                            tint = colourMap.getOrDefault(
                                                                parentRouteId,
                                                                Color.Transparent
                                                            ),
                                                            modifier = Modifier.padding(end = 6.dp)
                                                                .size(8.dp)
                                                                .wrapContentHeight(align = Alignment.CenterVertically)
                                                        )
                                                        Text(
                                                            "ROUTE $parentRouteId: ",
                                                            fontSize = 10.sp
                                                        )
                                                        Text(
                                                            "$steps",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        } else {
            Column(Modifier.padding(innerPadding).fillMaxSize()) {
                Spacer(Modifier.weight(1f))
                if(loading) {
//                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.width(64.dp),
//                            color = MaterialTheme.colorScheme.secondary,
//                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
//                        )
//                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Icon(
                            painterResource(R.drawable.ic_podiatry),
                            contentDescription = "routes",
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    if (fetchingStepsData) {
                        Text(
                            "Fetching Step data...",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            "No History Recorded Yet.",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        val stepsViewModel: StepsViewModel = viewModel()
        Steps(stepsViewModel = stepsViewModel) {}
    }
}