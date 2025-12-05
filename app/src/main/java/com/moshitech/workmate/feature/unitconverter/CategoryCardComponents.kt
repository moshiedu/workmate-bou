package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Category card with help icon for unit converter
 */
@Composable
fun CategoryCardWithHelp(
    category: UnitCategory,
    onClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.5.dp,
            category.accentColor.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            // Help icon
            IconButton(
                onClick = { onHelpClick() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Expandable group header for grouped category view
 */
@Composable
fun CategoryGroupHeader(
    group: CategoryGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = group.accentColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
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
                Text(
                    text = group.emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
            }
            Icon(
                imageVector = if (isExpanded) 
                    Icons.Default.ExpandLess 
                else 
                    Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = group.accentColor
            )
        }
    }
}
