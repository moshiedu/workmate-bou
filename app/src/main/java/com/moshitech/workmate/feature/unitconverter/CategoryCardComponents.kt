package com.moshitech.workmate.feature.unitconverter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
 * Expandable group header for grouped category view with enhanced visuals
 */
@Composable
fun CategoryGroupHeader(
    group: CategoryGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    categoryCount: Int = 0
) {
    val iconRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "icon_rotation"
    )
    
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            group.accentColor.copy(alpha = 0.15f),
                            group.accentColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Larger emoji
                    Text(
                        text = group.emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                            // Category count badge
                            if (categoryCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(24.dp)
                                        .background(
                                            color = group.accentColor.copy(alpha = 0.25f),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = categoryCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = group.accentColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                // Animated expand icon
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = group.accentColor,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(iconRotation)
                )
            }
        }
    }
}

/**
 * Compact group header for Grid and List views (non-expandable)
 */
@Composable
fun CompactGroupHeader(
    group: CategoryGroup,
    categoryCount: Int,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = group.emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = group.title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        // Category count badge
        if (categoryCount > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = group.accentColor.copy(alpha = 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = group.accentColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}
