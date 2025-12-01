package com.moshitech.workmate.feature.applock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    navController: NavController,
    viewModel: AppLockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("App Lock", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (state) {
                AppLockState.DASHBOARD -> DashboardView(viewModel, isDark, textColor)
                AppLockState.SECURITY_SETUP -> SecurityQuestionView(viewModel, isDark, textColor)
                AppLockState.FORGOT_PIN -> ForgotPinView(viewModel, isDark, textColor)
                else -> PinView(viewModel, isDark, textColor)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val filteredApps by viewModel.filteredApps.collectAsState()
    val lockedApps by viewModel.lockedApps.collectAsState()
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Stats Cards
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard("Total", stats.first.toString(), Color(0xFF3B82F6), isDark, Modifier.weight(1f))
                StatsCard("Locked", stats.second.toString(), Color(0xFFEF4444), isDark, Modifier.weight(1f))
                StatsCard("Unlocked", stats.third.toString(), Color(0xFF10B981), isDark, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Combined Settings Card
        item {
            val usePin by viewModel.usePinAuth.collectAsState()
            val useBiometric by viewModel.useBiometricAuth.collectAsState()
            
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Row 1: Service Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isServiceEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isServiceEnabled) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "App Lock Service",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    if (isServiceEnabled) "Active" else "Paused",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isServiceEnabled) Color(0xFF10B981) else Color.Gray
                                )
                            }
                        }
                        Switch(
                            checked = isServiceEnabled,
                            onCheckedChange = { viewModel.toggleService(it) },
                            modifier = Modifier.scale(0.7f).height(30.dp)
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = textColor.copy(alpha = 0.1f)
                    )
                    
                    // Row 2: Auth Methods
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PIN
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.togglePinAuth(!usePin) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = usePin,
                                onCheckedChange = { viewModel.togglePinAuth(it) },
                                modifier = Modifier.scale(0.7f).size(32.dp)
                            )
                            Text("PIN Code", color = textColor, style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Biometric
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleBiometricAuth(!useBiometric) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useBiometric,
                                onCheckedChange = { viewModel.toggleBiometricAuth(it) },
                                modifier = Modifier.scale(0.7f).size(32.dp)
                            )
                            Text("Biometric", color = textColor, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Accessibility Service Setup Guide
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isAccessibilityEnabled = remember {
                mutableStateOf(isAccessibilityServiceEnabled(context))
            }
            
            // Auto-refresh when returning to this screen
            androidx.compose.runtime.DisposableEffect(Unit) {
                val listener = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
                    }
                }
                val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
                lifecycle?.addObserver(listener)
                
                onDispose {
                    lifecycle?.removeObserver(listener)
                }
            }
            
            if (!isAccessibilityEnabled.value && lockedApps.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                     Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Required", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Enable Service")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Sticky Search and Filter
        stickyHeader {
            Surface(
                color = backgroundColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchAndFilterBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        selectedFilter = filterType,
                        onFilterSelect = viewModel::onFilterChange,
                        isDark = isDark,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        items(filteredApps, key = { it.packageName }) { app ->
            AppItem(app, lockedApps.contains(app.packageName), isDark, textColor) {
                viewModel.toggleAppLock(app.packageName)
            }
        }
    }
}

