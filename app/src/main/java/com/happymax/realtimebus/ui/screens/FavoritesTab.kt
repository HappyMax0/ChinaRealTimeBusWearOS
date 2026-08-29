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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.ui.components.BusLineRowItem
import com.happymax.realtimebus.ui.components.LiveRefreshCounterBadge
import com.happymax.realtimebus.ui.theme.TransitPrimary
import com.happymax.realtimebus.ui.theme.TransitSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesTab(
    favorites: List<BusStation>,
    filterQuery: String,
    onFilterQueryChanged: (String) -> Unit,
    refreshCountdown: Int,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    onRemoveFavorite: (stationId: String, name: String) -> Unit,
    onLineClick: (line: BusLineInfo, station: BusStation) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLines = favorites.sumOf { it.lines.size }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Banner with Realtime Status
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = TransitPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "常用收藏站点",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "已收藏 ${favorites.size} 个站点 · 共 $totalLines 条线路",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LiveRefreshCounterBadge(
                            countdownSeconds = refreshCountdown,
                            isRefreshing = isRefreshing,
                            onManualRefresh = onManualRefresh
                        )
                    }

                    if (favorites.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Filter input within favorites
                        OutlinedTextField(
                            value = filterQuery,
                            onValueChange = onFilterQueryChanged,
                            placeholder = {
                                Text(
                                    "在已收藏中快速筛选站点或线路号...",
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (filterQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { onFilterQueryChanged("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "清除",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = TransitPrimary,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("favorites_filter_input")
                        )
                    }
                }
            }
        }

        // Empty state or filtered empty state
        if (favorites.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("empty_favorites_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TransitPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Explore,
                                    contentDescription = null,
                                    tint = TransitPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (filterQuery.isNotBlank()) "未找到匹配的收藏站点" else "暂无收藏站点",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (filterQuery.isNotBlank()) "尝试清除筛选关键词查看所有收藏"
                            else "前往「搜索站点」查找您常用的公交站，点击星标即可快速添加至此，手表端亦将同步呈现！",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToSearch,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TransitPrimary
                            ),
                            modifier = Modifier.testTag("go_to_search_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("立即去搜索并收藏")
                        }
                    }
                }
            }
        } else {
            // Station Cards List
            items(favorites, key = { it.id }) { station ->
                FavoriteStationCard(
                    station = station,
                    onRemoveFavorite = { onRemoveFavorite(station.id, station.name) },
                    onLineClick = { line -> onLineClick(line, station) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FavoriteStationCard(
    station: BusStation,
    onRemoveFavorite: () -> Unit,
    onLineClick: (BusLineInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("station_card_${station.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Station Title & City Tag & Unfavorite Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TransitSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = station.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TransitPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = station.city,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onRemoveFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("unfavorite_btn_${station.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "取消收藏",
                        tint = Color(0xFFFFA000)
                    )
                }
            }

            if (station.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = station.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bus lines passing this stop
            if (station.lines.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "暂无详细经停线路数据",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    station.lines.forEach { line ->
                        BusLineRowItem(
                            line = line,
                            onClick = { onLineClick(line) },
                            modifier = Modifier.testTag("line_item_${line.lineId}")
                        )
                    }
                }
            }
        }
    }
}
