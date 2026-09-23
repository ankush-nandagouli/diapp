package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AlertsDialog
import com.example.ui.components.SystemHealthLogsDialog
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamTeal
import com.example.ui.viewmodel.DakshyamViewModel

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PARTNERS("Partners", Icons.Default.People),
    CASH_FLOW("Cash Flow", Icons.Default.Payments),
    PROJECTS("Projects", Icons.Default.Engineering),
    REPORTS("Daily Logs", Icons.Default.Assignment),
    PROFILE("Profile", Icons.Default.Business)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: DakshyamViewModel,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.PARTNERS) }
    var showAlertsDialog by remember { mutableStateOf(false) }
    var showSystemLogsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val activePartner by viewModel.activePartner.collectAsState()
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState()
    val companyProfile by viewModel.companyProfile.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!companyProfile?.logoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = companyProfile?.logoUrl,
                                contentDescription = "Business Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = companyProfile?.companyName ?: "Dakshyam Innovations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Locked Active Partner Identity (No switching allowed)
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .testTag("active_partner_identity_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Profile Locked",
                                        tint = DakshyamCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Active: ${activePartner?.name ?: "Partner"}",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DakshyamNavy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerManualSync() },
                        modifier = Modifier.testTag("btn_sync_all_cloud")
                    ) {
                        if (isCloudSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = DakshyamCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync All Data Across Devices",
                                tint = DakshyamCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSystemLogsDialog = true },
                        modifier = Modifier.testTag("btn_system_health_logs")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = "System Health & All Logs",
                            tint = DakshyamCyan
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.markAlertsRead()
                            showAlertsDialog = true
                        },
                        modifier = Modifier.testTag("notification_bell_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadAlertsCount > 0) {
                                    Badge(
                                        containerColor = DakshyamCyan,
                                        contentColor = DakshyamNavy
                                    ) {
                                        Text("$unreadAlertsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Activity Alerts",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.testTag("top_bar_logout_btn")
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DakshyamNavy,
                            selectedTextColor = DakshyamNavy,
                            indicatorColor = DakshyamCyan.copy(alpha = 0.25f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "ScreenTabTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.PARTNERS -> PartnersScreen(viewModel = viewModel)
                    NavigationTab.CASH_FLOW -> CashFlowScreen(viewModel = viewModel)
                    NavigationTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                    NavigationTab.REPORTS -> DailyReportsScreen(viewModel = viewModel)
                    NavigationTab.PROFILE -> ProfileScreen(viewModel = viewModel, onLogout = onLogout)
                }
            }
        }
    }

    if (showAlertsDialog) {
        AlertsDialog(
            viewModel = viewModel,
            onDismiss = { showAlertsDialog = false }
        )
    }

    if (showSystemLogsDialog) {
        SystemHealthLogsDialog(
            viewModel = viewModel,
            onDismiss = { showSystemLogsDialog = false }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log Out Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your session as ${activePartner?.name ?: "Partner"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.signOutPartner()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DakshyamRose)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Enforce first login password change in dashboard
    val needsPasswordChange = activePartner != null && (activePartner?.mustChangePassword == true || activePartner?.password == "427752")
    if (needsPasswordChange) {
        val currentPartner = activePartner!!
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var newPassVisible by remember { mutableStateOf(false) }
        var confirmPassVisible by remember { mutableStateOf(false) }
        var changeError by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { /* Mandatory prompt - cannot dismiss */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = DakshyamNavy,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "First Login: Set Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DakshyamNavy
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = DakshyamCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome ${currentPartner.name}! This is your first login. You must set a unique password (min 6 chars) to protect partner records.",
                                fontSize = 12.sp,
                                color = DakshyamNavy
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; changeError = null },
                        label = { Text("New Password") },
                        placeholder = { Text("Min 6 characters") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPassVisible = !newPassVisible }) {
                                Icon(
                                    imageVector = if (newPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = DakshyamNavy
                                )
                            }
                        },
                        visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dashboard_new_password"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; changeError = null },
                        label = { Text("Confirm New Password") },
                        placeholder = { Text("Repeat new password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                                Icon(
                                    imageVector = if (confirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = DakshyamNavy
                                )
                            }
                        },
                        visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dashboard_confirm_password"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (changeError != null) {
                        Text(
                            text = changeError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val np = newPass.trim()
                        val cp = confirmPass.trim()
                        if (np.length < 6) {
                            changeError = "Password must be at least 6 characters"
                            return@Button
                        }
                        if (np == "427752") {
                            changeError = "Please select a different password than default '427752'"
                            return@Button
                        }
                        if (np != cp) {
                            changeError = "Passwords do not match"
                            return@Button
                        }

                        isSaving = true
                        viewModel.updatePartnerPassword(currentPartner.id, np) { success, msg ->
                            isSaving = false
                            if (success) {
                                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                changeError = msg ?: "Failed to update password."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    enabled = !isSaving,
                    modifier = Modifier.testTag("btn_save_dashboard_password")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save New Password")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.signOutPartner()
                        onLogout()
                    }
                ) {
                    Text("Sign Out", color = DakshyamRose)
                }
            }
        )
    }
}
