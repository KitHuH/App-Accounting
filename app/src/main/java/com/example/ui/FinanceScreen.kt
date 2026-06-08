package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.togetherWith
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val budget by viewModel.currentBudget.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentMonthYearString by viewModel.currentMonthYearString.collectAsStateWithLifecycle()
    val viewDate by viewModel.viewDate.collectAsStateWithLifecycle()

    val compactMode by viewModel.compactMode.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showSystemDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.Expense?>(null) }
    var expenseToEdit by remember { mutableStateOf<com.example.data.Expense?>(null) }
    var sortDescending by remember { mutableStateOf(true) }
    
    val sortedExpenses = remember(expenses, sortDescending) {
        if (sortDescending) expenses.sortedByDescending { it.timestamp } else expenses.sortedBy { it.timestamp }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val createDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = java.io.OutputStreamWriter(outputStream)
                    writer.append("Tanggal,Deskripsi,Kategori,Jumlah\n")
                    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    expenses.forEach { expense ->
                        val dateStr = dateFormat.format(java.util.Date(expense.timestamp))
                        val descStr = "\"${expense.description.replace("\"", "\"\"")}\""
                        val categoryStr = "\"${expense.category}\""
                        val amountStr = expense.amount.toString()
                        writer.append("$dateStr,$descStr,$categoryStr,$amountStr\n")
                    }
                    writer.flush()
                }
                android.widget.Toast.makeText(context, "Data berhasil diekspor!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Gagal mengekspor data.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dompetku", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showSystemDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                        }
                    },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFabMenu,
                    enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(expandFrom = Alignment.Bottom),
                    exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showSetBudgetDialog = true
                                showFabMenu = false
                            },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Pemasukan Dana") },
                            text = { Text("Pemasukan Dana") },
                            modifier = Modifier.padding(bottom = 8.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                showAddExpenseDialog = true
                                showFabMenu = false
                            },
                            icon = { Icon(Icons.Default.Remove, contentDescription = "Tambah Pengeluaran") },
                            text = { Text("Pengeluaran") },
                            modifier = Modifier.padding(bottom = 16.dp),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                androidx.compose.animation.AnimatedContent(
                    targetState = showFabMenu,
                    label = "FabIconTransition",
                    transitionSpec = {
                        androidx.compose.animation.scaleIn() togetherWith androidx.compose.animation.scaleOut()
                    }
                ) { isMenuOpen ->
                    FloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isMenuOpen) Icons.Default.Close else Icons.Default.Add, 
                            contentDescription = if (isMenuOpen) "Tutup" else "Tambah"
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            FinanceScreenDecorations(themeMode)
            
            androidx.compose.animation.AnimatedContent(
                targetState = currentMonthYearString,
                label = "MonthTransition",
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                },
                modifier = Modifier.fillMaxSize().padding(padding)
            ) { monthYearStr ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 88.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousMonth() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                            }
                            androidx.compose.material3.TextButton(onClick = { showMonthPicker = true }) {
                                Text(
                                    text = monthYearStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Berikutnya")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        DashboardCard(totalSpent = totalSpent, budgetAmount = budget?.totalBudget, themeMode = themeMode)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
            
                    if (expenses.isNotEmpty()) {
                        item {
                            Text(
                                text = "Ringkasan Kategori",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        item {
                            val categories = expenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.amount } }.toList()
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(items = categories, key = { it.first }) { entry ->
                                    CategorySummaryCard(category = entry.first, amount = entry.second)
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            MonthlyTrendChart(expenses)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Pengeluaran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            var showSortMenu by remember { mutableStateOf(false) }
                            Box {
                                androidx.compose.material3.TextButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Urutkan",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (sortDescending) "Terbaru" else "Terlama", style = MaterialTheme.typography.bodySmall)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Terbaru ke Terlama") },
                                        onClick = { 
                                            sortDescending = true
                                            showSortMenu = false 
                                        },
                                        trailingIcon = { if (sortDescending) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Terlama ke Terbaru") },
                                        onClick = { 
                                            sortDescending = false
                                            showSortMenu = false 
                                        },
                                        trailingIcon = { if (!sortDescending) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
            
                    if (sortedExpenses.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("Belum ada pengeluaran", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(
                            items = sortedExpenses,
                            key = { it.id }
                        ) { expense ->
                            ExpenseItem(
                                expense = expense,
                                compactMode = compactMode,
                                modifier = Modifier.animateItem(),
                                onEdit = { expenseToEdit = it },
                                onDelete = { expenseToDelete = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp).animateItem())
                        }
                    } // end else
                } // end LazyColumn
            } // end Crossfade
        } // end Box
    } // end Scaffold body

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            initialExpense = null,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { amount, desc, category, timestamp ->
                viewModel.addExpense(amount, desc, category, timestamp)
                showAddExpenseDialog = false
            }
        )
    }

    if (expenseToEdit != null) {
        AddExpenseDialog(
            initialExpense = expenseToEdit,
            onDismiss = { expenseToEdit = null },
            onSave = { amount, desc, category, timestamp ->
                val updatedExpense = expenseToEdit!!.copy(
                    amount = amount,
                    description = desc,
                    category = category,
                    timestamp = timestamp
                )
                viewModel.updateExpense(updatedExpense)
                expenseToEdit = null
            }
        )
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentDate = viewDate,
            onDismiss = { showMonthPicker = false },
            onSave = { month, year ->
                viewModel.setMonthYear(month, year)
                showMonthPicker = false
            }
        )
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = showSystemDialog,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        var autoUpdateEnabled by remember { mutableStateOf(true) }
        var isCheckingUpdate by remember { mutableStateOf(false) }
        var biometricEnabled by remember { mutableStateOf(prefs.getBoolean("biometric_enabled", false)) }
        var secretClickCount by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            if (autoUpdateEnabled) {
                isCheckingUpdate = true
                kotlinx.coroutines.delay(1500)
                isCheckingUpdate = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = { showSystemDialog = false }
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showSystemDialog,
                enter = androidx.compose.animation.scaleIn(
                    initialScale = 0.8f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 400f)
                ),
                exit = androidx.compose.animation.scaleOut(targetScale = 0.9f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {} // intercept clicks
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Pengaturan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedButton(
                            onClick = {
                                viewModel.resetData()
                                showSystemDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Lembaran Baru")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { 
                                createDocumentLauncher.launch("Pengeluaran_${currentMonthYearString.replace(" ", "_")}.csv")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ekspor Riwayat ke CSV")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kunci Aplikasi (Biometrik)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (biometricEnabled) "Aplikasi akan terkunci saat dibuka" else "Aplikasi tidak terkunci",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { 
                                    biometricEnabled = it
                                    prefs.edit().putBoolean("biometric_enabled", it).apply()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Update Otomatis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isCheckingUpdate) {
                                    Text(
                                        text = "Memeriksa pembaruan...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = if (autoUpdateEnabled) "Aplikasi sudah dalam versi terbaru" else "Update otomatis dimatikan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            androidx.compose.material3.Switch(
                                checked = autoUpdateEnabled,
                                onCheckedChange = { 
                                    autoUpdateEnabled = it 
                                    if (it) {
                                        isCheckingUpdate = true
                                    }
                                }
                            )
                        }

                        LaunchedEffect(autoUpdateEnabled, isCheckingUpdate) {
                            if (isCheckingUpdate) {
                                kotlinx.coroutines.delay(1500)
                                isCheckingUpdate = false
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Tema Aplikasi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var expandedTheme by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedTheme = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (themeMode) {
                                        com.example.ui.theme.AppThemeType.AWAN_WAYANG -> "Awan Wayang (Putih)"
                                        com.example.ui.theme.AppThemeType.AWAN_WAYANG_DARK -> "Awan Wayang (Gelap)"
                                        com.example.ui.theme.AppThemeType.DARK_FUTURISTIC -> "Neon Cyberpunk (Gelap)"
                                        com.example.ui.theme.AppThemeType.NATURE_FOREST -> "Nature Forest (Hijau)"
                                        com.example.ui.theme.AppThemeType.OCEAN_BLUE -> "Ocean Blue (Biru)"
                                    }
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedTheme,
                                onDismissRequest = { expandedTheme = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                com.example.ui.theme.AppThemeType.values().forEach { theme ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                when(theme) {
                                                    com.example.ui.theme.AppThemeType.AWAN_WAYANG -> "Awan Wayang (Putih)"
                                                    com.example.ui.theme.AppThemeType.AWAN_WAYANG_DARK -> "Awan Wayang (Gelap)"
                                                    com.example.ui.theme.AppThemeType.DARK_FUTURISTIC -> "Neon Cyberpunk (Gelap)"
                                                    com.example.ui.theme.AppThemeType.NATURE_FOREST -> "Nature Forest (Hijau)"
                                                    com.example.ui.theme.AppThemeType.OCEAN_BLUE -> "Ocean Blue (Biru)"
                                                }
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.setTheme(theme)
                                            expandedTheme = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.setCompactMode(!compactMode) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mode Ringkas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Lebih banyak data terlihat dalam layar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            androidx.compose.material3.Switch(
                                checked = compactMode,
                                onCheckedChange = { viewModel.setCompactMode(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "Tentang Aplikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Dompetku adalah aplikasi keuangan pribadi pintar yang dirancang untuk membantu Anda mencatat setiap pengeluaran harian dan mengatur anggaran bulanan dengan mudah.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Versi 2.0", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Build & Disain By Gemini dan Gewa", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold, 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                secretClickCount++
                                if (secretClickCount == 7) {
                                    secretClickCount = 0
                                    try {
                                        android.media.MediaPlayer.create(context, com.example.R.raw.secret_sound)?.start()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showSystemDialog = false }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text("Tutup", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            currentBudget = budget?.totalBudget,
            onDismiss = { showSetBudgetDialog = false },
            onSet = { amount ->
                viewModel.setBudget(amount)
                showSetBudgetDialog = false
            }
        )
    }
    
    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Hapus Pengeluaran", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus catatan pengeluaran ini?") },
            confirmButton = {
                TextButton(onClick = {
                    expenseToDelete?.let { viewModel.deleteExpense(it) }
                    expenseToDelete = null
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
    } // end root Box
}

@Composable
fun DashboardCard(totalSpent: Double, budgetAmount: Double?, themeMode: com.example.ui.theme.AppThemeType) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    val remaining = if (budgetAmount != null) budgetAmount - totalSpent else 0.0

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
        ) {
            DashboardCardDecoration(themeMode)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Total Pengeluaran Bulan Ini",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRp.format(totalSpent),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (budgetAmount != null) formatRp.format(budgetAmount) else "Belum diatur",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    if (budgetAmount != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Dana",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            val remainingColor = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            Text(
                                text = formatRp.format(remaining),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = remainingColor
                            )
                        }
                    }
                }
                
                if (budgetAmount != null && budgetAmount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val progress = (totalSpent / budgetAmount).coerceIn(0.0, 1.0).toFloat()
                    val progressColor = when {
                        progress > 0.9f -> MaterialTheme.colorScheme.error
                        progress > 0.7f -> Color(0xFFE65100) // Orange
                        else -> MaterialTheme.colorScheme.primary
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: com.example.data.Expense, compactMode: Boolean = false, modifier: Modifier = Modifier, onEdit: (com.example.data.Expense) -> Unit, onDelete: (com.example.data.Expense) -> Unit) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.Builder().setLanguage("id").setRegion("ID").build())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactMode) 8.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compactMode) 32.dp else 48.dp)
                    .clip(RoundedCornerShape(if (compactMode) 8.dp else 12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Determine icon based on category
                val icon = when (expense.category) {
                    "Makanan" -> Icons.Default.Restaurant
                    "Transportasi" -> Icons.Default.DirectionsCar
                    "Belanja" -> Icons.Default.ShoppingCart
                    "Tagihan" -> Icons.Default.Receipt
                    else -> Icons.Default.AttachMoney
                }
                Icon(
                    imageVector = icon,
                    contentDescription = expense.category,
                    modifier = Modifier.size(if (compactMode) 16.dp else 24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(if (compactMode) 8.dp else 16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = if (compactMode) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = sdf.format(Date(expense.timestamp)),
                    style = if (compactMode) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRp.format(expense.amount),
                    style = if (compactMode) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error // Expenses usually shown in red/error color
                )
                Row {
                    IconButton(onClick = { onEdit(expense) }, modifier = Modifier.size(if (compactMode) 20.dp else 24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(if (compactMode) 14.dp else 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(if (compactMode) 4.dp else 8.dp))
                    IconButton(onClick = { onDelete(expense) }, modifier = Modifier.size(if (compactMode) 20.dp else 24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            modifier = Modifier.size(if (compactMode) 14.dp else 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    initialExpense: com.example.data.Expense? = null,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, category: String, timestamp: Long) -> Unit
) {
    var amountStr by remember { mutableStateOf(if (initialExpense != null) initialExpense.amount.toLong().toString() else "") }
    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: "Makanan") }
    
    var selectedDateMillis by remember { mutableStateOf(initialExpense?.timestamp ?: System.currentTimeMillis()) }
    val displayDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(selectedDateMillis))
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        ReusableDatePickerModal(
            initialDateMillis = selectedDateMillis,
            onDateSelected = { timestamp ->
                if (timestamp != null) {
                    selectedDateMillis = timestamp
                }
                showDatePicker = false
            },
            onDismissRequest = { showDatePicker = false }
        )
    }

    val categories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Lainnya")

    val displayAmount = try {
        if (amountStr.isNotEmpty()) {
            val parsed = amountStr.replace(Regex("[^\\d]"), "").toLong()
            NumberFormat.getNumberInstance(Locale.Builder().setLanguage("id").setRegion("ID").build()).format(parsed)
        } else ""
    } catch (e: Exception) { amountStr }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (initialExpense == null) "Tambah Pengeluaran" else "Edit Pengeluaran", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) 
        },
        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = displayAmount,
                    onValueChange = { 
                        val clean = it.replace(Regex("[^\\d]"), "")
                        if (clean.length <= 15) amountStr = clean 
                    },
                    label = { Text("Jumlah (Rp)") },
                    leadingIcon = { Text("Rp", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        label = { Text("Tanggal") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Kategori", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = categories, key = { it }) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0 && description.isNotBlank()) {
                        onSave(amount, description, category, selectedDateMillis)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SetBudgetDialog(
    currentBudget: Double?,
    onDismiss: () -> Unit,
    onSet: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf(currentBudget?.toLong()?.toString() ?: "") }

    val displayAmount = try {
        if (amountStr.isNotEmpty()) {
            val parsed = amountStr.replace(Regex("[^\\d]"), "").toLong()
            NumberFormat.getNumberInstance(Locale.Builder().setLanguage("id").setRegion("ID").build()).format(parsed)
        } else ""
    } catch (e: Exception) { amountStr }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Pemasukan Dana", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) 
        },
        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Berapa pemasukan dana Anda bulan ini?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = displayAmount,
                    onValueChange = { 
                        val clean = it.replace(Regex("[^\\d]"), "")
                        if (clean.length <= 15) amountStr = clean 
                    },
                    label = { Text("Jumlah (Rp)") },
                    leadingIcon = { Text("Rp", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount >= 0) {
                        onSet(amount)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CategorySummaryCard(category: String, amount: Double) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.widthIn(min = 120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val icon = when (category) {
                "Makanan" -> Icons.Default.Restaurant
                "Transportasi" -> Icons.Default.DirectionsCar
                "Belanja" -> Icons.Default.ShoppingCart
                "Tagihan" -> Icons.Default.Receipt
                else -> Icons.Default.AttachMoney
            }
            Icon(
                imageVector = icon,
                contentDescription = category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRp.format(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReusableDatePickerModal(
    initialDateMillis: Long,
    onDateSelected: (Long?) -> Unit,
    onDismissRequest: () -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Batal")
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}

@Composable
fun MonthlyTrendChart(expenses: List<Expense>) {
    if (expenses.isEmpty()) return
    var viewDays by remember { mutableStateOf(30) }
    
    val calendar = Calendar.getInstance()
    val dailySums = FloatArray(31) { 0f }
    for (expense in expenses) {
         calendar.timeInMillis = expense.timestamp
         val day = calendar.get(Calendar.DAY_OF_MONTH) - 1
         if(day in 0..30) {
            dailySums[day] += expense.amount.toFloat()
         }
    }
    
    val entries = dailySums.take(viewDays).mapIndexed { index, amount ->
        FloatEntry(x = (index + 1).toFloat(), y = amount)
    }
    
    val chartEntryModel = entryModelOf(entries)
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tren Pengeluaran Harian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(5, 10, 15, 20, 25, 30)
                items(options.size) { index ->
                    val days = options[index]
                    androidx.compose.material3.FilterChip(
                        selected = viewDays == days,
                        onClick = { viewDays = days },
                        label = { Text("$days Hari") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(
                    isScrollEnabled = true
                ),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}

@Composable
fun FinanceScreenDecorations(themeMode: com.example.ui.theme.AppThemeType) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        when (themeMode) {
            com.example.ui.theme.AppThemeType.AWAN_WAYANG, com.example.ui.theme.AppThemeType.AWAN_WAYANG_DARK -> {
                val color = if (themeMode == com.example.ui.theme.AppThemeType.AWAN_WAYANG_DARK) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f)
                drawCircle(color, radius = width * 0.8f, center = androidx.compose.ui.geometry.Offset(width, 0f))
                drawCircle(color, radius = width * 0.6f, center = androidx.compose.ui.geometry.Offset(0f, height * 0.8f))
                
                // Wayang/Cloud arcs
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(-width * 0.2f, height * 0.1f)
                path.quadraticBezierTo(width * 0.2f, height * 0.05f, width * 0.5f, height * 0.15f)
                path.quadraticBezierTo(width * 0.8f, height * 0.25f, width * 1.2f, height * 0.1f)
                drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f))
            }
            com.example.ui.theme.AppThemeType.DARK_FUTURISTIC -> {
                val gridColor = Color(0xFF00FFCC).copy(alpha = 0.05f)
                val gridSize = 120f
                var x = 0f
                while (x < width) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, height), 2f)
                    x += gridSize
                }
                var y = 0f
                while (y < height) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(width, y), 2f)
                    y += gridSize
                }
                drawCircle(Color(0xFFFF00FF).copy(alpha = 0.1f), radius = 200f, center = androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.2f))
            }
            com.example.ui.theme.AppThemeType.NATURE_FOREST -> {
                val leafColor = Color(0xFF2E7D32).copy(alpha = 0.05f)
                drawCircle(leafColor, radius = width * 0.4f, center = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.1f))
                drawCircle(leafColor, radius = width * 0.3f, center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.8f))
                // Vine
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height * 0.5f)
                path.quadraticBezierTo(width * 0.5f, height * 0.7f, width, height * 0.4f)
                drawPath(path, leafColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f))
            }
            com.example.ui.theme.AppThemeType.OCEAN_BLUE -> {
                val waveColor = Color(0xFF00ACC1).copy(alpha = 0.05f)
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height * 0.7f)
                path.cubicTo(width * 0.3f, height * 0.6f, width * 0.7f, height * 0.8f, width, height * 0.7f)
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
                drawPath(path, waveColor)
                drawCircle(waveColor, radius = 60f, center = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.3f))
                drawCircle(waveColor, radius = 40f, center = androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.5f))
            }
        }
    }
}