@Composable
fun TypingText(text: String, color: Color) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { index, _ ->
            delay(50) // Typing speed
            displayedText = text.substring(0, index + 1)
        }
    }
    
    Text(
        text = displayedText,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun StatsCard(label: String, value: String, color: Color, isDark: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Subtle icon indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(label) {
                        "Locked" -> Icons.Default.Lock
                        "Unlocked" -> Icons.Default.LockOpen
                        else -> Icons.Default.Search // Using Search as generic "All" icon or can use something else
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: AppLockViewModel.AppLockFilter,
    onFilterSelect: (AppLockViewModel.AppLockFilter) -> Unit,
    isDark: Boolean,
    textColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search apps...", color = textColor.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = textColor.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == AppLockViewModel.AppLockFilter.ALL,
                onClick = { onFilterSelect(AppLockViewModel.AppLockFilter.ALL) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF3B82F6)
                )
            )
            FilterChip(
                selected = selectedFilter == AppLockViewModel.AppLockFilter.LOCKED,
                onClick = { onFilterSelect(AppLockViewModel.AppLockFilter.LOCKED) },
                label = { Text("Locked") },
                leadingIcon = { if (selectedFilter == AppLockViewModel.AppLockFilter.LOCKED) Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFEF4444)
                )
            )
            FilterChip(
                selected = selectedFilter == AppLockViewModel.AppLockFilter.UNLOCKED,
                onClick = { onFilterSelect(AppLockViewModel.AppLockFilter.UNLOCKED) },
                label = { Text("Unlocked") },
                leadingIcon = { if (selectedFilter == AppLockViewModel.AppLockFilter.UNLOCKED) Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF10B981)
                )
            )
        }
    }
}

// Helper function to check if accessibility service is enabled
private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val accessibilityManager = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    val enabledServices = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(context.packageName) == true
}

@Composable
fun AppItem(app: AppInfo, isLocked: Boolean, isDark: Boolean, textColor: Color, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
            
            Box(
                modifier = Modifier
                    .background(
                        color = if (isLocked) Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLocked) "Locked" else "Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLocked) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PinView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val pin by viewModel.pin.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.headlineSmall, color = textColor)
        if (error != null) {
            Text(error!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Pin Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < pin.length) textColor else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Number Pad
        val numbers = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        numbers.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(80.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(if (isDark) Color(0xFF1E293B) else Color.White, CircleShape)
                                .clickable {
                                    if (digit == "⌫") viewModel.onDeleteDigit() else viewModel.onPinDigit(digit)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(digit, style = MaterialTheme.typography.headlineMedium, color = textColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Show "Forgot PIN?" only in VERIFY state
        val state by viewModel.state.collectAsState()
        if (state == AppLockState.VERIFY) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.showForgotPin() }) {
                Text("Forgot PIN?", color = Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
fun SecurityQuestionView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val question by viewModel.securityQuestion.collectAsState()
    val answer by viewModel.securityAnswer.collectAsState()
    val error by viewModel.error.collectAsState()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF3B82F6)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Security Question",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Text(
            "This will help you recover your PIN if you forget it",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = question,
            onValueChange = { viewModel.onSecurityQuestionChange(it) },
            label = { Text("Security Question") },
            placeholder = { Text("e.g., What's your favorite color?") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = textColor.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = answer,
            onValueChange = { viewModel.onSecurityAnswerChange(it) },
            label = { Text("Answer") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = textColor.copy(alpha = 0.5f)
            )
        )
        
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error!!,
                color = Color(0xFFEF4444),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.saveSecurityQuestion() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text("Save")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = { viewModel.skipSecurityQuestion() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip for now", color = textColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ForgotPinView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val question by viewModel.securityQuestion.collectAsState()
    val answer by viewModel.recoveryAnswer.collectAsState()
    val error by viewModel.error.collectAsState()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    
    val hasSecurityQuestion = question.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFEF4444)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Forgot PIN?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        if (hasSecurityQuestion) {
            // Show security question recovery
            Text(
                "Answer your security question to reset your PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Security Question:",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = answer,
                onValueChange = { viewModel.onRecoveryAnswerChange(it) },
                label = { Text("Your Answer") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f)
                )
            )
            
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.verifyRecoveryAnswer() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("Verify & Reset PIN")
            }
        } else {
            // Show reset option when no security question
            Text(
                "No security question was set for this PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Reset Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "To recover access, you'll need to reset your App Lock. This will:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("• Clear your current PIN", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
                        Text("• Remove all locked apps", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
                        Text("• Disable the App Lock service", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can set up a new PIN afterward.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.resetAppLock() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Reset App Lock")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = { viewModel.cancelRecovery() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel", color = textColor.copy(alpha = 0.7f))
        }
    }
}
