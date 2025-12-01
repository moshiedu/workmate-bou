
package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.deviceinfo.viewmodel.AppTypeFilter
import com.moshitech.workmate.feature.deviceinfo.viewmodel.PermissionStatusFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    statusFilter: PermissionStatusFilter,
    appTypeFilter: AppTypeFilter,
    onStatusFilterChange: (PermissionStatusFilter) -> Unit,
    onAppTypeFilterChange: (AppTypeFilter) -> Unit,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Status Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PermissionStatusFilter.entries.forEach { filter ->
                val isSelected = statusFilter == filter
                FilterChip(
                    selected = isSelected,
                    enabled = true,
                    onClick = { onStatusFilterChange(filter) },
                    label = { 
                        Text(
                            text = when(filter) {
                                PermissionStatusFilter.ALL -> "All Status"
                                PermissionStatusFilter.GRANTED -> "Allowed"
                                PermissionStatusFilter.DENIED -> "Denied"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            when(filter) {
                                PermissionStatusFilter.ALL -> Icons.Default.Apps
                                PermissionStatusFilter.GRANTED -> Icons.Default.CheckCircle
                                PermissionStatusFilter.DENIED -> Icons.Default.Block
                            },
                            contentDescription = null,
                            modifier = Modifier.size(10.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when(filter) {
                            PermissionStatusFilter.GRANTED -> Color(0xFF10B981).copy(alpha = 0.2f)
                            PermissionStatusFilter.DENIED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                        },
                        selectedLabelColor = when(filter) {
                            PermissionStatusFilter.GRANTED -> Color(0xFF10B981)
                            PermissionStatusFilter.DENIED -> Color(0xFFEF4444)
                            else -> Color(0xFF3B82F6)
                        },
                        selectedLeadingIconColor = when(filter) {
                            PermissionStatusFilter.GRANTED -> Color(0xFF10B981)
                            PermissionStatusFilter.DENIED -> Color(0xFFEF4444)
                            else -> Color(0xFF3B82F6)
                        },
                        labelColor = textColor.copy(alpha = 0.7f),
                        iconColor = textColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp), // Reduced height
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = textColor.copy(alpha = 0.1f),
            thickness = 0.5.dp
        )
        
        // Row 2: App Type Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTypeFilter.entries.forEach { filter ->
                val isSelected = appTypeFilter == filter
                FilterChip(
                    selected = isSelected,
                    enabled = true,
                    onClick = { onAppTypeFilterChange(filter) },
                    label = { 
                        Text(
                            text = when(filter) {
                                AppTypeFilter.ALL -> "All Apps"
                                AppTypeFilter.USER_APPS -> "User"
                                AppTypeFilter.SYSTEM_APPS -> "System"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            when(filter) {
                                AppTypeFilter.ALL -> Icons.Default.Apps
                                AppTypeFilter.USER_APPS -> Icons.Default.Person
                                AppTypeFilter.SYSTEM_APPS -> Icons.Default.Settings
                            },
                            contentDescription = null,
                            modifier = Modifier.size(10.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when(filter) {
                            AppTypeFilter.USER_APPS -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            AppTypeFilter.SYSTEM_APPS -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                        },
                        selectedLabelColor = when(filter) {
                            AppTypeFilter.USER_APPS -> Color(0xFF8B5CF6)
                            AppTypeFilter.SYSTEM_APPS -> Color(0xFFF59E0B)
                            else -> Color(0xFF3B82F6)
                        },
                        selectedLeadingIconColor = when(filter) {
                            AppTypeFilter.USER_APPS -> Color(0xFF8B5CF6)
                            AppTypeFilter.SYSTEM_APPS -> Color(0xFFF59E0B)
                            else -> Color(0xFF3B82F6)
                        },
                        labelColor = textColor.copy(alpha = 0.7f),
                        iconColor = textColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp), // Reduced height
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
