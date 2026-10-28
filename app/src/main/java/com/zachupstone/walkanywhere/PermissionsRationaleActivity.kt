package com.zachupstone.walkanywhere

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
                    "WalkAnywhere records your step count using your phone's motion sensor and " +
                            "saves it to Health Connect, then reads it back to show your daily " +
                            "steps against your routes. Your step data stays on your device and " +
                            "is never shared.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}