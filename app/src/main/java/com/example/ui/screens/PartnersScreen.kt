package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.data.local.PartnerEntity
import com.example.data.model.PartnerRemovalMotion
import com.example.data.model.RemovalMotionStatus
import com.example.ui.theme.DakshyamAmber
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamEmerald
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamRose
import com.example.ui.theme.DakshyamTeal
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.DakshyamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersScreen(
    viewModel: DakshyamViewModel,
    modifier: Modifier = Modifier
) {
    val partners by viewModel.partners.collectAsState()
    val totalCapital by viewModel.totalCapital.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()
    val activeRemovalMotion by viewModel.activeRemovalMotion.collectAsState()
    val context = LocalContext.current

    var showAddPartnerDialog by remember { mutableStateOf(false) }
    var partnerToEdit by remember { mutableStateOf<PartnerEntity?>(null) }
    var partnerForCapitalInjection by remember { mutableStateOf<PartnerEntity?>(null) }
    var partnerForRemovalProposal by remember { mutableStateOf<PartnerEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Removal Resolution Voting Banner (if motion active)
            activeRemovalMotion?.let { motion ->
                item {
                    ActiveRemovalMotionCard(
                        motion = motion,
                        activePartner = activePartner,
                        onVote = { approve ->
                            activePartner?.let { voter ->
                                viewModel.castRemovalVote(motion.id, voter, approve)
                            }
                        },
                        onCancel = {
                            viewModel.cancelRemovalMotion(motion.id)
                        }
                    )
                }
            }

            // Header Stats Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_capital_summary_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Total Founder Money Collected",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(totalCapital),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DakshyamTeal.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Total Capital",
                                    tint = DakshyamCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Active Partners",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "${partners.count { it.isActive }} Registered",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Active Profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = activePartner?.name ?: "None selected",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DakshyamCyan
                                )
                            }
                        }
                    }
                }
            }

            // Capital Distribution Bar
            if (totalCapital > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Capital Equity Breakdown",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            partners.forEach { partner ->
                                val pct = if (totalCapital > 0) (partner.capitalContributed / totalCapital).toFloat() else 0f
                                val pctText = "%.1f%%".format(pct * 100)
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = partner.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${Formatters.formatCurrency(partner.capitalContributed)} ($pctText)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DakshyamTeal
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { pct.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = DakshyamTeal,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Founding Partners & Stakeholders",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedButton(
                        onClick = { showAddPartnerDialog = true },
                        modifier = Modifier.testTag("add_dynamic_partner_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Field", fontSize = 13.sp)
                    }
                }
            }

            // Partner Cards
            items(partners, key = { it.id }) { partner ->
                val isSelf = activePartner?.id == partner.id
                PartnerCard(
                    partner = partner,
                    isActiveProfile = isSelf,
                    onEdit = {
                        if (isSelf) {
                            partnerToEdit = partner
                        }
                    },
                    onAddCapital = { partnerForCapitalInjection = partner },
                    onProposeRemoval = { partnerForRemovalProposal = partner }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddPartnerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_partner"),
            containerColor = DakshyamNavy,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Partner")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Partner", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Dialog: Add/Edit Dynamic Partner Form
    if (showAddPartnerDialog || partnerToEdit != null) {
        val editing = partnerToEdit != null
        val target = partnerToEdit ?: PartnerEntity(name = "")
        PartnerFormDialog(
            initialPartner = target,
            isEditing = editing,
            onDismiss = {
                showAddPartnerDialog = false
                partnerToEdit = null
            },
            onUploadAvatar = { uri, callback ->
                viewModel.uploadImageToCloudinary(context, uri, "dakshyam_partners", callback)
            },
            onSave = { partner ->
                viewModel.savePartner(partner)
                showAddPartnerDialog = false
                partnerToEdit = null
            }
        )
    }

    // Dialog: Capital Injection
    if (partnerForCapitalInjection != null) {
        val targetPartner = partnerForCapitalInjection!!
        CapitalInjectionDialog(
            partner = targetPartner,
            onDismiss = { partnerForCapitalInjection = null },
            onConfirm = { amount, note ->
                viewModel.recordCapitalInjection(targetPartner, amount, note)
                Toast.makeText(
                    context,
                    "Deposit of ${Formatters.formatCurrency(amount)} for ${targetPartner.name} synced to cloud!",
                    Toast.LENGTH_SHORT
                ).show()
                partnerForCapitalInjection = null
            }
        )
    }

    // Dialog: Propose Partner Removal (Majority Vote)
    if (partnerForRemovalProposal != null) {
        ProposePartnerRemovalDialog(
            targetPartner = partnerForRemovalProposal!!,
            proposer = activePartner,
            onDismiss = { partnerForRemovalProposal = null },
            onPropose = { reason ->
                activePartner?.let { proposer ->
                    viewModel.proposePartnerRemoval(partnerForRemovalProposal!!, proposer, reason)
                }
                partnerForRemovalProposal = null
            }
        )
    }
}

@Composable
fun PartnerCard(
    partner: PartnerEntity,
    isActiveProfile: Boolean,
    onEdit: () -> Unit,
    onAddCapital: () -> Unit,
    onProposeRemoval: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("partner_card_${partner.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveProfile) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActiveProfile) DakshyamNavy else DakshyamTeal.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (partner.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = partner.avatarUrl,
                                contentDescription = "${partner.name} Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = partner.name.take(2).uppercase(),
                                color = if (isActiveProfile) Color.White else DakshyamNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = partner.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (isActiveProfile) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = DakshyamTeal,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Active Profile",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = partner.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isActiveProfile) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_edit_self_${partner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit My Profile",
                            tint = DakshyamNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Contact & Details
            if (partner.email.isNotEmpty() || partner.phone.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (partner.email.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = partner.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    if (partner.phone.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = partner.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Financial Contribution Tally
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Capital Contribution",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatCurrency(partner.capitalContributed),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DakshyamNavy
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddCapital,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DakshyamTeal,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (!isActiveProfile) {
                            OutlinedButton(
                                onClick = onProposeRemoval,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DakshyamRose),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DakshyamRose.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vote to Remove", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveRemovalMotionCard(
    motion: PartnerRemovalMotion,
    activePartner: PartnerEntity?,
    onVote: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_removal_motion_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF1F2)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DakshyamRose.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HowToVote,
                        contentDescription = null,
                        tint = DakshyamRose,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Partner Removal Resolution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DakshyamNavy
                    )
                }

                Surface(
                    color = when (motion.status) {
                        RemovalMotionStatus.ACTIVE -> DakshyamAmber.copy(alpha = 0.2f)
                        RemovalMotionStatus.PASSED, RemovalMotionStatus.APPROVED -> DakshyamRose.copy(alpha = 0.2f)
                        RemovalMotionStatus.REJECTED -> DakshyamEmerald.copy(alpha = 0.2f)
                        RemovalMotionStatus.CANCELLED -> Color.LightGray.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = motion.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (motion.status) {
                            RemovalMotionStatus.ACTIVE -> Color(0xFFB45309)
                            RemovalMotionStatus.PASSED, RemovalMotionStatus.APPROVED -> DakshyamRose
                            RemovalMotionStatus.REJECTED -> DakshyamEmerald
                            RemovalMotionStatus.CANCELLED -> Color.DarkGray
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Motion to remove ${motion.targetPartnerName} from the firm.",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DakshyamNavy
            )
            Text(
                text = "Proposed by: ${motion.proposedByPartnerName} • Reason: \"${motion.reason}\"",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            val approvalVotes = motion.votes.values.count { it }
            val rejectionVotes = motion.votes.values.count { !it }
            val required = motion.requiredMajority

            Text(
                text = "Majority Rule: $approvalVotes approved / $rejectionVotes against (Requires $required approvals)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DakshyamNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            val totalEligible = motion.eligiblePartnersCount.coerceAtLeast(1)
            LinearProgressIndicator(
                progress = { (approvalVotes.toFloat() / totalEligible).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = DakshyamRose,
                trackColor = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (motion.votes.isNotEmpty()) {
                Text(
                    text = "Ballots Cast So Far:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DakshyamNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    motion.votes.forEach { (voterId, voteApproved) ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (voteApproved) DakshyamRose.copy(alpha = 0.15f) else DakshyamEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Partner #$voterId: ${if (voteApproved) "Approved" else "Retain"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (voteApproved) DakshyamRose else DakshyamEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            val canVote = motion.status == RemovalMotionStatus.ACTIVE &&
                    activePartner != null &&
                    activePartner.id != motion.targetPartnerId &&
                    !motion.votes.containsKey(activePartner.id)

            if (canVote) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onVote(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = DakshyamRose),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vote to Remove", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onVote(false) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vote to Retain", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (motion.status == RemovalMotionStatus.ACTIVE && motion.votes.containsKey(activePartner?.id)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Your vote has been recorded on this resolution.",
                    fontSize = 11.sp,
                    color = DakshyamEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (motion.status == RemovalMotionStatus.ACTIVE && activePartner?.id == motion.proposedByPartnerId) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Withdraw Motion", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProposePartnerRemovalDialog(
    targetPartner: PartnerEntity,
    proposer: PartnerEntity?,
    onDismiss: () -> Unit,
    onPropose: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = DakshyamRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Propose Partner Removal", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Under partnership governance, removing ${targetPartner.name} requires an open motion and a majority vote (${((4 / 2) + 1)} partners) to approve removal.",
                    fontSize = 13.sp,
                    color = DakshyamNavy
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        error = it.isBlank()
                    },
                    label = { Text("Stated Cause / Reason *") },
                    placeholder = { Text("e.g. Failure to fulfill capital commitment, breach of deed") },
                    isError = error,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        error = true
                        return@Button
                    }
                    onPropose(reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamRose)
            ) {
                Text("Submit for Majority Vote", fontWeight = FontWeight.Bold)
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
fun PartnerFormDialog(
    initialPartner: PartnerEntity,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onUploadAvatar: (Uri, (Result<String>) -> Unit) -> Unit = { _, _ -> },
    onSave: (PartnerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialPartner.name) }
    var role by remember { mutableStateOf(initialPartner.role) }
    var email by remember { mutableStateOf(initialPartner.email) }
    var phone by remember { mutableStateOf(initialPartner.phone) }
    var avatarUrl by remember { mutableStateOf(initialPartner.avatarUrl) }
    var capitalText by remember { mutableStateOf(if (initialPartner.capitalContributed > 0) initialPartner.capitalContributed.toString() else "") }
    var nameError by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAvatar = true
            onUploadAvatar(uri) { result ->
                isUploadingAvatar = false
                result.onSuccess { uploadedUrl ->
                    avatarUrl = uploadedUrl
                    Toast.makeText(context, "Partner photo updated & synced!", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Upload issue: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit My Partner Profile" else "Enroll New Founding Partner",
                fontWeight = FontWeight.Bold,
                color = DakshyamNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isEditing) {
                    Surface(
                        color = DakshyamCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "New partner will be enrolled with temporary security credentials (default password: 427752). They will be prompted to set a private password on first sign in.",
                            fontSize = 12.sp,
                            color = DakshyamNavy,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Avatar / Profile Photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DakshyamTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Partner Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = DakshyamNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (isUploadingAvatar) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uploading...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (avatarUrl.isNotBlank()) "Change Photo" else "Upload Photo", fontSize = 12.sp)
                        }
                    }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_partner_name"),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = capitalText,
                    onValueChange = { capitalText = it },
                    label = { Text("Capital Contributed (INR)") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (isEditing) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Change Password (optional)") },
                        placeholder = { Text("Leave blank to keep current password") },
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
                    val finalPassword = when {
                        isEditing && passwordInput.isNotBlank() -> passwordInput.trim()
                        isEditing -> initialPartner.password
                        else -> "427752"
                    }
                    val updated = initialPartner.copy(
                        name = name.trim(),
                        role = role.trim().ifEmpty { "Partner" },
                        email = email.trim(),
                        phone = phone.trim(),
                        avatarUrl = avatarUrl.trim(),
                        capitalContributed = cap,
                        password = finalPassword,
                        mustChangePassword = if (isEditing && passwordInput.isNotBlank()) false else initialPartner.mustChangePassword
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("save_partner_button")
            ) {
                Text(if (isEditing) "Save My Profile" else "Enroll Partner")
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
fun CapitalInjectionDialog(
    partner: PartnerEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("Capital contribution by ${partner.name}") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Deposit Capital • ${partner.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Capital: ${Formatters.formatCurrency(partner.capitalContributed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DakshyamTeal,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = (it.toDoubleOrNull() ?: 0.0) <= 0.0
                    },
                    label = { Text("Additional Capital Amount (INR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error,
                    supportingText = if (error) { { Text("Enter a valid amount > 0") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_capital_amount"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Transaction Description / Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        error = true
                        return@Button
                    }
                    onConfirm(amount, note.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamTeal),
                modifier = Modifier.testTag("confirm_capital_deposit_btn")
            ) {
                Text("Confirm Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
