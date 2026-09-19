package com.zachupstone.walkanywhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.zachupstone.walkanywhere.map.MainRoute
import com.zachupstone.walkanywhere.steps.Steps
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.viewmodel.MapViewModel
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalkAnywhereTheme {
                WalkAnywhereApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun WalkAnywhereApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val mapViewModel = MapViewModel()
    val stepsViewModel = StepsViewModel()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Consumes top bar/navigation suite offsets safely
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        MainRoute(mapViewModel)
                    }
                    AppDestinations.STEPS -> {
                        Steps(stepsViewModel)
                    }
                    else -> {}
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Main Route", R.drawable.ic_home),
    STEPS("Steps", R.drawable.ic_favorite),
    SETTINGS("Profile", R.drawable.ic_account_box),
}



@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        WalkAnywhereApp()
    }
}