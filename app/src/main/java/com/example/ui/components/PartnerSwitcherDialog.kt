package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PartnerEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.PartnerAccessState
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamTeal
import com.example.ui.viewmodel.DakshyamViewModel

@Composable
fun PartnerSwitcherDialog(
    viewModel: DakshyamViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val partners by viewModel.partners.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()
    val partnerAccessState by viewModel.partnerAccessState.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    val isAuthenticated = (partnerAccessState is PartnerAccessState.Authorized) || isUserLoggedIn

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active Identity, 1: Partner Sign-In, 2: Enroll Partner

    // Form inputs for Auth
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isEnrollPasswordVisible by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("Executive Partner") }
    var phoneInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var authFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = DakshyamNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Partner Security & Access", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Access Status Banner
                when (val state = partnerAccessState) {
                    is PartnerAccessState.Authorized -> {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Secure Session: ${state.partner.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "${state.partner.role} • ${state.user.email.ifBlank { "Verified" }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                    is PartnerAccessState.AccessDenied -> {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Access Blocked: ${state.reason}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    else -> {}
                }

                // Sub-tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = DakshyamNavy
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active Profile", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_active_profile")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sign In", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_auth_signin")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Enroll", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_auth_enroll")
                    )
                }

                when (selectedTab) {
                    // TAB 0: SWITCH ACTIVE PROFILE
                    0 -> {
                        if (isAuthenticated) {
                            Surface(
                                color = DakshyamNavy.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Security Active",
                                        tint = DakshyamNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Session Locked: You are authenticated as ${activePartner?.name}. Direct switching is disabled to prevent unauthorized task assignments.",
                                        fontSize = 11.sp,
                                        color = DakshyamNavy,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Select an active partner identity to sign project updates and financial vouchers:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(partners, key = { it.id }) { partner ->
                                val isSelected = activePartner?.id == partner.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isAuthenticated && !isSelected) {
                                                Toast.makeText(
                                                    context,
                                                    "Direct profile switching restricted. Please authenticate as ${partner.name}.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                emailInput = partner.email
                                                authFeedbackMessage = "Please authenticate as ${partner.name} to switch identity."
                                                isFeedbackError = false
                                                selectedTab = 1
                                            } else if (!isAuthenticated) {
                                                viewModel.switchActivePartner(partner)
                                                onDismiss()
                                            }
                                        }
                                        .testTag("select_partner_${partner.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            DakshyamNavy.copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) DakshyamNavy else DakshyamTeal.copy(alpha = 0.2f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = partner.name.take(2).uppercase(),
                                                    color = if (isSelected) Color.White else DakshyamNavy,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = partner.name,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = DakshyamTeal.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "Active",
                                                                color = DakshyamNavy,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${partner.role} • ${partner.email.ifEmpty { "No email linked" }}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        if (isAuthenticated && !isSelected) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = "Switch Locked",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Sign-in required", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        } else {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    if (isAuthenticated && !isSelected) {
                                                        emailInput = partner.email
                                                        selectedTab = 1
                                                    } else if (!isAuthenticated) {
                                                        viewModel.switchActivePartner(partner)
                                                        onDismiss()
                                                    }
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = DakshyamNavy)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (partnerAccessState is PartnerAccessState.Authorized) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.signOutPartner()
                                    Toast.makeText(context, "Signed out securely", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_sign_out_partner"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Secure Session (Sign Out)")
                            }
                        }
                    }

                    // TAB 1: SIGN IN TO PARTNER PORTAL & GOOGLE
                    1 -> {
                        Text(
                            text = "Authenticate with your official partner credentials or Google account:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Partner Email") },
                            placeholder = { Text("partner@dakshyam.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = DakshyamNavy
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // Action Feedback
                        authFeedbackMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (isFeedbackError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    authFeedbackMessage = "Please enter both email and password."
                                    isFeedbackError = true
                                    return@Button
                                }
                                isLoading = true
                                authFeedbackMessage = null
                                viewModel.signInWithEmail(emailInput, passwordInput) { res ->
                                    isLoading = false
                                    when (res) {
                                        is AuthResult.Success -> {
                                            authFeedbackMessage = "Authenticated as ${res.data.name}."
                                            isFeedbackError = false
                                            Toast.makeText(context, "Welcome ${res.data.name}!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                        is AuthResult.Error -> {
                                            authFeedbackMessage = res.message
                                            isFeedbackError = true
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auth_submit_login"),
                            colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Sign In to Partner Portal")
                        }

                        // Google Sign-In button
                        OutlinedButton(
                            onClick = {
                                authFeedbackMessage = "Initiating Google Sign-In with Credential Manager..."
                                isFeedbackError = false
                                viewModel.signInWithGoogle(context, "dakshyam-innovations-web-client-id") { res ->
                                    when (res) {
                                        is AuthResult.Success -> {
                                            authFeedbackMessage = "Authenticated with Google as ${res.data.name}."
                                            isFeedbackError = false
                                            Toast.makeText(context, "Signed in via Google: ${res.data.name}", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                        is AuthResult.Error -> {
                                            authFeedbackMessage = res.message
                                            isFeedbackError = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auth_google_signin")
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp), tint = DakshyamCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign In with Google", color = DakshyamNavy)
                        }

                        TextButton(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    authFeedbackMessage = "Enter your email to request a reset link."
                                    isFeedbackError = true
                                } else {
                                    viewModel.sendPasswordReset(emailInput) { res ->
                                        when (res) {
                                            is AuthResult.Success -> {
                                                authFeedbackMessage = "Password reset instructions dispatched."
                                                isFeedbackError = false
                                            }
                                            is AuthResult.Error -> {
                                                authFeedbackMessage = res.message
                                                isFeedbackError = true
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Forgot Password?", fontSize = 12.sp, color = DakshyamNavy)
                        }
                    }

                    // TAB 2: ENROLL NEW PARTNER
                    2 -> {
                        Text(
                            text = "Register a new partner in the secure Dakshyam access directory:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("enroll_name_input")
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Partner Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("enroll_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Temporary Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isEnrollPasswordVisible = !isEnrollPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isEnrollPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isEnrollPasswordVisible) "Hide password" else "Show password",
                                        tint = DakshyamNavy
                                    )
                                }
                            },
                            visualTransformation = if (isEnrollPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("enroll_password_input")
                        )

                        OutlinedTextField(
                            value = roleInput,
                            onValueChange = { roleInput = it },
                            label = { Text("Role (e.g. Executive Partner)") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("enroll_role_input")
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("enroll_phone_input")
                        )

                        authFeedbackMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (isFeedbackError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                if (nameInput.isBlank() || emailInput.isBlank() || passwordInput.length < 6) {
                                    authFeedbackMessage = "Please complete all fields (password minimum 6 characters)."
                                    isFeedbackError = true
                                    return@Button
                                }
                                isLoading = true
                                authFeedbackMessage = null
                                viewModel.signUpPartner(
                                    email = emailInput,
                                    pass = passwordInput,
                                    name = nameInput,
                                    role = roleInput,
                                    phone = phoneInput
                                ) { res ->
                                    isLoading = false
                                    when (res) {
                                        is AuthResult.Success -> {
                                            authFeedbackMessage = "Successfully enrolled ${res.data.name}."
                                            isFeedbackError = false
                                            Toast.makeText(context, "Enrolled: ${res.data.name}", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                        is AuthResult.Error -> {
                                            authFeedbackMessage = res.message
                                            isFeedbackError = true
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().testTag("btn_enroll_partner_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Enroll Partner Credentials")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy)
            ) {
                Text("Close")
            }
        }
    )
}

