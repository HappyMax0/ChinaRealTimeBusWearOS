/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.realtimebuscn.presentation

import android.accessibilityservice.GestureDescription
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.PagerScaffoldDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScreenScaffoldDefaults.contentPadding
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.realtimebuscn.R
import com.example.realtimebuscn.presentation.theme.RealTimeBusCNTheme
import kotlin.time.Duration.Companion.minutes
import androidx.wear.compose.material.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            RealTimeBusCNTheme {
                WearApp()
            }
        }
    }
}

@Composable
fun WearApp() {

AppScaffold {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // TODO: build navigation graph
        composable("home") {
            HomeScreen({ navController.navigate("search") }, { navController.navigate("details/28路") },  { navController.navigate("station/杨树浦路眉州路")})
        }
        composable("details/{line}") {
                backStackEntry ->
            // 从 backStackEntry 中获取参数
            val line = backStackEntry.arguments?.getString("line")
            line?.let {
                LineDetailPage(line, { navController.navigate("site/28路/杨树浦路眉州路") })
            }
        }
        composable("site/{line}/{site}") {
                backStackEntry ->
            // 从 backStackEntry 中获取参数
            val line = backStackEntry.arguments?.getString("line")
            val site = backStackEntry.arguments?.getString("site")
            line?.let {
                site?.let{
                    SitePage(line, site)
                }
            }
        }
        composable("station/{name}"){
                backStackEntry ->
            // 从 backStackEntry 中获取参数
            val stationName = backStackEntry.arguments?.getString("name")
            stationName?.let {
                StationPage(stationName, { navController.navigate("site/28路/杨树浦路眉州路") })
            }
        }
        composable("search") {
            SearchPage({ result -> navController.navigate("searchResult/result") }, { navController.navigate("home") })
        }
        composable("searchResult/{query}") {
                backStackEntry ->
            // 从 backStackEntry 中获取参数
            val query = backStackEntry.arguments?.getString("query")
            query?.let {
                ResultsScreen(query, { navController.navigate("home") })
            }

        }
    }
}

}

@Composable
fun HomeScreen(onSearch: () -> Unit, onFavouriteListItemClicked: () -> Unit, onNearbyListItemClicked: () -> Unit){
    val line1 = LineStation("28路", "杨树浦路眉州路", "包头路嫩江路", 9,  19.minutes)
    val line2 = LineStation("135路", "杨树浦路眉州路", "杨树浦路黎平路", 5,  10.minutes)
    val stationsList = mutableListOf<LineStation>(line1, line2)

    var site1 = SiteStation("杨树浦路眉州路",  mutableListOf<LineStation>(line1, line2))

    val siteList = mutableListOf<SiteStation>(site1)

    val pageCount = 3
    val pagerState = rememberPagerState { pageCount }

    val listState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            timeText = { null }
        ) {
                paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                HorizontalPager(
                    state = pagerState,
                    flingBehavior =
                        PagerScaffoldDefaults.snapWithSpringFlingBehavior(state = pagerState),
                ) { page ->
                    when (page) {
                        0 -> FavouriteList(stationsList, onSearch, onFavouriteListItemClicked)
                        1 -> NearbyList(siteList, onSearch, onNearbyListItemClicked)
                        2 -> SettingsPage()
                    }
                }

                // 页面指示器
                HorizontalPageIndicator(
                    pagerState = pagerState,
                    selectedColor = MaterialTheme.colors.primary,
                    unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }

        }
    }
}

@Composable
fun SettingsPage(){
    val listState = rememberTransformingLazyColumnState()

    TransformingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(stringResource(R.string.settings)) }
        }
        item{
            OvalButton(Icons.Filled.LocationOn, stringResource(R.string.location), { })
        }
        item{
            OvalButton(Icons.Filled.Settings, stringResource(R.string.language), { })
        }
        item{
            OvalButton(Icons.Filled.Info, stringResource(R.string.about), { })
        }
    }
}

@Composable
fun OvalButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp), // Adjust the height as needed
        shape = RoundedCornerShape(24.dp), // This creates the pill/oval shape
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.surface
        )
    ) {
        Row {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
            Text(text = contentDescription.toString(), modifier = Modifier)
        }

    }
}

