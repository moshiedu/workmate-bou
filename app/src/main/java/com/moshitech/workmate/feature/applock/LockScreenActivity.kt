package com.moshitech.workmate.feature.applock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockScreenActivity : FragmentActivity() {
    
    private lateinit var lockManager: AppLockManager
    private var lockedPackage: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lockManager = AppLockManager.getInstance(applicationContext)
        val repository = com.moshitech.workmate.data.repository.UserPreferencesRepository(applicationContext)
        lockedPackage = intent.getStringExtra("locked_package")
        
        // Exclude from recents (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            setTaskDescription(android.app.ActivityManager.TaskDescription.Builder().build())
        } else {
            @Suppress("DEPRECATION")
            setTaskDescription(android.app.ActivityManager.TaskDescription())
        }
        
        lifecycleScope.launch {
            val usePin = repository.usePinAuth.first()
            val useBiometric = repository.useBiometricAuth.first()
            val theme = repository.theme.first()
            
            // Determine if dark mode should be used
            val systemDark = resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            val isDark = when (theme) {
                com.moshitech.workmate.data.repository.AppTheme.LIGHT -> false
                com.moshitech.workmate.data.repository.AppTheme.DARK -> true
                com.moshitech.workmate.data.repository.AppTheme.SYSTEM -> systemDark
            }
            
            if (useBiometric && savedInstanceState == null) {
                showBiometricPrompt()
            }
            
            setContent {
                LockScreenContent(
                    isDark = isDark,
                    usePin = usePin,
                    useBiometric = useBiometric,
                    onPinEntered = { pin, onError -> verifyAndUnlock(pin, onError) },
                    onBiometricRequested = { showBiometricPrompt() },
                    onCancel = { goHome() }
                )
            }
        }
    }
    
    private fun verifyAndUnlock(pin: String, onError: () -> Unit) {
        lifecycleScope.launch {
            if (lockManager.verifyPin(pin)) {
                lockedPackage?.let { lockManager.unlockApp(it) }
                finish()
            } else {
                onError()
            }
        }
    }
    
    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) 
            != BiometricManager.BIOMETRIC_SUCCESS) {
            return
        }
        
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    lockedPackage?.let { lockManager.unlockApp(it) }
                    finish()
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock App")
            .setSubtitle("Use your fingerprint to unlock")
            .setNegativeButtonText("Cancel")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        // Override back button to go home instead of returning to locked app
        goHome()
    }
}

@Composable
fun LockScreenContent(
    isDark: Boolean,
    usePin: Boolean,
    useBiometric: Boolean,
    onPinEntered: (String, () -> Unit) -> Unit,
    onBiometricRequested: () -> Unit,
    onCancel: () -> Unit
) {
    // Theme-aware colors
    val overlayColor = if (isDark) Color(0xF0000000) else Color(0xF0FFFFFF)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF3B82F6)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "App Locked",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                if (usePin) {
                    Text(
                        text = "Enter your PIN to continue",
                        fontSize = 14.sp,
                        color = secondaryTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { 
                            pin = it
                            showError = false
                        },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = showError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = borderColor,
                            focusedLabelColor = secondaryTextColor,
                            unfocusedLabelColor = secondaryTextColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (showError) {
                        Text(
                            text = "Incorrect PIN",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (pin.isNotEmpty()) {
                                onPinEntered(pin) {
                                    showError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Unlock")
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Authentication Required",
                        fontSize = 14.sp,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (useBiometric) {
                    TextButton(
                        onClick = onBiometricRequested,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use Biometric", color = Color(0xFF3B82F6))
                    }
                }
                
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        }
    }
}
