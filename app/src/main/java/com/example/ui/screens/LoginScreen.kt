package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PartnerEntity
import com.example.data.repository.AuthResult
import com.example.ui.theme.DakshyamCyan
import com.example.ui.theme.DakshyamEmerald
import com.example.ui.theme.DakshyamNavy
import com.example.ui.theme.DakshyamNavyAccent
import com.example.ui.theme.DakshyamNavyBlue
import com.example.ui.theme.DakshyamSky
import com.example.ui.theme.DakshyamTeal
import com.example.ui.viewmodel.DakshyamViewModel

@Composable
fun LoginScreen(
    viewModel: DakshyamViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val partners by viewModel.partners.collectAsState()
    val companyProfile by viewModel.companyProfile.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Founding Partner Access, 1 = Login with Email
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Partner Password Verification Dialog State
    var selectedPartnerForLogin by remember { mutableStateOf<PartnerEntity?>(null) }
    var partnerPasswordInput by remember { mutableStateOf("") }
    var partnerPasswordVisible by remember { mutableStateOf(false) }
    var partnerPasswordError by remember { mutableStateOf<String?>(null) }

    // First Login Password Change Dialog State
    var partnerPendingPasswordChange by remember { mutableStateOf<PartnerEntity?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var changePasswordError by remember { mutableStateOf<String?>(null) }
    var isSavingNewPassword by remember { mutableStateOf(false) }

    val connectionStatus by viewModel.connectionStatus.collectAsState()
    var isCheckingConnection by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DakshyamNavy,
                        DakshyamNavyBlue,
                        Color(0xFF0F2042)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Brand Emblem / Logo
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(3.dp, Color.White),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("brand_emblem")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!companyProfile?.logoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = companyProfile?.logoUrl,
                                contentDescription = "Business Logo",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = "Dakshyam Logo",
                                tint = DakshyamNavy,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Company Name
                Text(
                    text = companyProfile?.companyName ?: "Dakshyam Innovations",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = companyProfile?.tagline ?: "Engineering Next-Gen Robotics & Intelligent Systems",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Live Supabase Connection Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (connectionStatus?.isConnected == true) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (connectionStatus?.isConnected == true) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .testTag("supabase_connection_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (connectionStatus?.isConnected == true) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (connectionStatus == null) "Supabase: Checking..."
                                   else if (connectionStatus?.isConnected == true) "Supabase: Connected (${connectionStatus?.latencyMs}ms)"
                                   else "Supabase: Offline",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (isCheckingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Text(
                                text = "Test",
                                color = DakshyamCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isCheckingConnection = true
                                        viewModel.verifySupabaseConnection {
                                            isCheckingConnection = false
                                        }
                                    }
                                    .testTag("btn_test_supabase_connection")
                            )
                        }
                    }
                }

                // Login Mode Tabs (Navy Blue & White styling)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; errorMessage = null },
                            text = {
                                Text(
                                    "Partner Login",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.65f)
                                )
                            },
                            modifier = Modifier.testTag("tab_partner_quick_access")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; errorMessage = null },
                            text = {
                                Text(
                                    "Login with Email",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.65f)
                                )
                            },
                            modifier = Modifier.testTag("tab_partner_auth")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (selectedTab == 0) {
                // Partner Selection Cards (Requires Password)
                item {
                    Text(
                        text = "SELECT YOUR FOUNDING PARTNER PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                val officialPartners = partners

                if (officialPartners.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .testTag("login_loading_partners_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Connecting to Supabase Database...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Loading partner credentials or switch to Email Login.",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(officialPartners.size) { index ->
                        val partner = officialPartners[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                // Prompt for password instead of 1-tap direct login
                                selectedPartnerForLogin = partner
                                partnerPasswordInput = ""
                                partnerPasswordError = null
                            }
                            .testTag("login_partner_${partner.name.lowercase().replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = DakshyamNavy.copy(alpha = 0.1f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (partner.avatarUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = partner.avatarUrl,
                                            contentDescription = partner.name,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = partner.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = DakshyamNavy,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = partner.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DakshyamNavy
                                )
                                Text(
                                    text = "${partner.role} • ${partner.email}",
                                    fontSize = 12.sp,
                                    color = DakshyamNavy.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Protected",
                                tint = DakshyamNavyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                }
            } else {
                // Login with Email Form (Navy Blue and White theme)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Login with Email",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DakshyamNavy
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Partner Email") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = DakshyamNavyBlue)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_email"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DakshyamNavyBlue,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = DakshyamNavyBlue,
                                    unfocusedLabelColor = DakshyamNavy.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavyBlue)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = DakshyamNavy
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_password"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = DakshyamNavy,
                                    unfocusedTextColor = DakshyamNavy,
                                    focusedBorderColor = DakshyamNavyBlue,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = DakshyamNavyBlue,
                                    unfocusedLabelColor = DakshyamNavy.copy(alpha = 0.6f)
                                )
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.testTag("login_error_message")
                                )
                            }

                            Button(
                                onClick = {
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter your partner email and password"
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.signInWithEmail(email.trim(), password) { result ->
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                val target = result.data
                                                if (target.mustChangePassword || password.trim() == "427752") {
                                                    partnerPendingPasswordChange = target
                                                } else {
                                                    viewModel.setLoggedIn(true)
                                                    Toast.makeText(context, "Logged in as ${result.data.name}", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                }
                                            }
                                            is AuthResult.Error -> {
                                                // Graceful fallback login for authorized partner emails if credentials match
                                                val match = partners.find { it.email.equals(email.trim(), ignoreCase = true) }
                                                if (match != null) {
                                                    val isCustomPasswordSet = match.password.isNotEmpty() && match.password != "427752"
                                                    val isPasswordCorrect = if (isCustomPasswordSet) {
                                                        // Once the password is changed, default password 427752 MUST NEVER work
                                                        password == match.password
                                                    } else {
                                                        password == match.password.ifEmpty { "427752" }
                                                    }

                                                    if (isPasswordCorrect) {
                                                        if (match.mustChangePassword || password.trim() == "427752") {
                                                            partnerPendingPasswordChange = match
                                                        } else {
                                                            viewModel.loginAsPartner(match)
                                                            onLoginSuccess()
                                                        }
                                                    } else {
                                                        errorMessage = if (isCustomPasswordSet) {
                                                            "Invalid credentials. Your password was previously changed; default password '427752' is permanently disabled."
                                                        } else {
                                                            result.message
                                                        }
                                                    }
                                                } else {
                                                    errorMessage = result.message
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_submit_login"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DakshyamNavy,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Login with Email", fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = { showResetDialog = true },
                                    modifier = Modifier.testTag("btn_forgot_password")
                                ) {
                                    Text(
                                        "Forgot Password?",
                                        color = DakshyamNavyBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted Supabase PostgreSQL Enterprise Environment",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // 1. PARTNER PASSWORD VERIFICATION DIALOG
    selectedPartnerForLogin?.let { partner ->
        AlertDialog(
            onDismissRequest = {
                selectedPartnerForLogin = null
                partnerPasswordInput = ""
                partnerPasswordError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = DakshyamNavy.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = partner.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = DakshyamNavy,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = partner.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DakshyamNavy
                        )
                        Text(
                            text = partner.role,
                            fontSize = 12.sp,
                            color = DakshyamNavy.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enter partner security password to access your dashboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DakshyamNavy
                    )

                    val isCustomPasswordSet = partner.password.isNotEmpty() && partner.password != "427752"

                    OutlinedTextField(
                        value = partnerPasswordInput,
                        onValueChange = {
                            partnerPasswordInput = it
                            partnerPasswordError = null
                        },
                        label = { Text("Password") },
                        placeholder = {
                            Text(if (isCustomPasswordSet) "Enter personal password" else "Enter temporary password (427752)")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { partnerPasswordVisible = !partnerPasswordVisible }) {
                                Icon(
                                    imageVector = if (partnerPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = DakshyamNavy
                                )
                            }
                        },
                        visualTransformation = if (partnerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DakshyamNavy,
                            unfocusedTextColor = DakshyamNavy,
                            focusedBorderColor = DakshyamNavy,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    if (partnerPasswordError != null) {
                        Text(
                            text = partnerPasswordError ?: "",
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
                        val entered = partnerPasswordInput.trim()
                        if (entered.isBlank()) {
                            partnerPasswordError = "Please enter your password"
                            return@Button
                        }

                        viewModel.authenticatePartnerWithSecurity(
                            partner = partner,
                            enteredPassword = entered,
                            onSuccess = { targetPartner ->
                                selectedPartnerForLogin = null
                                partnerPasswordInput = ""
                                partnerPasswordError = null

                                // Check if first-login password change is required
                                if (targetPartner.mustChangePassword || entered == "427752") {
                                    partnerPendingPasswordChange = targetPartner
                                } else {
                                    Toast.makeText(context, "Welcome back, ${targetPartner.name}", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            },
                            onError = { errMsg ->
                                partnerPasswordError = errMsg
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("btn_confirm_partner_password")
                ) {
                    Text("Unlock & Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedPartnerForLogin = null
                        partnerPasswordInput = ""
                        partnerPasswordError = null
                    }
                ) {
                    Text("Cancel", color = DakshyamNavy)
                }
            },
            containerColor = Color.White
        )
    }

    // 2. FIRST-LOGIN CHANGE PASSWORD DIALOG
    partnerPendingPasswordChange?.let { partner ->
        AlertDialog(
            onDismissRequest = {
                // Must change password before proceeding
            },
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
                        text = "Change Default Password",
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
                                tint = DakshyamNavyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome ${partner.name}! This is your first login. You must set a secure password (min 6 chars, letters & numbers) to protect your account.",
                                fontSize = 12.sp,
                                color = DakshyamNavy
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            changePasswordError = null
                        },
                        label = { Text("New Security Password") },
                        placeholder = { Text("Min 6 chars (letters & numbers)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DakshyamNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = DakshyamNavy
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_password"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DakshyamNavy,
                            unfocusedTextColor = DakshyamNavy,
                            focusedBorderColor = DakshyamNavy,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            changePasswordError = null
                        },
                        label = { Text("Confirm New Password") },
                        placeholder = { Text("Re-enter new password") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DakshyamNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = DakshyamNavy
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_confirm_password"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DakshyamNavy,
                            unfocusedTextColor = DakshyamNavy,
                            focusedBorderColor = DakshyamNavy,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    if (changePasswordError != null) {
                        Text(
                            text = changePasswordError ?: "",
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
                        val newPass = newPasswordInput.trim()
                        val confirmPass = confirmPasswordInput.trim()
                        if (newPass.length < 6) {
                            changePasswordError = "Password must be at least 6 characters long"
                            return@Button
                        }
                        if (newPass == "427752") {
                            changePasswordError = "Please choose a different password than default '427752'"
                            return@Button
                        }
                        if (newPass != confirmPass) {
                            changePasswordError = "Passwords do not match"
                            return@Button
                        }

                        isSavingNewPassword = true
                        val targetPartner = partner
                        viewModel.updatePartnerPassword(targetPartner.id, newPass) { success, errMsg ->
                            isSavingNewPassword = false
                            if (success) {
                                val updatedPartner = targetPartner.copy(
                                    password = newPass,
                                    mustChangePassword = false
                                )
                                partnerPendingPasswordChange = null
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                changePasswordError = null
                                viewModel.loginAsPartner(updatedPartner)
                                Toast.makeText(context, "Password updated successfully! Welcome, ${updatedPartner.name}", Toast.LENGTH_LONG).show()
                                onLoginSuccess()
                            } else {
                                changePasswordError = errMsg ?: "Unable to save new password. Please try again."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    enabled = !isSavingNewPassword,
                    modifier = Modifier.testTag("btn_save_new_password")
                ) {
                    if (isSavingNewPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save & Launch App")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        partnerPendingPasswordChange = null
                        newPasswordInput = ""
                        confirmPasswordInput = ""
                        changePasswordError = null
                    }
                ) {
                    Text("Cancel", color = DakshyamNavy)
                }
            },
            containerColor = Color.White
        )
    }

    // 3. FORGOT PASSWORD DIALOG
    if (showResetDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetMsg by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = DakshyamNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Partner Password", fontWeight = FontWeight.Bold, color = DakshyamNavy)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your official partner email to receive a password reset link:",
                        color = DakshyamNavy,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Partner Email") },
                        placeholder = { Text("e.g. partner@dakshyam.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reset_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DakshyamNavy,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    if (resetMsg != null) {
                        Text(
                            resetMsg!!,
                            color = if (resetMsg!!.contains("sent", ignoreCase = true) || resetMsg!!.contains("default", ignoreCase = true)) DakshyamEmerald else MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedEmail = resetEmail.trim()
                        if (trimmedEmail.isNotBlank()) {
                            isResetting = true
                            viewModel.sendPasswordReset(trimmedEmail) { res ->
                                isResetting = false
                                when (res) {
                                    is AuthResult.Success -> {
                                        resetMsg = "Password reset email instructions sent to $trimmedEmail"
                                        Toast.makeText(context, "Reset link dispatched", Toast.LENGTH_SHORT).show()
                                    }
                                    is AuthResult.Error -> {
                                        // Check if it's one of our registered partner emails
                                        val match = partners.find { it.email.equals(trimmedEmail, ignoreCase = true) }
                                        if (match != null) {
                                            // Reset their password back to default 427752
                                            viewModel.updatePartnerPassword(match.id, "427752") {
                                                resetMsg = "Password reset to default (427752) for ${match.name}. Please log in and change your password."
                                                Toast.makeText(context, "Password reset to default", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            resetMsg = res.message
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        } else {
                            resetMsg = "Please enter your partner email address"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    enabled = !isResetting,
                    modifier = Modifier.testTag("btn_send_reset_link")
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = DakshyamNavy)
                }
            },
            containerColor = Color.White
        )
    }
}
