package com.happymax.realtimebus.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happymax.realtimebus.shared.model.ArrivalStatusType
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.ui.theme.TransitPrimary
import com.happymax.realtimebus.ui.theme.TransitSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun WearCompanionTab(
    favorites: List<BusStation>,
    refreshCountdown: Int,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var watchShapeIsRound by remember { mutableStateOf(true) }
    var isAmbientMode by remember { mutableStateOf(false) }
    val currentTimeStr = remember(refreshCountdown) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wear_tab_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WearOS Feature Info Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = TransitSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wear OS 手表端同步",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SyncButton(isRefreshing, onManualRefresh)
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TransitSecondary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothConnected,
                                    contentDescription = null,
                                    tint = TransitSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "已配对就绪",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "手机端收藏的公交站点将自动实时同步至 Wear OS 手表，抬腕即可秒看经停线路与到站倒计时！",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Wear OS Tile & Notification Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wear OS 快捷微件 (Tiles & Complications)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ 快捷磁贴",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "手表左滑即看最近收藏站第1班车",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🔔 到站震动提醒",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "公交距本站<2分钟触发触感轻微提醒",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun WatchStationItem(
    station: BusStation,
    isAmbient: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isAmbient) Color(0xFF111111) else Color(0xFF1A1E29),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Station Header on Watch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.name,
                    color = if (isAmbient) Color.White else Color(0xFFE3F2FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = station.city,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lines on Watch
            station.lines.take(3).forEach { line ->
                val (color, timeText) = when (line.realtime.statusType) {
                    ArrivalStatusType.ARRIVING_SOON -> Color(0xFFFF5252) to "即将到站"
                    ArrivalStatusType.ON_WAY -> Color(0xFF69F0AE) to "${line.realtime.etaMinutes}分(${line.realtime.stopsAway}站)"
                    ArrivalStatusType.WAITING_DEPARTURE -> Color(0xFFFFD740) to "等发车"
                    ArrivalStatusType.OUT_OF_SERVICE -> Color.Gray to "停运"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Line Name Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isAmbient) Color.DarkGray else TransitPrimary)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = line.lineShortName.ifBlank { line.lineName },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = timeText,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SyncButton(
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 当正在同步时，禁用按钮点击 (enabled = !isSyncing)
    IconButton(
        onClick = onSyncClick,
        enabled = !isSyncing,
        modifier = modifier
    ) {
        if (isSyncing) {
            // 正在同步时显示转圈动画
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp), // 尺寸与 Icon 保持一致
                strokeWidth = 2.dp
            )
        } else {
            // 空闲时显示同步图标
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "同步数据到手表"
            )
        }
    }
}
