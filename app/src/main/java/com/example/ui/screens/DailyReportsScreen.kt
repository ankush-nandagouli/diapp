package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DailyReportEntity
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamSky
import com.example.ui.theme.DakshyamTeal
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.DakshyamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportsScreen(
    viewModel: DakshyamViewModel,
    modifier: Modifier = Modifier
) {
    val dailyReports by viewModel.dailyReports.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()

    var showAddReportDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Stats Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_reports_header_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Daily Progress Reports (DPR)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dailyReports.size} Logs Recorded",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DakshyamTeal.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = DakshyamCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Linked to active hardware & engineering projects. Tracks site activity, fabrication status, and media attachments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Engineering Logs Timeline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedButton(
                        onClick = { showAddReportDialog = true },
                        modifier = Modifier.testTag("log_progress_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Progress", fontSize = 13.sp)
                    }
                }
            }

            // Empty State
            if (dailyReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No daily reports submitted yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Log project milestones, hardware testing notes, and media proof.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Daily Report Cards
            items(dailyReports, key = { it.id }) { report ->
                DailyReportCard(
                    report = report,
                    onDelete = { viewModel.deleteDailyReport(report) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddReportDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_daily_report"),
            containerColor = DakshyamTeal,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Report")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log DPR", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Add Daily Report Dialog
    if (showAddReportDialog) {
        AddDailyReportDialog(
            viewModel = viewModel,
            projects = projects.map { it.id to it.title },
            partners = partners.map { it.name },
            defaultPartner = activePartner?.name ?: (partners.firstOrNull()?.name ?: ""),
            onDismiss = { showAddReportDialog = false },
            onConfirm = { projectId, projectTitle, author, summary, mediaUri, mediaType ->
                viewModel.addDailyReport(
                    projectId = projectId,
                    projectTitle = projectTitle,
                    reportedByPartner = author,
                    summary = summary,
                    mediaUri = mediaUri,
                    mediaType = mediaType
                )
                showAddReportDialog = false
            }
        )
    }
}

@Composable
fun DailyReportCard(
    report: DailyReportEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_report_card_${report.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Project Chip & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DakshyamNavy.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = report.projectTitle.ifEmpty { "General Engineering" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DakshyamNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = Formatters.formatDate(report.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Work Summary Text
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Cloudinary & Cloud Photo Proof if available
            if (report.mediaType == "PHOTO" && report.mediaUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = report.mediaUri,
                        contentDescription = "DPR Photo Proof",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Cloudinary Verified",
                            color = DakshyamCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Media & Author Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DakshyamTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = report.reportedByPartner.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DakshyamTeal
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reported by: ${report.reportedByPartner}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Media Attachment Badge & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (report.mediaType != "NONE" && report.mediaUri.isNotEmpty()) {
                        Surface(
                            color = DakshyamSky.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (report.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = DakshyamSky
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (report.mediaType == "VIDEO") "Video Attached" else "Photo Attached",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DakshyamSky
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Report",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDailyReportDialog(
    viewModel: DakshyamViewModel,
    projects: List<Pair<Long, String>>,
    partners: List<String>,
    defaultPartner: String,
    onDismiss: () -> Unit,
    onConfirm: (
        projectId: Long,
        projectTitle: String,
        author: String,
        summary: String,
        mediaUri: String,
        mediaType: String
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedProjectPair by remember {
        mutableStateOf(projects.firstOrNull() ?: (0L to "General / Dakshyam R&D"))
    }
    var author by remember { mutableStateOf(defaultPartner.ifEmpty { partners.firstOrNull() ?: "" }) }
    var summary by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("PHOTO") } // "PHOTO", "VIDEO", "NONE"
    var mediaUri by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            viewModel.uploadMedia(context, uri, folder = "dakshyam_dpr") { res ->
                isUploadingImage = false
                res.onSuccess { url ->
                    mediaUri = url
                    mediaType = "PHOTO"
                    Toast.makeText(context, "Proof attached successfully", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var projectExpanded by remember { mutableStateOf(false) }
    var authorExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Daily Progress Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Project Selector
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = projectExpanded,
                        onExpandedChange = { projectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProjectPair.second,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Associated Project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = projectExpanded,
                            onDismissRequest = { projectExpanded = false }
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.second) },
                                    onClick = {
                                        selectedProjectPair = proj
                                        projectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Author Selector
                ExposedDropdownMenuBox(
                    expanded = authorExpanded,
                    onExpandedChange = { authorExpanded = it }
                ) {
                    OutlinedTextField(
                        value = author,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reporting Partner") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = authorExpanded,
                        onDismissRequest = { authorExpanded = false }
                    ) {
                        partners.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    author = p
                                    authorExpanded = false
                                }
                            )
                        }
                    }
                }

                // Summary Text
                OutlinedTextField(
                    value = summary,
                    onValueChange = {
                        summary = it
                        summaryError = it.isBlank()
                    },
                    label = { Text("Work Completed / Progress Summary *") },
                    isError = summaryError,
                    supportingText = if (summaryError) { { Text("Summary required") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("input_report_summary"),
                    maxLines = 4
                )

                // Media Attachment Type
                Text("Media Attachment", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mediaType == "PHOTO",
                        onClick = { mediaType = "PHOTO" },
                        label = { Text("Photo") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mediaType == "VIDEO",
                        onClick = { mediaType = "VIDEO" },
                        label = { Text("Video") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mediaType == "NONE",
                        onClick = { mediaType = "NONE" },
                        label = { Text("None") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (mediaType == "PHOTO") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upload Proof / Site Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DakshyamNavy)
                                if (isUploadingImage) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            }
                            if (mediaUri.startsWith("http")) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    AsyncImage(
                                        model = mediaUri,
                                        contentDescription = "Uploaded proof preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (mediaUri.startsWith("http") || mediaUri.isNotBlank()) "Change Photo" else "Select & Upload Photo",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (mediaType != "NONE") {
                    OutlinedTextField(
                        value = mediaUri,
                        onValueChange = { mediaUri = it },
                        label = { Text("Storage URL / Proof Filename") },
                        placeholder = { Text(if (mediaType == "VIDEO") "e.g. test_run_v2.mp4" else "e.g. pcb_assembly.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (summary.isBlank()) {
                        summaryError = true
                        return@Button
                    }
                    val finalUri = if (mediaType != "NONE" && mediaUri.isBlank()) {
                        "dakshyam/${selectedProjectPair.first}/${if (mediaType == "VIDEO") "video_capture.mp4" else "photo_capture.jpg"}"
                    } else mediaUri

                    onConfirm(
                        selectedProjectPair.first,
                        selectedProjectPair.second,
                        author.ifEmpty { partners.firstOrNull() ?: "Authorized Partner" },
                        summary.trim(),
                        finalUri,
                        mediaType
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("confirm_submit_report_btn")
            ) {
                Text("Submit DPR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
