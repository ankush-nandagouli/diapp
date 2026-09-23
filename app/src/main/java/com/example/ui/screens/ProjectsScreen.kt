package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Slider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyReportEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProjectComponentEntity
import com.example.data.local.ProjectEntity
import com.example.ui.theme.DakshyamAmber
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamEmerald
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamTeal
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.DakshyamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ComponentDraft(
    var name: String = "",
    var quantity: String = "1",
    var unitPrice: String = "0"
) {
    val totalPrice: Double
        get() {
            val q = quantity.toIntOrNull() ?: 1
            val p = unitPrice.toDoubleOrNull() ?: 0.0
            return q * p
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: DakshyamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val allComponents by viewModel.allComponents.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val dailyReports by viewModel.dailyReports.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()

    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var projectForDprDialog by remember { mutableStateOf<ProjectEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("projects_summary_card"),
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
                                    text = "Active Projects & BOM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${projects.size} Projects Active",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(DakshyamTeal.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = DakshyamCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val totalBOMCost = allComponents.sumOf { it.totalPrice }
                        val totalBudget = projects.sumOf { it.budget }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Components Tracked", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("${allComponents.size} Items", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Hardware BOM Valuation", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text(Formatters.formatCurrency(totalBOMCost), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DakshyamCyan)
                            }
                        }
                    }
                }
            }

            // Projects List Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Engineering Projects & Components",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedButton(
                        onClick = { showCreateProjectDialog = true },
                        modifier = Modifier.testTag("add_project_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Project", fontSize = 13.sp)
                    }
                }
            }

            // Empty state
            if (projects.isEmpty()) {
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
                                Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No projects created yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create a project with dynamic components & prices (BOM).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Project Cards
            items(projects, key = { it.id }) { project ->
                val projectComponents = allComponents.filter { it.projectId == project.id }
                val projectReports = dailyReports.filter { it.projectId == project.id }
                ProjectCard(
                    project = project,
                    components = projectComponents,
                    reports = projectReports,
                    activePartnerName = activePartner?.name ?: "",
                    onStatusChange = { newStatus -> viewModel.updateProjectStatus(project, newStatus) },
                    onDelete = { viewModel.deleteProject(project) },
                    onUploadImage = { uri ->
                        viewModel.uploadImageToCloudinary(context, uri, folder = "dakshyam_projects") { res ->
                            res.onSuccess { url ->
                                viewModel.updateProject(project.copy(imageUrl = url))
                                Toast.makeText(context, "Project image uploaded to Cloudinary", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onLogDprClick = { projectForDprDialog = project },
                    onDeleteDpr = { rpt -> viewModel.deleteDailyReport(rpt) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateProjectDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_project"),
            containerColor = DakshyamNavy,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Project", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Dialog: Log Project Daily Progress Report (DPR)
    projectForDprDialog?.let { targetProject ->
        LogProjectDprDialog(
            project = targetProject,
            activePartner = activePartner,
            viewModel = viewModel,
            onDismiss = { projectForDprDialog = null },
            onSubmit = { progressPercent, newStatus, author, summary, mediaUri ->
                viewModel.logProjectDailyProgress(
                    projectId = targetProject.id,
                    projectTitle = targetProject.title,
                    progressPercent = progressPercent,
                    status = newStatus,
                    author = author,
                    summary = summary,
                    mediaUri = mediaUri,
                    mediaType = if (mediaUri.isNotBlank()) "PHOTO" else "NONE",
                    onComplete = {
                        Toast.makeText(context, "Daily Progress Report saved & attached to ${targetProject.title}", Toast.LENGTH_SHORT).show()
                    }
                )
                projectForDprDialog = null
            }
        )
    }

    // Dialog: Create Project with Dynamic Components Form, Cloudinary Image & Optional Kickoff DPR
    if (showCreateProjectDialog) {
        CreateProjectDialog(
            viewModel = viewModel,
            partners = partners.map { it.name },
            activePartnerName = activePartner?.name ?: "",
            onDismiss = { showCreateProjectDialog = false },
            onCreate = { title, client, budget, status, assigned, draftComponents, imageUrl, initialDprSummary, initialDprAuthor ->
                val entityComponents = draftComponents.map { draft ->
                    ProjectComponentEntity(
                        projectId = 0L,
                        name = draft.name.trim(),
                        quantity = draft.quantity.toIntOrNull() ?: 1,
                        unitPrice = draft.unitPrice.toDoubleOrNull() ?: 0.0,
                        totalPrice = draft.totalPrice
                    )
                }
                viewModel.createProject(
                    title = title,
                    client = client,
                    budget = budget,
                    status = status,
                    assignedPartners = assigned,
                    components = entityComponents,
                    imageUrl = imageUrl,
                    initialDprSummary = initialDprSummary,
                    initialDprAuthor = initialDprAuthor
                )
                showCreateProjectDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    components: List<ProjectComponentEntity>,
    reports: List<DailyReportEntity>,
    activePartnerName: String,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onUploadImage: (Uri) -> Unit = {},
    onLogDprClick: () -> Unit = {},
    onDeleteDpr: (DailyReportEntity) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var dprExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadImage(uri)
        }
    }

    val statusColor = when (project.status) {
        "In Progress" -> DakshyamCyan
        "Completed" -> DakshyamEmerald
        "Testing" -> DakshyamAmber
        "Planning" -> DakshyamTeal
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val totalComponentCost = components.sumOf { it.totalPrice }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Cloudinary Image Banner if present
            if (project.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    AsyncImage(
                        model = project.imageUrl,
                        contentDescription = "Project Blueprint",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Cloudinary CDN",
                            color = DakshyamCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Status Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Client: ${project.client} • Assigned: ${project.assignedPartners.ifEmpty { "All Partners" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Actions: Cloudinary upload & Status Dropdown
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_upload_image_project_${project.id}")
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Upload Blueprint to Cloudinary",
                                tint = DakshyamTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Box {
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { statusMenuExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = project.status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                    Icon(
                                        Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = statusColor
                                    )
                                }
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = statusMenuExpanded,
                                onDismissRequest = { statusMenuExpanded = false }
                            ) {
                                listOf("Planning", "In Progress", "Testing", "Completed", "On Hold").forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st) },
                                        onClick = {
                                            onStatusChange(st)
                                            statusMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(12.dp))

            // Budget vs BOM Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Allocated Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(project.budget), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Components Total (BOM)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(totalComponentCost), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DakshyamTeal))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Components section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DakshyamNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hardware Components (${components.size} items)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = DakshyamNavy
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DakshyamNavy
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (components.isEmpty()) {
                        Text(
                            text = "No components added yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        components.forEach { comp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = comp.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = "${comp.quantity}x @ ${Formatters.formatCurrency(comp.unitPrice)}/unit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Formatters.formatCurrency(comp.totalPrice),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expandable Daily Progress Reports (DPR) section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { dprExpanded = !dprExpanded }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DakshyamTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily Progress Reports (${reports.size} updates)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = DakshyamTeal
                    )
                }
                Icon(
                    imageVector = if (dprExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DakshyamTeal
                )
            }

            AnimatedVisibility(visible = dprExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reports.isEmpty()) {
                        Text(
                            text = "No daily progress reports recorded yet for this project.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                        reports.take(6).forEach { rpt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rpt.reportedByPartner,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = DakshyamNavy
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = dateFormat.format(Date(rpt.timestamp)),
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            IconButton(
                                                onClick = { onDeleteDpr(rpt) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete report",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rpt.summary,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (rpt.mediaUri.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            AsyncImage(
                                                model = rpt.mediaUri,
                                                contentDescription = "DPR Media Proof",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onLogDprClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Daily Work Report", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Update Daily Work (DPR) + Delete Project
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onLogDprClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_log_dpr_project_${project.id}")
                ) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DakshyamCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Update Daily Work", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Project",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogProjectDprDialog(
    project: ProjectEntity,
    activePartner: PartnerEntity?,
    viewModel: DakshyamViewModel,
    onDismiss: () -> Unit,
    onSubmit: (
        progressPercent: Int,
        newStatus: String,
        author: String,
        summary: String,
        mediaUri: String
    ) -> Unit
) {
    val context = LocalContext.current
    var progressPercent by remember { mutableStateOf(50) }
    var newStatus by remember { mutableStateOf(project.status) }
    var author by remember {
        mutableStateOf(activePartner?.name ?: project.assignedPartners.split(",").firstOrNull()?.trim() ?: "Assigned Partner")
    }
    var summary by remember { mutableStateOf("") }
    var mediaUri by remember { mutableStateOf("") }
    var isUploadingMedia by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingMedia = true
            viewModel.uploadImageToCloudinary(context, uri, folder = "dakshyam_dpr") { res ->
                isUploadingMedia = false
                res.onSuccess { url ->
                    mediaUri = url
                    Toast.makeText(context, "Proof uploaded to Cloudinary", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        tint = DakshyamTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Daily Progress (DPR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Project: ${project.title}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Assigned Author Tag
                item {
                    Surface(
                        color = DakshyamNavy.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = DakshyamNavy, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Reporting Partner (Assigned)", fontSize = 10.sp, color = Color.Gray)
                                Text(author, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DakshyamNavy)
                            }
                        }
                    }
                }

                // Project Status Selector
                item {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = newStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Update Milestone Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf("Planning", "In Progress", "Testing", "Completed", "On Hold").forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        newStatus = st
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Completion Progress Slider
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Completion Estimate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$progressPercent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DakshyamTeal)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = progressPercent.toFloat(),
                            onValueChange = { progressPercent = it.toInt() },
                            valueRange = 0f..100f,
                            steps = 19
                        )
                    }
                }

                // Daily Work Summary
                item {
                    OutlinedTextField(
                        value = summary,
                        onValueChange = {
                            summary = it
                            summaryError = it.isBlank()
                        },
                        label = { Text("Daily Work Completed *") },
                        placeholder = { Text("Describe today's achievements, hardware tests, firmware commits, or blockages...") },
                        isError = summaryError,
                        supportingText = if (summaryError) { { Text("Please describe today's work") } } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_dpr_summary"),
                        maxLines = 4
                    )
                }

                // Proof / Photo Upload via Cloudinary
                item {
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
                                Text("Attach Proof / Blueprint (Cloudinary)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (isUploadingMedia) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            }
                            if (mediaUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    AsyncImage(
                                        model = mediaUri,
                                        contentDescription = "Work Proof",
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
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (mediaUri.isBlank()) "Upload Proof Image" else "Change Proof Image", fontSize = 12.sp)
                            }
                        }
                    }
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
                    onSubmit(progressPercent, newStatus, author, summary.trim(), mediaUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("submit_dpr_btn")
            ) {
                Text("Submit Daily Work")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    viewModel: DakshyamViewModel,
    partners: List<String>,
    activePartnerName: String = "",
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        client: String,
        budget: Double,
        status: String,
        assigned: String,
        components: List<ComponentDraft>,
        imageUrl: String,
        initialDprSummary: String,
        initialDprAuthor: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("Dakshyam Internal R&D") }
    var budgetText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("In Progress") }
    var assignedPartners by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var initialDprSummary by remember { mutableStateOf("") }
    var initialDprAuthor by remember {
        mutableStateOf(activePartnerName.ifBlank { partners.firstOrNull() ?: "" })
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            viewModel.uploadMedia(context, uri, folder = "dakshyam_projects") { res ->
                isUploadingImage = false
                res.onSuccess { url ->
                    imageUrl = url
                    Toast.makeText(context, "Blueprint attached successfully", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Dynamic Components List
    val draftComponents = remember {
        mutableStateListOf(
            ComponentDraft(name = "Microcontroller Board", quantity = "2", unitPrice = "1200"),
            ComponentDraft(name = "Sensors & Wiring Harness", quantity = "1", unitPrice = "3500")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Project & Hardware BOM", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = it.isBlank()
                        },
                        label = { Text("Project Title *") },
                        isError = titleError,
                        supportingText = if (titleError) { { Text("Title required") } } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_project_title"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client / Initiative") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Total Budget (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = assignedPartners,
                        onValueChange = { assignedPartners = it },
                        label = { Text("Assigned Partners (Comma-separated names)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Cloudinary Project Blueprint / Photo Upload
                item {
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
                                Text(
                                    text = "Project Blueprint / Photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DakshyamNavy
                                )
                                if (isUploadingImage) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            }
                            if (imageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Uploaded blueprint",
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
                                    text = if (imageUrl.isNotEmpty()) "Change Blueprint Image" else "Upload Blueprint Image",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Initial Daily Progress Report (DPR) Kickoff
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DakshyamTeal.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = DakshyamTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Attach Initial DPR (Daily Progress Report)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DakshyamNavy
                                )
                            }
                            Text(
                                text = "Attach the day-1 kickoff work report so assigned partners can start tracking immediately.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = initialDprAuthor,
                                onValueChange = { initialDprAuthor = it },
                                label = { Text("Report Author / Lead (e.g. Assigned Partner)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = initialDprSummary,
                                onValueChange = { initialDprSummary = it },
                                label = { Text("Day 1 Kickoff Summary (Optional)") },
                                placeholder = { Text("e.g., Initial requirements gathered, BOM components drafted...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                maxLines = 3
                            )
                        }
                    }
                }

                // Dynamic Components Section
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Components & Bill of Materials",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = {
                                draftComponents.add(ComponentDraft())
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Component", tint = DakshyamTeal)
                        }
                    }
                }

                items(draftComponents.indices.toList(), key = { it }) { index ->
                    val comp = draftComponents[index]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Component #${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DakshyamNavy)
                                if (draftComponents.size > 1) {
                                    IconButton(
                                        onClick = { draftComponents.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = comp.name,
                                onValueChange = { comp.name = it },
                                label = { Text("Part Name / Spec", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = comp.quantity,
                                    onValueChange = { comp.quantity = it },
                                    label = { Text("Qty", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = comp.unitPrice,
                                    onValueChange = { comp.unitPrice = it },
                                    label = { Text("Unit Price", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Item Subtotal: ${Formatters.formatCurrency(comp.totalPrice)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DakshyamTeal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val budget = budgetText.toDoubleOrNull() ?: 0.0
                    onCreate(
                        title.trim(),
                        client.trim(),
                        budget,
                        status,
                        assignedPartners.trim(),
                        draftComponents.filter { it.name.isNotBlank() },
                        imageUrl,
                        initialDprSummary.trim(),
                        initialDprAuthor.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("confirm_create_project_btn")
            ) {
                Text("Launch Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
