package com.happymax.realtimebus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happymax.realtimebus.ui.dialogs.BusLineDetailBottomSheet
import com.happymax.realtimebus.ui.screens.FavoritesTab
import com.happymax.realtimebus.ui.screens.SearchTab
import com.happymax.realtimebus.ui.screens.WearCompanionTab
import com.happymax.realtimebus.ui.theme.MyApplicationTheme
import com.happymax.realtimebus.ui.theme.TransitPrimary
import com.happymax.realtimebus.ui.viewmodel.BusViewModel
import com.happymax.realtimebus.ui.viewmodel.BusViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BusViewModel by viewModels {
        val app = application as BusApplication
        BusViewModelFactory(app, app.repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.messageSnackbar) {
                    uiState.messageSnackbar?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbarMessage()
                    }
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTabIndex) {
                                        0 -> "收藏站点"
                                        1 -> "搜索站点"
                                        else -> "手表端同步"
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier.testTag("main_top_app_bar")
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.testTag("main_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "收藏站点"
                                    )
                                },
                                label = {
                                    Text("收藏站点",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_favorites")
                            )

                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == 1) Icons.Default.Search else Icons.Outlined.Search,
                                        contentDescription = "搜索站点"
                                    )
                                },
                                label = {
                                    Text("搜索站点",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal)
                                        },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_search")
                            )

                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == 2) Icons.Default.Watch else Icons.Outlined.Watch,
                                        contentDescription = "手表端预览"
                                    )
                                },
                                label = { Text("手表端 (WearOS)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_wear")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = selectedTabIndex,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_content"
                        ) { tab ->
                            when (tab) {
                                0 -> FavoritesTab(
                                    favorites = uiState.favoriteStations,
                                    filterQuery = uiState.favoritesFilterQuery,
                                    onFilterQueryChanged = viewModel::onFavoritesFilterChanged,
                                    refreshCountdown = uiState.refreshCountdown,
                                    isRefreshing = uiState.isRefreshing,
                                    onManualRefresh = viewModel::manualRefresh,
                                    onRemoveFavorite = viewModel::removeFavorite,
                                    onLineClick = viewModel::showLineDetail,
                                    onNavigateToSearch = { selectedTabIndex = 1 }
                                )
                                1 -> SearchTab(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                                    selectedCity = uiState.selectedCity,
                                    onCitySelected = viewModel::onCitySelected,
                                    searchResults = uiState.searchResults,
                                    isSearching = uiState.isSearching,
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onLineClick = viewModel::showLineDetail,
                                    onPerformSearch = viewModel::performSearch
                                )
                                2 -> WearCompanionTab(
                                    favorites = uiState.favoriteStations,
                                    refreshCountdown = uiState.refreshCountdown,
                                    isRefreshing = uiState.isRefreshing,
                                    onManualRefresh = viewModel::manualRefresh
                                )
                            }
                        }

                        // Bus Line Detail Modal Bottom Sheet
                        uiState.selectedLineDetail?.let { line ->
                            BusLineDetailBottomSheet(
                                line = line,
                                station = uiState.selectedStationDetail,
                                onDismiss = viewModel::dismissLineDetail
                            )
                        }
                    }
                }
            }
        }
    }
}