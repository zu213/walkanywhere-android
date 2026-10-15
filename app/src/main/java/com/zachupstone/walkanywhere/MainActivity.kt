package com.zachupstone.walkanywhere

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.zachupstone.walkanywhere.map.MainRoute
import com.zachupstone.walkanywhere.steps.Steps
import com.zachupstone.walkanywhere.routes.Routes
import com.zachupstone.walkanywhere.settings.Settings
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme
import com.zachupstone.walkanywhere.util.ManifestUtils
import com.zachupstone.walkanywhere.viewmodel.MapViewModel
import com.zachupstone.walkanywhere.viewmodel.RoutesViewModel
import com.zachupstone.walkanywhere.viewmodel.SettingsViewModel
import com.zachupstone.walkanywhere.viewmodel.StepsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val stepRepository by lazy {
        (application as WalkAnywhereApplication).stepRepository
    }

    private val stepSyncer by lazy {
        (application as WalkAnywhereApplication).stepSyncer
    }

    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(stepRepository.stepPermissions)) {
            stepSyncer.subscribeToFitnessService()
        }
    }

    private val requestActivityRecognition = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestHealthPermissions.launch(stepRepository.stepPermissions)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the API key from the manifest file
        val apiKey = ManifestUtils.getApiKeyFromManifest(this)
        // Initialize the Places API with the retrieved API key
        if (!Places.isInitialized() && apiKey != null) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }

        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    if (stepRepository.hasPermissions() && stepSyncer.hasPermissions()) {
                        stepSyncer.subscribeToFitnessService()
                    } else {
                        // Initial request of perms
                        if(!stepSyncer.hasPermissions()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                requestActivityRecognition.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            }
                        } else {
                            requestHealthPermissions.launch(stepRepository.stepPermissions)
                        }
                    }
                }
            }
        }

        setContent {
            WalkAnywhereTheme {
                WalkAnywhereApp()
            }
        }
    }
}

@Serializable
object MainRoute
@Serializable
object SettingsRoute

@PreviewScreenSizes
@Composable
fun WalkAnywhereApp() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(navController, startDestination = MainRoute) {
        composable<MainRoute> {
            MainScaffold(onSettingsClick = { navController.navigate(SettingsRoute) { launchSingleTop = true } })
        }
        composable<SettingsRoute> {
            Settings(settingsViewModel, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainScaffold(onSettingsClick: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val mapViewModel: MapViewModel = viewModel()
    val routesViewModel: RoutesViewModel = viewModel()
    val stepsViewModel: StepsViewModel = viewModel()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label,
                            modifier = Modifier.size(32.dp)
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
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        MainRoute(mapViewModel, null)
                    }
                    AppDestinations.STEPS -> {
                        Steps(stepsViewModel, onSettingsClick)
                    }
                    AppDestinations.ROUTES -> {
                        Routes(routesViewModel)
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Current Route", R.drawable.ic_directions_walk),
    ROUTES("Routes", R.drawable.ic_list_hamburger),

    STEPS("Steps", R.drawable.ic_podiatry),
}



@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        WalkAnywhereApp()
    }
}