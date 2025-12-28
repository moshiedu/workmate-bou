package com.moshitech.workmate.feature.imagestudio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.data.EditorPreferences

/**
 * Crop Confirmation Dialog
 * Shows before applying crop with options for layer handling
 */
@Composable
fun CropConfirmationDialog(
    hasUnappliedLayers: Boolean,
    onConfirm: (autoApplyLayers: Boolean, dontAskAgain: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var autoApplyLayers by remember { mutableStateOf(true) }
    var dontAskAgain by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Crop?") },
        text = {
            Column {
                Text("This will crop the image to the selected area.")
                
                if (hasUnappliedLayers) {
                    Spacer(Modifier.height(16.dp))
                    
                    // Auto-apply layers checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { autoApplyLayers = !autoApplyLayers }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = autoApplyLayers,
                            onCheckedChange = { autoApplyLayers = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Auto-apply layers before crop", fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Don't ask again checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontAskAgain = !dontAskAgain }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = dontAskAgain,
                        onCheckedChange = { dontAskAgain = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Don't ask again", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(autoApplyLayers, dontAskAgain) }
            ) {
                Text("Apply Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * First-Time Layer Preference Dialog
 * Shown on first crop attempt with unapplied layers
 */
@Composable
fun LayerPreferenceDialog(
    onPreferenceSelected: (EditorPreferences.LayerApplicationMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { 
        mutableStateOf(EditorPreferences.LayerApplicationMode.ASK_EACH_TIME) 
    }
    
    AlertDialog(
        onDismissRequest = { /* Don't allow dismiss without selection */ },
        title = { Text("Layer Application Preference") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("You have unapplied layers. How would you like to handle this?")
                
                Spacer(Modifier.height(8.dp))
                
                // Always auto-apply option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            selectedMode = EditorPreferences.LayerApplicationMode.ALWAYS_AUTO 
                        }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedMode == EditorPreferences.LayerApplicationMode.ALWAYS_AUTO,
                        onClick = { selectedMode = EditorPreferences.LayerApplicationMode.ALWAYS_AUTO }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Always auto-apply", fontSize = 14.sp)
                        Text(
                            "Layers are automatically flattened before crop/rotate",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Ask each time option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            selectedMode = EditorPreferences.LayerApplicationMode.ASK_EACH_TIME 
                        }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedMode == EditorPreferences.LayerApplicationMode.ASK_EACH_TIME,
                        onClick = { selectedMode = EditorPreferences.LayerApplicationMode.ASK_EACH_TIME }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Ask me each time", fontSize = 14.sp)
                        Text(
                            "Show confirmation dialog before crop/rotate",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Never auto-apply option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            selectedMode = EditorPreferences.LayerApplicationMode.NEVER_AUTO 
                        }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedMode == EditorPreferences.LayerApplicationMode.NEVER_AUTO,
                        onClick = { selectedMode = EditorPreferences.LayerApplicationMode.NEVER_AUTO }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Manual only", fontSize = 14.sp)
                        Text(
                            "You must manually apply layers using the button",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "You can change this later in settings",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPreferenceSelected(selectedMode) }
            ) {
                Text("Save & Continue")
            }
        }
    )
}
