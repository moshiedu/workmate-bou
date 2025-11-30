package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Status Filters
        items(PermissionStatusFilter.values()) { filter ->
            FilterChip(
                selected = statusFilter == filter,
                onClick = { onStatusFilterChange(filter) },
                label = { 
                    Text(
                        when(filter) {
                            PermissionStatusFilter.ALL -> "All Status"
                            PermissionStatusFilter.GRANTED -> "Allowed"
                            PermissionStatusFilter.DENIED -> "Denied"
                        }
                    ) 
                },
                leadingIcon = if (statusFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when(filter) {
                        PermissionStatusFilter.GRANTED -> Color(0xFF10B981).copy(alpha = 0.2f)
                        PermissionStatusFilter.DENIED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> textColor.copy(alpha = 0.1f)
                    },
                    selectedLabelColor = textColor,
                    labelColor = textColor.copy(alpha = 0.7f),
                    selectedLeadingIconColor = textColor
                )
            )
        }
        
        // Separator
        item {
            VerticalDivider(
                modifier = Modifier.height(32.dp).padding(horizontal = 4.dp),
                color = textColor.copy(alpha = 0.2f)
            )
        }
        
        // App Type Filters
        items(AppTypeFilter.values()) { filter ->
            FilterChip(
                selected = appTypeFilter == filter,
                onClick = { onAppTypeFilterChange(filter) },
                label = { 
                    Text(
                        when(filter) {
                            AppTypeFilter.ALL -> "All Apps"
                            AppTypeFilter.USER_APPS -> "User Apps"
                            AppTypeFilter.SYSTEM_APPS -> "System Apps"
                        }
                    ) 
                },
                leadingIcon = if (appTypeFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when(filter) {
                        AppTypeFilter.USER_APPS -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                        AppTypeFilter.SYSTEM_APPS -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        else -> textColor.copy(alpha = 0.1f)
                    },
                    selectedLabelColor = textColor,
                    labelColor = textColor.copy(alpha = 0.7f),
                    selectedLeadingIconColor = textColor
                )
            )
        }
    }
}
