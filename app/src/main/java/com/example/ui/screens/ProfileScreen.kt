package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CompanyProfileEntity
import com.example.data.local.PartnerEntity
import com.example.ui.components.SupabaseCloudSyncCard
import com.example.ui.components.SystemHealthLogsDialog
import com.example.ui.theme.DakshyamAmber
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamEmerald
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamSky
import com.example.ui.theme.DakshyamTeal
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.DakshyamViewModel

@Composable
fun ProfileScreen(
    viewModel: DakshyamViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activePartner by viewModel.activePartner.collectAsState()
    val companyProfile by viewModel.companyProfile.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    var isCheckingConnection by remember { mutableStateOf(false) }

    var showSystemLogsDialog by remember { mutableStateOf(false) }
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    var isPurging by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isUploadingLogo by remember { mutableStateOf(false) }
    var showLogoUrlDialog by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var partnerToEdit by remember { mutableStateOf<PartnerEntity?>(null) }
    var partnerForPhotoUpload by remember { mutableStateOf<PartnerEntity?>(null) }

    // Photo Picker for any selected partner
    val specificPartnerAvatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val target = partnerForPhotoUpload
        if (uri != null && target != null) {
            isUploadingAvatar = true
            viewModel.uploadPartnerAvatar(context, target, uri) { result ->
                isUploadingAvatar = false
                if (result.isSuccess) {
                    Toast.makeText(context, "${target.name}'s photo updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
                partnerForPhotoUpload = null
            }
        } else {
            partnerForPhotoUpload = null
        }
    }

    // Company Profile Form State
    var companyName by remember(companyProfile) { mutableStateOf(companyProfile?.companyName ?: "Dakshyam Innovations") }
    var tagline by remember(companyProfile) { mutableStateOf(companyProfile?.tagline ?: "Engineering Next-Gen Robotics & Intelligent Systems") }
    var regNumber by remember(companyProfile) { mutableStateOf(companyProfile?.registrationNumber ?: "U72900MH2024PTC123456") }
    var gstin by remember(companyProfile) { mutableStateOf(companyProfile?.gstin ?: "27AABCD1234E1Z5") }
    var email by remember(companyProfile) { mutableStateOf(companyProfile?.officialEmail ?: "contact@dakshyam.com") }
    var phone by remember(companyProfile) { mutableStateOf(companyProfile?.officialPhone ?: "+91 98765 43210") }
    var address by remember(companyProfile) { mutableStateOf(companyProfile?.officeAddress ?: "Technology Incubation Park, Suite 402, India") }
    var website by remember(companyProfile) { mutableStateOf(companyProfile?.website ?: "https://dakshyam.com") }

    // Cloudinary Photo Pickers
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingLogo = true
            viewModel.uploadBusinessLogo(context, uri) { result ->
                isUploadingLogo = false
                if (result.isSuccess) {
                    Toast.makeText(context, "Corporate logo uploaded & synchronized across all partner phones!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && activePartner != null) {
            isUploadingAvatar = true
            viewModel.uploadPartnerAvatar(context, activePartner!!, uri) { result ->
                isUploadingAvatar = false
                if (result.isSuccess) {
                    Toast.makeText(context, "Partner photo updated & synchronized across devices!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Device Cloud Synchronization Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_multi_device_sync"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, DakshyamTeal.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isCloudSyncing) DakshyamAmber.copy(alpha = 0.2f) else DakshyamTeal.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCloudSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = DakshyamAmber
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = "Sync",
                                        tint = DakshyamTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isCloudSyncing) "Syncing Across Partner Phones..." else "Live Multi-Device Cloud Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DakshyamNavy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isCloudSyncing) DakshyamAmber else DakshyamTeal)
                                )
                            }
                            Text(
                                text = "Logo & partner updates reflect on all partners' devices instantly",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.triggerManualSync {
                                Toast.makeText(context, "Synced latest profile & logo from cloud", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_manual_sync_profile"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 12.sp)
                    }
                }
            }
        }
        // Active Partner Identity Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_partner_card"),
                colors = CardDefaults.cardColors(containerColor = DakshyamNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(DakshyamCyan.copy(alpha = 0.2f))
                                    .clickable {
                                        avatarPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!activePartner?.avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = activePartner?.avatarUrl,
                                        contentDescription = "Partner Avatar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = (activePartner?.name ?: "P").take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = DakshyamCyan,
                                        fontSize = 22.sp
                                    )
                                }
                                if (isUploadingAvatar) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = DakshyamCyan,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = activePartner?.name ?: "Founding Partner",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = activePartner?.role ?: "Partner",
                                    fontSize = 13.sp,
                                    color = DakshyamCyan
                                )
                                Text(
                                    text = activePartner?.email ?: "corporate@dakshyam.com",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            color = DakshyamCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DakshyamCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("badge_partner_session_locked")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Session Locked",
                                    tint = DakshyamCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Active Session", fontSize = 11.sp, color = DakshyamCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capital Equity Contributed:",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = Formatters.formatCurrency(activePartner?.capitalContributed ?: 0.0),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DakshyamEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { partnerToEdit = activePartner },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_edit_my_profile"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_change_my_password"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DakshyamCyan),
                            border = BorderStroke(1.dp, DakshyamCyan.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            avatarPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_upload_partner_avatar"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Partner Photo (Cloudinary)", fontSize = 13.sp)
                    }
                }
            }
        }

        // Business Logo & Brand Visuals (Cloudinary Integration)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("business_logo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Business Logo & Corporate Identity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DakshyamNavy
                            )
                            Text(
                                text = "Cloudinary Cloud Storage Integration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DakshyamCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Cloudinary Active",
                                color = DakshyamTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Logo Thumbnail
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.5.dp, DakshyamCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!companyProfile?.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = companyProfile?.logoUrl,
                                        contentDescription = "Dakshyam Logo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = DakshyamNavy,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                if (isUploadingLogo) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = DakshyamCyan,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (!companyProfile?.logoUrl.isNullOrEmpty()) "Custom Logo Configured" else "Default Vector Logo",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "All partners have equal rights to upload and update the official business logo.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        logoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    enabled = !isUploadingLogo,
                                    colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_upload_business_logo")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload File", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showLogoUrlDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_set_logo_url")
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = DakshyamTeal)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Put Logo URL", fontSize = 12.sp, color = DakshyamTeal)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Business Details & Corporate Entity Profile
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("business_details_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Company Details & Governance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DakshyamNavy
                        )
                        Icon(Icons.Default.Security, contentDescription = null, tint = DakshyamCyan, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "Authorized for ${partners.joinToString(", ") { it.name }.ifEmpty { "Registered Partners" }}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Corporate Vision") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = regNumber,
                            onValueChange = { regNumber = it },
                            label = { Text("CIN / Reg Number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = { Text("GSTIN") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Official Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Official Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Office Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Corporate Website") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (companyProfile != null) {
                        Text(
                            text = "Last updated by ${companyProfile?.updatedByPartner ?: "Partner"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            val updated = (companyProfile ?: CompanyProfileEntity()).copy(
                                companyName = companyName.trim(),
                                tagline = tagline.trim(),
                                registrationNumber = regNumber.trim(),
                                gstin = gstin.trim(),
                                officialEmail = email.trim(),
                                officialPhone = phone.trim(),
                                officeAddress = address.trim(),
                                website = website.trim()
                            )
                            viewModel.updateCompanyProfile(updated) {
                                Toast.makeText(context, "Company profile saved successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_company_profile"),
                        colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Business Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Partner Registry Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Founding Partners (${partners.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DakshyamNavy
                        )
                        OutlinedButton(
                            onClick = { showAddPartnerDialog = true },
                            modifier = Modifier.testTag("btn_add_partner_profile"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, DakshyamNavy.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DakshyamNavy)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Partner", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DakshyamNavy)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    partners.forEach { partner ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (partner.id == activePartner?.id) DakshyamCyan.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (partner.avatarUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = partner.avatarUrl,
                                                contentDescription = partner.name,
                                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Text(
                                                text = partner.name.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (partner.id == activePartner?.id) DakshyamNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = partner.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        if (partner.id == activePartner?.id) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = DakshyamEmerald.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    color = DakshyamEmerald,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${partner.role} • ${partner.email}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (partner.phone.isNotEmpty()) {
                                        Text(
                                            text = partner.phone,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Partner Actions: If self, allow photo upload & edit profile. If other, show Protected lock badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (partner.id == activePartner?.id) {
                                    IconButton(
                                        onClick = {
                                            partnerForPhotoUpload = partner
                                            specificPartnerAvatarPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CloudUpload,
                                            contentDescription = "Upload Photo for ${partner.name}",
                                            tint = DakshyamTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { partnerToEdit = partner },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("btn_edit_partner_${partner.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit My Profile",
                                            tint = DakshyamNavy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Protected",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Protected",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }

        // Supabase Cloud Database Live Sync Card
        item {
            SupabaseCloudSyncCard(viewModel = viewModel)
        }

        // System Health & Error Logs Access Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_system_health_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, DakshyamTeal.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "System Health & Error Logs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DakshyamNavy
                        )
                        Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = DakshyamTeal, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Real-time inspection of database connectivity, diagnostic metrics, errors, warnings, and audit events.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showSystemLogsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_view_system_logs_profile"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DakshyamNavy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MonitorHeart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Diagnostics & System Logs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Database Purge & Cloud Administration (Requested Feature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("database_management_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, DakshyamRose.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Database Administration (Danger Zone)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DakshyamRose
                        )
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DakshyamRose, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Automated polling loops and background scripts have been removed. Updates only execute when a partner saves changes. You can manually synchronize with cloud or purge all database records below.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Supabase Cloud Connectivity Checker
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (connectionStatus?.isConnected == true) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (connectionStatus?.isConnected == true) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (connectionStatus?.isConnected == true) Color(0xFF10B981) else Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (connectionStatus == null) "Supabase: Checking Connection..."
                                               else if (connectionStatus?.isConnected == true) "Supabase: Connected (${connectionStatus?.latencyMs}ms)"
                                               else "Supabase: Offline (${connectionStatus?.message})",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (connectionStatus?.isConnected == true) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        isCheckingConnection = true
                                        viewModel.verifySupabaseConnection { status ->
                                            isCheckingConnection = false
                                            Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isCheckingConnection,
                                    modifier = Modifier.testTag("btn_ping_supabase_api")
                                ) {
                                    if (isCheckingConnection) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ping API", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "REST PostgREST & WebSocket Realtime Endpoint: https://uttffudevdijhrpmewqx.supabase.co",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.syncAllFromCloud {
                                    Toast.makeText(context, "Data synchronized from Supabase Cloud Database", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_sync_from_cloud"),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isCloudSyncing && !isPurging
                        ) {
                            if (isCloudSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Cloud", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showPurgeConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_purge_database"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DakshyamRose,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isPurging
                        ) {
                            if (isPurging) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge All Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Session & Logout Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, DakshyamRose.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Security & Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DakshyamRose
                    )
                    Text(
                        text = "Signed in as ${activePartner?.name ?: "Partner"}. Logging out revokes active session and returns to login gate.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_logout_profile"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DakshyamRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of Dakshyam Partner Access", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLogoUrlDialog) {
        SetLogoUrlDialog(
            currentUrl = companyProfile?.logoUrl ?: "",
            onDismiss = { showLogoUrlDialog = false },
            onConfirm = { url ->
                showLogoUrlDialog = false
                viewModel.updateBusinessLogoUrl(url) {
                    Toast.makeText(context, "Official company logo updated & synced across all partner phones!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showSystemLogsDialog) {
        SystemHealthLogsDialog(
            viewModel = viewModel,
            onDismiss = { showSystemLogsDialog = false }
        )
    }

    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isPurging) showPurgeConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DakshyamRose,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Purge All Database Records?",
                    fontWeight = FontWeight.Bold,
                    color = DakshyamRose
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will permanently wipe all stored data from your Supabase PostgreSQL cloud database:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• All Projects and BOM component records\n• All Daily Progress Reports (DPR)\n• All Cash Flow transactions and ledger entries\n• All Activity alerts and System logs\n• Resets partner capital & withdrawal balances to ₹0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This action is irreversible. All partners will immediately see empty collections.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DakshyamRose
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isPurging = true
                        viewModel.purgeEntireDatabase { success, message ->
                            isPurging = false
                            showPurgeConfirmDialog = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DakshyamRose,
                        contentColor = Color.White
                    ),
                    enabled = !isPurging,
                    modifier = Modifier.testTag("btn_confirm_purge_database")
                ) {
                    if (isPurging) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Purging...")
                    } else {
                        Text("Yes, Purge Everything")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPurgeConfirmDialog = false },
                    enabled = !isPurging
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your founding partner session?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.signOutPartner()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DakshyamRose)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddPartnerDialog) {
        AddPartnerFromProfileDialog(
            onDismiss = { showAddPartnerDialog = false },
            onSave = { newPartner ->
                viewModel.savePartner(newPartner) {
                    Toast.makeText(context, "${newPartner.name} enrolled as Founding Partner", Toast.LENGTH_SHORT).show()
                }
                showAddPartnerDialog = false
            }
        )
    }

    if (showChangePasswordDialog && activePartner != null) {
        ChangePartnerPasswordDialog(
            partner = activePartner!!,
            onDismiss = { showChangePasswordDialog = false },
            onUpdatePassword = { newPwd ->
                viewModel.updatePartnerPassword(activePartner!!.id, newPwd) { success ->
                    if (success) {
                        Toast.makeText(context, "Password updated. Default 427752 is permanently deactivated.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
                showChangePasswordDialog = false
            }
        )
    }

    partnerToEdit?.let { targetPartner ->
        EditPartnerDetailsDialog(
            partner = targetPartner,
            onDismiss = { partnerToEdit = null },
            onSave = { updated ->
                viewModel.savePartner(updated) {
                    Toast.makeText(context, "${updated.name}'s profile details updated", Toast.LENGTH_SHORT).show()
                }
                partnerToEdit = null
            }
        )
    }
}

@Composable
fun AddPartnerFromProfileDialog(
    onDismiss: () -> Unit,
    onSave: (PartnerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var capitalText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enroll New Founding Partner",
                fontWeight = FontWeight.Bold,
                color = DakshyamNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = DakshyamCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "New partner will be enrolled with temporary security credentials (default password: 427752). They must choose a private password upon first sign-in.",
                        fontSize = 12.sp,
                        color = DakshyamNavy,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Full Name *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Designation / Role") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Official Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = capitalText,
                    onValueChange = { capitalText = it },
                    label = { Text("Capital Contributed (INR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val cap = capitalText.toDoubleOrNull() ?: 0.0
                    val newPartner = PartnerEntity(
                        name = name.trim(),
                        role = role.trim().ifEmpty { "Partner" },
                        email = email.trim(),
                        phone = phone.trim(),
                        capitalContributed = cap,
                        password = "427752",
                        mustChangePassword = true
                    )
                    onSave(newPartner)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("btn_confirm_add_partner_profile")
            ) {
                Text("Enroll Partner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePartnerPasswordDialog(
    partner: PartnerEntity,
    onDismiss: () -> Unit,
    onUpdatePassword: (String) -> Unit
) {
    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavy, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Security Password", fontWeight = FontWeight.Bold, color = DakshyamNavy)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Setting a new personal password permanently deactivates the default password (427752).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMsg != null) {
                    Surface(
                        color = DakshyamRose.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg!!,
                            color = DakshyamRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = currentPwd,
                    onValueChange = {
                        currentPwd = it
                        errorMsg = null
                    },
                    label = { Text("Current Password") },
                    visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPwd,
                    onValueChange = {
                        newPwd = it
                        errorMsg = null
                    },
                    label = { Text("New Password") },
                    visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = {
                        confirmPwd = it
                        errorMsg = null
                    },
                    label = { Text("Confirm New Password") },
                    trailingIcon = {
                        IconButton(onClick = { pwdVisible = !pwdVisible }) {
                            Icon(
                                imageVector = if (pwdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPwd.trim() != partner.password) {
                        errorMsg = "Current password is incorrect."
                        return@Button
                    }
                    if (newPwd.trim().length < 4) {
                        errorMsg = "New password must be at least 4 characters."
                        return@Button
                    }
                    if (newPwd.trim() == "427752") {
                        errorMsg = "You cannot use the temporary default password (427752). Choose a unique password."
                        return@Button
                    }
                    if (newPwd.trim() != confirmPwd.trim()) {
                        errorMsg = "New password confirmation does not match."
                        return@Button
                    }
                    onUpdatePassword(newPwd.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy)
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPartnerDetailsDialog(
    partner: PartnerEntity,
    onDismiss: () -> Unit,
    onSave: (PartnerEntity) -> Unit
) {
    var name by remember { mutableStateOf(partner.name) }
    var role by remember { mutableStateOf(partner.role) }
    var email by remember { mutableStateOf(partner.email) }
    var phone by remember { mutableStateOf(partner.phone) }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit My Profile Details",
                fontWeight = FontWeight.Bold,
                color = DakshyamNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update your partner contact and identity information.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Designation") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Corporate Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Change Password (optional)") },
                    placeholder = { Text("Leave empty to retain current password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "Note: Changing your password permanently disables the default password (427752).",
                    fontSize = 11.sp,
                    color = DakshyamTeal
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalPassword = if (newPassword.isNotBlank()) newPassword.trim() else partner.password
                        val updated = partner.copy(
                            name = name.trim(),
                            role = role.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            password = finalPassword,
                            mustChangePassword = if (newPassword.isNotBlank()) false else partner.mustChangePassword
                        )
                        onSave(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy)
            ) {
                Text("Save My Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetLogoUrlDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit
) {
    var urlText by remember { mutableStateOf(currentUrl) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = DakshyamTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Official Logo by URL", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter the direct web URL of the company logo (PNG, JPG, SVG or Cloudinary link). It will instantly update across all partners' devices.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        error = false
                    },
                    label = { Text("Logo Image URL *") },
                    placeholder = { Text("https://example.com/logo.png") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                    isError = error,
                    supportingText = if (error) { { Text("Please enter a valid URL (starting with http:// or https://)") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_logo_url"),
                    singleLine = true
                )

                if (urlText.isNotBlank() && (urlText.startsWith("http://") || urlText.startsWith("https://"))) {
                    Text(
                        text = "Logo Preview:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = urlText.trim(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = urlText.trim()
                    if (trimmed.isBlank() || (!trimmed.startsWith("http://") && !trimmed.startsWith("https://"))) {
                        error = true
                        return@Button
                    }
                    onConfirm(trimmed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("btn_confirm_logo_url")
            ) {
                Text("Save & Sync Logo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

