package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Dialog displaying help information for a unit conversion category
 */
@Composable
fun CategoryHelpDialog(
    help: CategoryHelp,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val backgroundColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = help.category.accentColor

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = help.category.icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = help.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = secondaryTextColor
                        )
                    }
                }

                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Description
                    Text(
                        text = help.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Disclaimer (if present)
                    help.disclaimer?.let { disclaimerText ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
                            ),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (isDark) Color(0xFFDC2626) else Color(0xFFEF4444)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = disclaimerText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Use Cases
                    if (help.useCases.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Common Uses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        help.useCases.forEach { useCase ->
                            Row(
                                modifier = Modifier.padding(start = 28.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = "•",
                                    color = accentColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = useCase,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Tips
                    if (help.tips.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFC107), // Amber for tips
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Helpful Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        help.tips.forEach { tip ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFBE6)
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "💡",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Examples
                    if (help.examples.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50), // Green for examples
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Examples",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        help.examples.forEach { example ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE8F5E9)
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "📌",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = example,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer
                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor
                        )
                    ) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}
