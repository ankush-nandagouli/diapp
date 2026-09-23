package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Close
import coil.compose.AsyncImage
import com.example.data.local.CashFlowEntity
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
fun CashFlowScreen(
    viewModel: DakshyamViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalInflow by viewModel.totalInflow.collectAsState()
    val totalCapital by viewModel.totalCapital.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val activePartner by viewModel.activePartner.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "EXPENSE", "CAPITAL_INJECTION", "CLIENT_INFLOW"
    var previewReceiptUri by remember { mutableStateOf<String?>(null) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        if (selectedFilter == "ALL") transactions
        else transactions.filter { it.type == selectedFilter }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Balance Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cashflow_balance_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = DakshyamNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Net Cash Balance (Treasury)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(netBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (netBalance >= 0) DakshyamCyan else DakshyamRose
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total Founder Capital Collected
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = DakshyamCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Founder Money Collected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Text(
                                    text = Formatters.formatCurrency(totalCapital),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DakshyamCyan
                                )
                            }

                            // Total Expenses
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = DakshyamRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Total Expenses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Text(
                                    text = Formatters.formatCurrency(totalExpenses),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        if (totalInflow > 0.0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Client & Project Inflows: ${Formatters.formatCurrency(totalInflow)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Gross Inflows: ${Formatters.formatCurrency(totalCapital + totalInflow)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DakshyamEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${transactions.size})") },
                        modifier = Modifier.testTag("filter_all_tx")
                    )
                    FilterChip(
                        selected = selectedFilter == "EXPENSE",
                        onClick = { selectedFilter = "EXPENSE" },
                        label = { Text("Expenses") },
                        modifier = Modifier.testTag("filter_expenses_tx")
                    )
                    FilterChip(
                        selected = selectedFilter == "CAPITAL_INJECTION",
                        onClick = { selectedFilter = "CAPITAL_INJECTION" },
                        label = { Text("Capital") }
                    )
                    FilterChip(
                        selected = selectedFilter == "CLIENT_INFLOW",
                        onClick = { selectedFilter = "CLIENT_INFLOW" },
                        label = { Text("Inflows") }
                    )
                }
            }

            // Transaction History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions & Vouchers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${filteredTransactions.size} Records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (filteredTransactions.isEmpty()) {
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
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions recorded yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Record' to add real cash flow, purchases, or capital deposits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Transactions List
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionCard(
                    transaction = tx,
                    onPreviewReceipt = { uri -> previewReceiptUri = uri },
                    onDelete = { viewModel.deleteTransaction(tx) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddTxDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_transaction"),
            containerColor = DakshyamTeal,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Entry", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Record Transaction Dialog
    if (showAddTxDialog) {
        RecordTransactionDialog(
            partners = partners.map { it.name },
            projects = projects.map { it.title },
            defaultPaidBy = activePartner?.name ?: (partners.firstOrNull()?.name ?: ""),
            onDismiss = { showAddTxDialog = false },
            onUploadBill = { uri, onComplete ->
                viewModel.uploadReceiptToCloudinary(uri, onComplete)
            },
            onConfirm = { type, amount, category, desc, paidBy, project, receipt ->
                val partnerObj = partners.find { it.name == paidBy }
                viewModel.addTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = desc,
                    paidByPartnerName = paidBy,
                    paidByPartnerId = partnerObj?.id ?: 0L,
                    projectName = project,
                    receiptUri = receipt
                )
                showAddTxDialog = false
            }
        )
    }

    // Receipt & Invoice Preview Dialog
    if (previewReceiptUri != null) {
        AlertDialog(
            onDismissRequest = { previewReceiptUri = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Transaction Voucher / Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { previewReceiptUri = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = previewReceiptUri,
                            contentDescription = "Receipt Full View",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { previewReceiptUri = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: CashFlowEntity,
    onPreviewReceipt: (String) -> Unit = {},
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    val isCapital = transaction.type == "CAPITAL_INJECTION"

    val badgeColor = when {
        isExpense -> DakshyamRose
        isCapital -> DakshyamNavy
        else -> DakshyamEmerald
    }

    val typeLabel = when {
        isExpense -> "Expense"
        isCapital -> "Capital Deposit"
        else -> "Client Inflow"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.description.ifEmpty { transaction.category },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (transaction.projectName.isNotEmpty()) {
                                Text(
                                    text = " • ${transaction.projectName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DakshyamTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isExpense) "-" else "+"}${Formatters.formatCurrency(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    )
                    Text(
                        text = Formatters.formatDateShort(transaction.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Paid By & Receipt Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Paid by: ${transaction.paidByPartnerName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.receiptUri.isNotEmpty()) {
                        Surface(
                            color = DakshyamTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onPreviewReceipt(transaction.receiptUri) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp), tint = DakshyamTeal)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (transaction.receiptUri.startsWith("http")) "View Bill ↗" else "View Receipt ↗",
                                    fontSize = 10.sp,
                                    color = DakshyamTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
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
fun RecordTransactionDialog(
    partners: List<String>,
    projects: List<String>,
    defaultPaidBy: String,
    onDismiss: () -> Unit,
    onUploadBill: (Uri, (Result<String>) -> Unit) -> Unit = { _, _ -> },
    onConfirm: (
        type: String,
        amount: Double,
        category: String,
        description: String,
        paidBy: String,
        project: String,
        receipt: String
    ) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") } // "EXPENSE", "CAPITAL_INJECTION", "CLIENT_INFLOW"
    var amountText by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Hardware & Fabrication") }
    var paidBy by remember { mutableStateOf(defaultPaidBy.ifEmpty { partners.firstOrNull() ?: "" }) }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull() ?: "") }
    var receiptUriOrNote by remember { mutableStateOf("") }
    var isUploadingReceipt by remember { mutableStateOf(false) }
    var uploadStatusNote by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher for bill/receipt picking (image or pdf)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingReceipt = true
            uploadStatusNote = null
            onUploadBill(uri) { result ->
                isUploadingReceipt = false
                result.onSuccess { secureUrl ->
                    receiptUriOrNote = secureUrl
                    uploadStatusNote = "Bill / receipt attached successfully!"
                    Toast.makeText(context, "Bill uploaded successfully", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    receiptUriOrNote = uri.toString()
                    uploadStatusNote = "Attached local file: ${e.localizedMessage ?: "Sync pending"}"
                    Toast.makeText(context, "Bill attached: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val categories = listOf(
        "Hardware & Fabrication",
        "Sensors & Electronic Boards",
        "Travel & Field Logistics",
        "Cloud Services & Tooling",
        "Workshop Rent & Utilities",
        "Stipends & Labor",
        "Capital Deposit",
        "Client Inflow",
        "General Expenses"
    )

    var categoryExpanded by remember { mutableStateOf(false) }
    var partnerExpanded by remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "EXPENSE") "Record Expense / Product Order" else "Record Transaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            if (selectedCategory == "Capital Deposit" || selectedCategory == "Client Inflow") {
                                selectedCategory = "Hardware & Fabrication"
                            }
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "CAPITAL_INJECTION",
                        onClick = {
                            type = "CAPITAL_INJECTION"
                            selectedCategory = "Capital Deposit"
                        },
                        label = { Text("Capital") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "CLIENT_INFLOW",
                        onClick = {
                            type = "CLIENT_INFLOW"
                            selectedCategory = "Client Inflow"
                        },
                        label = { Text("Inflow") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // If Expense: Explicit Product/Item Ordered field
                if (type == "EXPENSE") {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product / Item Ordered *") },
                        placeholder = { Text("e.g. Raspberry Pi 4, Lithium Battery, Steel Chassis") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name"),
                        singleLine = true
                    )
                }

                // Cost / Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = (it.toDoubleOrNull() ?: 0.0) <= 0.0
                    },
                    label = { Text(if (type == "EXPENSE") "Cost / Amount (INR) *" else "Amount (INR) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    supportingText = if (amountError) { { Text("Valid amount > 0 required") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tx_amount"),
                    singleLine = true
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Who Paid Dropdown
                ExposedDropdownMenuBox(
                    expanded = partnerExpanded,
                    onExpandedChange = { partnerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paidBy,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid By (Partner)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partnerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = partnerExpanded,
                        onDismissRequest = { partnerExpanded = false }
                    ) {
                        partners.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    paidBy = p
                                    partnerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Optional Project Link
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = projectExpanded,
                        onExpandedChange = { projectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProject.ifEmpty { "General / Company Overhead" },
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
                            DropdownMenuItem(
                                text = { Text("General / Company Overhead") },
                                onClick = {
                                    selectedProject = ""
                                    projectExpanded = false
                                }
                            )
                            projects.forEach { prj ->
                                DropdownMenuItem(
                                    text = { Text(prj) },
                                    onClick = {
                                        selectedProject = prj
                                        projectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (type == "EXPENSE") "Specifications / Vendor Notes (Optional)" else "Description / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Bill / Receipt Upload
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bill / Receipt Document",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isUploadingReceipt) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                pickMediaLauncher.launch("image/*")
                            },
                            enabled = !isUploadingReceipt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_bill_button")
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (receiptUriOrNote.isNotEmpty()) "Change Uploaded Bill" else "Upload Bill / Invoice",
                                fontSize = 13.sp
                            )
                        }

                        if (receiptUriOrNote.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = DakshyamEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (receiptUriOrNote.startsWith("http")) "Bill uploaded to Cloudinary" else "Bill attached",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DakshyamEmerald,
                                    maxLines = 1
                                )
                            }
                        }

                        // Bill Reference or URL
                        OutlinedTextField(
                            value = receiptUriOrNote,
                            onValueChange = { receiptUriOrNote = it },
                            label = { Text("Bill URL or Voucher Reference No.") },
                            leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        amountError = true
                        return@Button
                    }
                    val finalDesc = when {
                        type == "EXPENSE" && productName.isNotBlank() && description.isNotBlank() ->
                            "${productName.trim()} (${description.trim()})"
                        type == "EXPENSE" && productName.isNotBlank() ->
                            productName.trim()
                        description.isNotBlank() ->
                            description.trim()
                        else ->
                            selectedCategory
                    }

                    onConfirm(
                        type,
                        amount,
                        selectedCategory,
                        finalDesc,
                        paidBy,
                        selectedProject,
                        receiptUriOrNote.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = DakshyamNavy),
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
