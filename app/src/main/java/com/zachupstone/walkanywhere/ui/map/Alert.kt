package com.zachupstone.walkanywhere.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (() -> Unit)?,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            if (onConfirmation == null) {
                // M3 right-aligns dialog buttons; a full-width Box is the only way to centre a lone one
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Dismiss")
                    }
                }
            } else {
                TextButton(onClick = onConfirmation) {
                    Text("Confirm")
                }
            }
        },
        dismissButton = if (onConfirmation != null) {
            {
                TextButton(onClick = onDismissRequest) {
                    Text("Dismiss")
                }
            }
        } else null
    )
}