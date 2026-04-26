package com.dwayne.a207417_duhaowei_dwtechres_lab01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ==========================================
// 1. 数据模型与主题配置
// ==========================================

// Task 2: Data Class (包含至少2个字段)
data class HealthUiState(
    val totalWater: Int = 0,
    val waterGoal: Int = 2000,
    val steps: Int = 3330
)

// Task 3: 维持 Material Design 主题
val PrimaryBlue = Color(0xFF005B96)
val SecondaryBlue = Color(0xFF5B92E5)
val BackgroundGray = Color(0xFFF8F9FA)

private val AppColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    background = BackgroundGray,
    surfaceVariant = Color.White
)

// ==========================================
// 2. ViewModel 状态管理
// ==========================================

// Task 2: ViewModel Integration
class HealthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun addWater(amount: Int) {
        _uiState.update { currentState ->
            currentState.copy(totalWater = currentState.totalWater + amount)
        }
    }
}

// ==========================================
// 3. 主活动与导航架构
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HealthApp()
                }
            }
        }
    }
}

// 定义路由名称
enum class HealthScreen {
    Dashboard,
    AddWater,
    Summary
}

// Task 1 & 2: 导航控制器与页面路由分配
@Composable
fun HealthApp(
    viewModel: HealthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = HealthScreen.Dashboard.name
    ) {
        // 页面 1：主仪表盘
        composable(route = HealthScreen.Dashboard.name) {
            DashboardScreen(
                uiState = uiState,
                onNavigateToAddWater = { navController.navigate(HealthScreen.AddWater.name) },
                onNavigateToSummary = { navController.navigate(HealthScreen.Summary.name) }
            )
        }

        // 页面 2：记录饮水量表单
        composable(route = HealthScreen.AddWater.name) {
            AddWaterScreen(
                onAddWater = { amount ->
                    viewModel.addWater(amount)
                    navController.popBackStack() // 记录完返回上一页
                },
                onCancel = { navController.popBackStack() } // 取消并返回
            )
        }

        // 页面 3：数据总结视图
        composable(route = HealthScreen.Summary.name) {
            SummaryScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ==========================================
// 4. 各个屏幕组件 (Screens)
// ==========================================

@Composable
fun DashboardScreen(
    uiState: HealthUiState,
    onNavigateToAddWater: () -> Unit,
    onNavigateToSummary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Dwayne's Health",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Matric: A207417", color = Color.Gray, fontSize = 14.sp)
        }

        // 饮水卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Today's Water: ${uiState.totalWater} ml", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToAddWater,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Record Water Intake")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 步数卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = "${uiState.steps} steps", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Goal: 6,000", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(progress = { uiState.steps.toFloat() / 6000f }, color = MaterialTheme.colorScheme.primary)
                    Text("55%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onNavigateToSummary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Full Summary")
        }
    }
}

@Composable
fun AddWaterScreen(
    onAddWater: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var waterInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Log Hydration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = waterInput,
            onValueChange = { waterInput = it },
            label = { Text("Enter water amount (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amount = waterInput.toIntOrNull() ?: 0
                if (amount > 0) onAddWater(amount)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    uiState: HealthUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hydration Status", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Total Intake: ${uiState.totalWater} ml")
                    Text("Daily Goal: ${uiState.waterGoal} ml")
                    val remaining = maxOf(0, uiState.waterGoal - uiState.totalWater)
                    Text("Remaining: $remaining ml", color = if (remaining == 0) Color.Green else Color.Red)
                }
            }
        }
    }
}