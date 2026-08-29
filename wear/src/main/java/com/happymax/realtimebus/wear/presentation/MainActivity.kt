/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.happymax.realtimebus.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.happymax.realtimebus.wear.R
import com.happymax.realtimebus.wear.presentation.theme.RealTimeBusTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 注意：以下必须导入 Wear OS 专属的 Compose 组件
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.TimeText
import com.happymax.realtimebus.shared.model.ArrivalStatusType
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.wear.presentation.theme.TransitPrimary
import com.happymax.realtimebus.wear.presentation.viewmodel.WearBusViewModel
import kotlin.getValue
import com.happymax.realtimebus.wear.presentation.viewmodel.WearBusViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: WearBusViewModel by viewModels {
        val app = application as BusApplication
        WearBusViewModelFactory(app, app.repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RealTimeBusTheme{
                RealWearBusFavoritesScreen(viewModel)
            }
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    RealTimeBusTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("More")
                    }
                },
            ) { contentPadding -> // ScreenScaffold provides default padding; adjust as needed
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = stringResource(R.string.hello_world, greetingName))
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button A")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button B")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button C")
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun RealWearBusFavoritesScreen(viewModel: WearBusViewModel) {
    // 管理列表状态，用于支持表冠滚动和弧形边缘效果
    val listState = rememberScalingLazyListState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Wear OS 的 Scaffold 用于管理顶部系统时间和右侧滚动条
    Scaffold(
        modifier = Modifier.background(Color.Black),
        timeText = {
            // 当列表正在滚动时隐藏时间，避免重叠遮挡
            if (!listState.isScrollInProgress) {
                TimeText()
            }
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        // 使用 ScalingLazyColumn 替代原先的 Column + verticalScroll
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = 28.dp, // 为顶部的 TimeText 留出空间
                bottom = 32.dp,
                start = 10.dp,
                end = 10.dp
            )
        ) {
            // 1. 顶部标题区域
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color(0xFF29B6F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "实时公交",
                        color = Color(0xFFE0E0E0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. 列表内容区域
            if (uiState.favoriteStations.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "暂无收藏站点",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "请在手机端添加",
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                items(uiState.favoriteStations) { station ->
                    // 这里直接复用你原有的 WatchStationItem
                    WatchStationItem(
                        station = station
                    )
                }

                // 3. 底部刷新按钮 (使用 Wear OS 标准的 CompactChip)
                item {
                    CompactChip(
                        onClick = viewModel::manualRefresh,
                        label = {
                            Text(
                                text = "点击更新 (${uiState.refreshCountdown}s)",
                                color = Color(0xFF81D4FA),
                                fontSize = 10.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFF81D4FA),
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = ChipDefaults.primaryChipColors(
                            backgroundColor = Color(0xFF1E222D)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WatchStationItem(
    station: BusStation,
    isAmbient: Boolean = false,
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
                androidx.compose.material3.Text(
                    text = station.name,
                    color = if (isAmbient) Color.White else Color(0xFFE3F2FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Text(
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
                        androidx.compose.material3.Text(
                            text = line.lineShortName.ifBlank { line.lineName },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    androidx.compose.material3.Text(
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


@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}