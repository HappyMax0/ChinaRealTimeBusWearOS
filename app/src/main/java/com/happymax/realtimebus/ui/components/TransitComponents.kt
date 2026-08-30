package com.happymax.realtimebus.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happymax.realtimebus.shared.model.ArrivalStatusType
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.model.CrowdednessLevel
import com.happymax.realtimebus.shared.model.RealtimeArrivalInfo
import com.happymax.realtimebus.ui.theme.BusArrivalFast
import com.happymax.realtimebus.ui.theme.BusArrivalModerate
import com.happymax.realtimebus.ui.theme.BusArrivalSoon
import com.happymax.realtimebus.ui.theme.BusNightMode

@Composable
fun ArrivalStatusBadge(
    realtime: RealtimeArrivalInfo,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (realtime.statusType) {
        ArrivalStatusType.ARRIVING_SOON -> Triple(
            BusArrivalSoon.copy(alpha = 0.15f),
            BusArrivalSoon,
            "即将到站"
        )
        ArrivalStatusType.ON_WAY -> Triple(
            BusArrivalFast.copy(alpha = 0.15f),
            BusArrivalFast,
            "约 ${realtime.etaMinutes} 分钟 (${realtime.stopsAway}站)"
        )
        ArrivalStatusType.WAITING_DEPARTURE -> Triple(
            BusArrivalModerate.copy(alpha = 0.15f),
            BusArrivalModerate,
            "等待发车 · 约${realtime.etaMinutes}分"
        )
        ArrivalStatusType.OUT_OF_SERVICE -> Triple(
            BusNightMode.copy(alpha = 0.15f),
            BusNightMode,
            "已停运"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CrowdednessBadge(
    crowdedness: CrowdednessLevel,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (crowdedness) {
        CrowdednessLevel.COMFORTABLE -> BusArrivalFast to "舒适"
        CrowdednessLevel.MODERATE -> BusArrivalModerate to "适中"
        CrowdednessLevel.CROWDED -> BusArrivalSoon to "拥挤"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BusLineRowItem(
    line: BusLineInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Line Number Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = line.lineShortName.ifBlank { line.lineName },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = line.direction,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "首末 ${line.startTime}-${line.endTime} · 票价 ${line.price}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                ArrivalStatusBadge(realtime = line.realtime)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CrowdednessBadge(crowdedness = line.realtime.crowdedness)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = line.realtime.busPlate,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LiveRefreshCounterBadge(
    countdownSeconds: Int,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "refresh_rotation"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onManualRefresh)
            .testTag("refresh_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "刷新",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRefreshing) "同步中..." else "${countdownSeconds}s 自动刷新",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