@Composable
fun DashboardCardDecoration(themeMode: com.example.ui.theme.AppThemeType) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        when (themeMode) {
            com.example.ui.theme.AppThemeType.AWAN_WAYANG, com.example.ui.theme.AppThemeType.AWAN_WAYANG_DARK -> {
                val color = if(themeMode == com.example.ui.theme.AppThemeType.AWAN_WAYANG) Color.White.copy(alpha=0.3f) else Color.White.copy(alpha=0.1f)
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height * 0.6f)
                path.quadraticBezierTo(width * 0.25f, height * 0.4f, width * 0.5f, height * 0.7f)
                path.quadraticBezierTo(width * 0.75f, height * 0.9f, width, height * 0.5f)
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
                drawPath(path, color)
                drawCircle(color, radius = width * 0.15f, center = androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.2f))
            }
            com.example.ui.theme.AppThemeType.DARK_FUTURISTIC -> {
                val color = Color(0xFF00FFCC).copy(alpha=0.2f)
                val size = 50f
                val centerX = width * 0.9f
                val centerY = height * 0.5f
                val hexPath = androidx.compose.ui.graphics.Path()
                for (i in 0..5) {
                    val angle = i * Math.PI / 3
                    val px = centerX + size * kotlin.math.cos(angle).toFloat()
                    val py = centerY + size * kotlin.math.sin(angle).toFloat()
                    if (i == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
                }
                hexPath.close()
                drawPath(hexPath, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
            }
            com.example.ui.theme.AppThemeType.NATURE_FOREST -> {
                val color = Color.White.copy(alpha=0.2f)
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height)
                path.quadraticBezierTo(width * 0.3f, height * 0.5f, width * 0.7f, height)
                drawPath(path, color)
            }
            com.example.ui.theme.AppThemeType.OCEAN_BLUE -> {
                val color = Color.White.copy(alpha=0.2f)
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, height * 0.5f)
                path.quadraticBezierTo(width * 0.25f, height * 0.3f, width * 0.5f, height * 0.6f)
                path.quadraticBezierTo(width * 0.75f, height * 0.8f, width, height * 0.5f)
                path.lineTo(width, height)
                path.lineTo(0f, height)
                drawPath(path, color)
            }
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    currentDate: java.util.Calendar,
    onDismiss: () -> Unit,
    onSave: (month: Int, year: Int) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(currentDate.get(java.util.Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(currentDate.get(java.util.Calendar.YEAR)) }

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pilih Bulan & Tahun", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Tahun Sebelumnya")
                    }
                    Text(text = "$selectedYear", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Tahun Berikutnya")
                    }
                }
                
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(months.size) { index ->
                        val isSelected = selectedMonth == index
                        androidx.compose.material3.TextButton(
                            onClick = { selectedMonth = index },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(months[index], style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedMonth, selectedYear) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

