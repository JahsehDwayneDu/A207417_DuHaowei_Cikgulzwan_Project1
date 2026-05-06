package com.dwayne.a207417_duhaowei_dwtechres_lab01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
// 使用兼容旧版 API 的时间库
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ==========================================
// 1. 数据模型与主活动
// ==========================================

data class WaterRecord(
    val id: Long = System.currentTimeMillis(),
    val amount: Int,
    // 兼容 API 24 的时间获取方式
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 设置主题并调用主程序入口
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HealthApp()
                }
            }
        }
    }
}

// ==========================================
// 2. ViewModel 与 状态 (Task 3: ViewModel)
// ==========================================

data class HealthUiState(
    val userName: String = "Dwayne",
    val matricNo: String = "A207417",
    val waterRecords: List<WaterRecord> = emptyList(),
    val waterGoal: Int = 2500,
    val steps: Int = 4200,
    val stepGoal: Int = 8000
)

class HealthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun addWater(amount: Int) {
        val newRecord = WaterRecord(amount = amount)
        _uiState.update { it.copy(waterRecords = listOf(newRecord) + it.waterRecords) }
    }
}

// ==========================================
// 3. 导航架构 (Task 2: 5 Screens Minimum)
// ==========================================

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "Logs", Icons.AutoMirrored.Filled.List)
    object Add : Screen("add", "Record", Icons.Default.AddCircle)
    object Tips : Screen("tips", "Tips", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun HealthApp() {
    val navController = rememberNavController()
    val viewModel: HealthViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Add.route) { AddWaterScreen(viewModel, navController) }
            composable(Screen.Tips.route) { TipsScreen() }
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        }
    }
}

// ==========================================
// 4. 各个页面实现 (UI + SDG 3 Focus)
// ==========================================

@Composable
fun DashboardScreen(viewModel: HealthViewModel) {
    val state by viewModel.uiState.collectAsState()
    val total = state.waterRecords.sumOf { it.amount }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Hello, ${state.userName}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Advancing SDG 3: Health & Well-being", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Daily Hydration", fontWeight = FontWeight.SemiBold)
                Text("$total / ${state.waterGoal} ml", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (total.toFloat() / state.waterGoal).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Steps", "${state.steps}", Icons.AutoMirrored.Filled.DirectionsRun, Modifier.weight(1f))
            StatCard("Goal", "${(state.steps*100)/state.stepGoal}%", Icons.Default.CheckCircle, Modifier.weight(1f))
        }
    }
}

@Composable
fun HistoryScreen(viewModel: HealthViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hydration Logs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (state.waterRecords.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data. Start drinking water!")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.waterRecords) { record ->
                    ListItem(
                        headlineContent = { Text("${record.amount} ml") },
                        supportingContent = { Text("Log Time: ${record.time}") },
                        leadingContent = { Icon(Icons.Default.LocalDrink, contentDescription = null, tint = Color(0xFF2196F3)) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddWaterScreen(viewModel: HealthViewModel, navController: NavHostController) {
    var amount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF2196F3))
        Text("Log New Intake", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount in ml") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val valInt = amount.toIntOrNull() ?: 0
                if (valInt > 0) {
                    viewModel.addWater(valInt)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Confirm")
        }
    }
}

@Composable
fun TipsScreen() {
    val tips = listOf(
        "Tip: Drink 500ml water right after waking up.",
        "SDG 3 Fact: Clean water prevents many diseases.",
        "Tip: Use a reusable bottle to protect the planet.",
        "Tip: Don't wait until you're thirsty to drink."
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Health Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        items(tips) { tip ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(tip, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: HealthViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(Color.LightGray, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(state.userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Matric No: ${state.matricNo}", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        ListItem(headlineContent = { Text("App Project") }, trailingContent = { Text("Project 1") })
        ListItem(headlineContent = { Text("SDG Goal") }, trailingContent = { Text("Goal 3") })
    }
}

// ==========================================
// 5. 辅助 UI 组件
// ==========================================

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Dashboard, Screen.History, Screen.Add, Screen.Tips, Screen.Profile)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}