package com.zachupstone.walkanywhere.map

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

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
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(onConfirmation == null) {
                        onDismissRequest()
                    } else {
                        onConfirmation()
                    }
                }
            ) {
                Text("Confirm")
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