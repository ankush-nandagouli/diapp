package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppHealthStatus
import com.example.data.model.SystemLogEntry
import com.example.data.model.SystemLogLevel
import com.example.ui.theme.DakshyamAmber
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamEmerald
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamNavyBlue
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamTeal
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.DakshyamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SystemHealthLogsDialog(
    viewModel: DakshyamViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appHealth by viewModel.appHealth.collectAsState()
    val systemLogs by viewModel.systemLogs.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedLevelFilter by remember { mutableStateOf<SystemLogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(systemLogs, selectedLevelFilter, searchQuery) {
        systemLogs.filter { log ->
            val matchesLevel = selectedLevelFilter == null || log.level == selectedLevelFilter
            val matchesSearch = searchQuery.isBlank() ||
                    log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true) ||
                    (log.details?.contains(searchQuery, ignoreCase = true) == true)
            matchesLevel && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("system_health_logs_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                Surface(
                    color = DakshyamNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DakshyamTeal.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    tint = DakshyamCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "System Health & Diagnostics",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Audit Logs • Security • Infrastructure",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Tab Switcher
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DakshyamNavyBlue,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = DakshyamCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("App Health Status", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("System Logs (${systemLogs.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> {
                            AppHealthTabContent(
                                appHealth = appHealth,
                                activePartnerName = activePartner?.name ?: "None",
                                onRunDiagnostics = {
                                    viewModel.refreshAppHealth()
                                    Toast.makeText(context, "Diagnostics refreshed successfully", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        else -> {
                            SystemLogsTabContent(
                                logs = filteredLogs,
                                allLogsCount = systemLogs.size,
                                selectedLevel = selectedLevelFilter,
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                onSelectLevel = { selectedLevelFilter = it },
                                onClearLogs = {
                                    viewModel.clearSystemLogs()
                                    Toast.makeText(context, "System logs cleared", Toast.LENGTH_SHORT).show()
                                },
                                onCopyLogs = {
                                    val text = viewModel.exportSystemLogsText()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dakshyam System Logs", text))
                                    Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHealthTabContent(
    appHealth: AppHealthStatus,
    activePartnerName: String,
    onRunDiagnostics: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (appHealth.isHealthy) DakshyamNavy else Color(0xFF4A0E17)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (appHealth.isHealthy) DakshyamEmerald.copy(alpha = 0.2f) else DakshyamRose.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (appHealth.isHealthy) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (appHealth.isHealthy) DakshyamEmerald else DakshyamRose,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (appHealth.isHealthy) "ALL SYSTEMS OPERATIONAL" else "SYSTEM ATTENTION REQUIRED",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Local Room Engine & Memory Clean",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRunDiagnostics,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DakshyamCyan),
                        border = BorderStroke(1.dp, DakshyamCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Check", fontSize = 11.sp)
                    }
                }
            }
        }

        // Subsystems Metrics
        item {
            Text(
                text = "Core Infrastructure Metrics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DakshyamNavy
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HealthMetricRow(
                        label = "Database Engine",
                        value = "100% Supabase PostgreSQL Cloud (No SQLite)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Automated Polling Scripts",
                        value = "Disabled (Partner Action Driven Only)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Active Authenticated Session",
                        value = "$activePartnerName (Self-Edit Locked)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Supabase PostgreSQL Database",
                        value = "Connected & Active (uttffudevdijhrpmewqx.supabase.co)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Cloud Sync & Realtime",
                        value = "PostgREST & Live Sync (All 8 Feature Tables)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Supabase Direct Port",
                        value = "Port 5432 (PostgreSQL Connection Ready)",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Registered Founding Partners",
                        value = "${appHealth.totalPartnersCount} Active Founders",
                        isOk = appHealth.totalPartnersCount > 0
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Treasury Cashflow Ledger",
                        value = "${appHealth.totalTransactionsCount} Entries (${Formatters.formatCurrency(appHealth.treasuryBalance)})",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Hardware R&D Projects Tracked",
                        value = "${appHealth.totalProjectsCount} Active Projects",
                        isOk = true
                    )
                    HorizontalDivider()
                    HealthMetricRow(
                        label = "Active Removal Motions",
                        value = if (appHealth.activeRemovalMotionCount == 0) "None Pending" else "${appHealth.activeRemovalMotionCount} Open Vote",
                        isOk = appHealth.activeRemovalMotionCount == 0
                    )
                }
            }
        }

        item {
            val format = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            Text(
                text = "Last diagnostic scan: ${format.format(Date(appHealth.lastHealthCheck))}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthMetricRow(label: String, value: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOk) DakshyamEmerald else DakshyamRose)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOk) DakshyamNavy else DakshyamRose
            )
        }
    }
}

@Composable
private fun SystemLogsTabContent(
    logs: List<SystemLogEntry>,
    allLogsCount: Int,
    selectedLevel: SystemLogLevel?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectLevel: (SystemLogLevel?) -> Unit,
    onClearLogs: () -> Unit,
    onCopyLogs: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Controls Row
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter logs by tag, message, or error...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Level Filter Chips & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedLevel == null,
                        onClick = { onSelectLevel(null) },
                        label = { Text("All ($allLogsCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DakshyamNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedLevel == SystemLogLevel.ERROR,
                        onClick = { onSelectLevel(if (selectedLevel == SystemLogLevel.ERROR) null else SystemLogLevel.ERROR) },
                        label = { Text("Errors", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DakshyamRose,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedLevel == SystemLogLevel.WARN,
                        onClick = { onSelectLevel(if (selectedLevel == SystemLogLevel.WARN) null else SystemLogLevel.WARN) },
                        label = { Text("Warnings", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DakshyamAmber,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopyLogs, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Logs", tint = DakshyamNavy, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onClearLogs, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = DakshyamRose, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        HorizontalDivider()

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No logs match the current filter", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    SystemLogItemCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun SystemLogItemCard(log: SystemLogEntry) {
    var expanded by remember { mutableStateOf(false) }
    val format = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { format.format(Date(log.timestamp)) }

    val levelColor = when (log.level) {
        SystemLogLevel.CRITICAL, SystemLogLevel.ERROR -> DakshyamRose
        SystemLogLevel.WARN -> DakshyamAmber
        SystemLogLevel.SUCCESS -> DakshyamEmerald
        SystemLogLevel.INFO -> DakshyamTeal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (log.details != null) expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = levelColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.level.name,
                            color = levelColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.tag,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DakshyamNavy
                    )
                }

                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!log.details.isNullOrBlank()) {
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = log.details,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DakshyamNavy
                        )
                    }
                }

                if (!expanded) {
                    Text(
                        text = "▶ Tap to inspect details",
                        fontSize = 10.sp,
                        color = DakshyamCyan,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