@Composable
fun SitePage(lineName:String, site:String){
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
    ) { paddingValues ->
        TransformingLazyColumn(state = listState,
            contentPadding = contentPadding) {
            item{
                ListHeader {
                    Column {
                        Row{
                            Text(lineName)
                            Text(" · ")
                            Text(site)
                        }
                        Row{
                            Text("终点站：")
                            Text("提篮桥")
                        }

                    }

                }
            }
            item {
                Row{
                    Text("5min")
                    Spacer(Modifier.width(20.dp))
                    Text("20min")
                }
            }
            item {
                Row{
                    RoundToggleButton(Icons.Filled.FavoriteBorder, stringResource(R.string.unFavorite), Icons.Filled.Favorite, stringResource(R.string.favorite), { } )

                    RoundToggleButton(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.forward), Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.reverse), { } )

                }
            }
       }
    }
}

@Composable
fun RoundToggleButton(unToggledIcon: ImageVector, unToggledDescription: String, toggledIcon: ImageVector, toggledDescription: String, onToggled: () -> Unit){
    var isToggled by remember { mutableStateOf(false) }

    ToggleButton(
        checked = isToggled,
        onCheckedChange = {
            isToggled = it
            onToggled },
        modifier = Modifier.padding(8.dp)
    ) {
        // 根据选中状态显示不同图标
        val icon = if (isToggled) toggledIcon else unToggledIcon
        Icon(
            imageVector = icon,
            contentDescription = if (isToggled) toggledDescription else unToggledDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun NearbyList(siteList:MutableList<SiteStation>, onSearch: () -> Unit, onItemClicked: () -> Unit){
    val listState = rememberTransformingLazyColumnState()
    TransformingLazyColumn( modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item {
            SearchButton(onSearch)
        }
        item{
            ListHeader { Text(stringResource(R.string.nearby)) }
        }
        items(siteList.size){
                index -> Card(onClick = onItemClicked) {
            Column {
                Text(siteList[index].name)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                    siteList[index].lineList.forEach { line ->
                        Row(modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(line.name)
                                Text(line.terminalStation)
                            }
                            Row {
                                Text(line.remainStations.toString())
                                Text("·")
                                Text(line.remainTime.toString())
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun FavouriteList(busList:MutableList<LineStation>, onSearch: () -> Unit, onItemClicked: () -> Unit){
    val listState = rememberTransformingLazyColumnState()

    TransformingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(stringResource(R.string.favourites)) }
        }
        items(busList.size){
                index -> Card(onClick = onItemClicked) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(busList[index].name)
                    Text(busList[index].stationName)
                    Text(busList[index].terminalStation)
                }

                Row {
                    Text(busList[index].remainStations.toString())
                    Text("·")
                    Text(busList[index].remainTime.toString())
                }
            }

        }
        }
    }
}

@Composable
fun SearchButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.6f).padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchPage(onSubmit: (String) -> Unit, onCancel: () -> Unit){
    val listState = rememberScalingLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 使用 Scaffold 包裹整个屏幕，提供 Wear OS 统一的界面框架
    Scaffold(
        // 提供一个 Vingnette 来模拟屏幕边缘的暗角效果，增强沉浸感
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        // 提供一个 TimeText 来显示当前时间
        timeText = { TimeText() },
        positionIndicator = {
            // 提供一个位置指示器，显示当前列表滚动的位置
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        var searchText by remember { mutableStateOf("") }
        // 使用 ScalingLazyColumn 作为主要内容布局，它专为 Wear OS 优化
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = AutoCenteringParams(itemIndex = 0),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // 在列表顶部添加一个占位符，使搜索框更靠近顶部
            item {
                Spacer(Modifier.height(16.dp))
            }
            item {
                // 使用 OutlinedTextField 作为搜索框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = {
                        Text(
                            text = stringResource(R.string.search),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // 限制宽度，防止内容溢出
                        .padding(horizontal = 8.dp),
                    // 在搜索框左侧添加搜索图标
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    // 配置键盘，在点击“搜索”后收起键盘
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSubmit(searchText)
                            // 这里可以处理搜索逻辑，例如发起网络请求
                            keyboardController?.hide() // 收起键盘
                        },
                    )
                )
            }
        }
    }
}

@Composable
private fun ResultsScreen(query: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    // 这里模拟搜索结果；实际项目里可挂接 ViewModel 发起网络/数据库查询
    val results by remember(query) {
        mutableStateOf(
            List(12) { idx -> "「$query」的结果 #${idx + 1}" }
        )
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            autoCentering = AutoCenteringParams(itemIndex = 0)
        ) {
            item {
                Text(
                    text = "搜索：$query",
                    style = MaterialTheme.typography.title3
                )
            }
            items(results.size) { index ->
                Card(onClick = { /* TODO: 打开详情 */ }) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(results[index], style = MaterialTheme.typography.body2)
                        Text("点按查看详情", style = MaterialTheme.typography.caption3)
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun LineDetailPage(lineName:String, onItemClicked: () -> Unit){
    val listState = rememberTransformingLazyColumnState()
    val terminalStation:String = "包头路嫩江路"
    val station1 = LineStation(lineName, "提篮桥", terminalStation, 9,  19.minutes)
    val station2 = LineStation(lineName, "临潼路杨树浦路", terminalStation, 9,  19.minutes)
    val station3 = LineStation(lineName, "杨树浦路大连路", terminalStation, 9,  19.minutes)
    val station4 = LineStation(lineName, "杨树浦路许昌路", terminalStation, 9,  19.minutes)
    val station5 = LineStation(lineName, "杨树浦路江浦路", terminalStation, 9,  19.minutes)
    val station6 = LineStation(lineName, "杨树浦路眉州路", terminalStation, 9,  19.minutes)
    val station7 = LineStation(lineName, "杨树浦路松潘路", terminalStation, 9,  19.minutes)
    val station8 = LineStation(lineName, "杨树浦路临青路", terminalStation, 9,  19.minutes)
    val station9 = LineStation(lineName, "杨树浦路宁武路", terminalStation, 9,  19.minutes)
    val station10 = LineStation(lineName, "隆昌路杨树浦路", terminalStation, 9,  19.minutes)

    val stations:List<LineStation> = listOf(station1, station2, station3, station4, station5, station6, station7, station8, station9, station10)
    var line = Line(lineName, terminalStation, stations)

    ScreenScaffold(
        scrollState = listState,
    ) { paddingValues ->
        TransformingLazyColumn(state = listState,
            contentPadding = contentPadding) {
            item{
                ListHeader {
                    Column {
                        Text(lineName)
                        Text(terminalStation)
                    }

                }
            }
            items(stations.size){
                    index -> Card(onClick = onItemClicked) {
                Row(modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(stations[index].stationName)
                    }
                    Row {
                        Text(stations[index].remainStations.toString())
                        Text("·")
                        Text(stations[index].remainTime.toString())
                    }
                }
            }
            }
        }
    }

}

@Composable
fun StationPage(stationName:String, onItemClicked: () -> Unit){
    val line1 = LineStation("28路", stationName, "包头路嫩江路", 9,  19.minutes)
    val line2 = LineStation("135路", stationName, "杨树浦路黎平路", 5,  10.minutes)
    val lineList = mutableListOf<LineStation>(line1, line2)

    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(
        scrollState = listState,
    ) { paddingValues ->
        TransformingLazyColumn(state = listState,
            contentPadding = contentPadding) {
            item{
                ListHeader {
                    Column {
                        Text(stationName)
                    }

                }
            }
            items(lineList.size){
                    index -> Card(onClick = onItemClicked) {
                Row(modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(lineList[index].name)
                        Text(lineList[index].terminalStation)
                    }
                    Row {
                        Text(lineList[index].remainStations.toString())
                        Text("·")
                        Text(lineList[index].remainTime.toString())
                    }
                }
            }
            }
        }
    }

}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    SitePage("28", "杨树浦路眉州路")
}