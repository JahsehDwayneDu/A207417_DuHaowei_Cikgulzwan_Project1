// 确保这里的包名和你 Android Studio 左侧目录树显示的完全一致
package com.dwayne.a207417_duhaowei_dwtechres_lab01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Task 1: 定义 Material 3 主题色 (取代之前硬编码的颜色) [cite: 28, 30]
val PrimaryBlue = Color(0xFF005B96)
val SecondaryBlue = Color(0xFF5B92E5)
val BackgroundGray = Color(0xFFF8F9FA)

private val AppColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    background = BackgroundGray,
    surfaceVariant = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Task 1: 应用全局 Material Theme
            MaterialTheme(colorScheme = AppColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Sdg3HealthDashboard()
                }
            }
        }
    }
}

@Composable
fun Sdg3HealthDashboard() {
    var waterInput by remember { mutableStateOf("") }
    var totalWater by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. 顶部栏
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Dwayne's Health",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Matric: A207417", color = Color.Gray, fontSize = 14.sp)
        }

        // 2. 交互区域：饮水量记录 (已升级为可展开的 Card)
        HydrationCard(
            waterInput = waterInput,
            onWaterInputChange = { waterInput = it },
            totalWater = totalWater,
            onAddWater = {
                val amount = waterInput.toIntOrNull() ?: 0
                totalWater += amount
            },
            onClearInput = { waterInput = "" }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 静态统计：改为使用官方 ElevatedCard
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
                HealthStatItem(Icons.AutoMirrored.Filled.DirectionsWalk, "3,330 steps", "Goal: 6,000")
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(progress = { 0.55f }, color = MaterialTheme.colorScheme.primary)
                    Text("55%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 包含动画和官方 Card 的组件 [cite: 35, 36]
@Composable
fun HydrationCard(
    waterInput: String,
    onWaterInputChange: (String) -> Unit,
    totalWater: Int,
    onAddWater: () -> Unit,
    onClearInput: () -> Unit
) {
    // 状态控制：卡片是否展开 [cite: 17, 18]
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            // Task 3: 使用 animateContentSize 实现平滑展开/收起动画
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 头部：标题与展开按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hydration Tracker", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 核心数据显示 (始终可见)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Total: $totalWater ml",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 隐藏的内容 (仅展开时可见) [cite: 18]
            if (expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.background)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = waterInput,
                        onValueChange = onWaterInputChange,
                        label = { Text("Enter water amount (ml)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onAddWater()
                            onClearInput()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Record Intake")
                    }
                }
            }
        }
    }
}

@Composable
fun HealthStatItem(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}