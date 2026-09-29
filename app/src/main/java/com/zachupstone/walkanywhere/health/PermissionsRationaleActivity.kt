package com.zachupstone.walkanywhere.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalkAnywhereTheme {
                Text(
                    "WalkAnywhere reads your step count to show your daily steps. " +
                            "Your data stays on your device.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}