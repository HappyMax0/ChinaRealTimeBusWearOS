/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.happymax.realtimebuscnwear

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
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
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.happymax.realtimebuscnwear.theme.RealTimeBusCNTheme
import androidx.wear.compose.material.*
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState


// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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
            HomeScreen(LocalContext.current, { navController.navigate("search") }, { navController.navigate("site/28路/杨树浦路眉州路")})
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

            }
        }
        composable("search") {
            SearchPage({ result -> navController.navigate("home/$result") })
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
fun HomeScreen(context: Context, onAddSiteClick: () -> Unit, onFavouriteListItemClicked: () -> Unit, viewModel: BusViewModel = viewModel()){
    // 收集 ViewModel 中的 UI 狀態
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val busList = mutableListOf<LolimiBusApiParam>()

    // 使用 LaunchedEffect 在 Composable 首次加載時觸發一次數據請求
    // 這裡我們硬編碼了參數，你也可以換成 TextField 讓用戶輸入
    LaunchedEffect(Unit) {

        for(site in LolimiBusApi.getList(context.dataStore)){
            busList.add(LolimiBusApiParam(type = "json", // 範例參數
                city = "上海",
                line = site.name,
                o = if(site.isReverse) "2" else "1" ))
        }

        viewModel.fetchBusListData(busList)
    }

    val pageCount = 3
    val pagerState = rememberPagerState { pageCount }
    val listState = rememberScalingLazyListState()

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            timeText = { null },
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
                        0 -> {
                            when (val state = uiState) {
                                is BusUiState.Loading -> {
                                    // 1. 加載中狀態
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is BusUiState.Success -> {
                                    // 2. 成功狀態
                                    ShowLineList(listState, state.responseList, onFavouriteListItemClicked)
                                }
                                is BusUiState.Error -> {
                                    // 3. 錯誤狀態
                                    ErrorScreen(state.message)
                                }
                            }
                        }
                        1 -> AddSiteList(listState, context, onAddSiteClick)
                        2 -> SettingsPage(listState)
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
fun SettingsPage(listState: ScalingLazyListState){

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(stringResource(R.string.settings)) }
        }
        item{
            OvalButton(painterResource(R.drawable.location_city_24px), stringResource(R.string.city), { })
        }
        item{
            OvalButton(painterResource(R.drawable.g_translate_24px), stringResource(R.string.language), { })
        }
        item{
            OvalButton(painterResource(R.drawable.info_24px), stringResource(R.string.about), { })
        }
    }
}

@Composable
fun OvalButton(
    imageVector: Painter,
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
                imageVector,
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
                    RoundToggleButton(false, painterResource(R.drawable.favorite_24px), stringResource(R.string.unFavorite), painterResource(R.drawable.favorite_24px), stringResource(R.string.favorite), { } )

                    RoundToggleButton(false, painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.forward), painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.reverse), { } )

                }
            }
       }
    }
}

@Composable
fun RoundToggleButton(state: Boolean, unToggledIcon: Painter, unToggledDescription: String, toggledIcon: Painter, toggledDescription: String, onToggled: (Boolean) -> Unit){
    var isToggled by remember { mutableStateOf(state) }

    ToggleButton(
        checked = isToggled,
        onCheckedChange = {
            isToggled = it
            onToggled(it) },
        modifier = Modifier.size(24.dp)
    ) {
        // 根据选中状态显示不同图标
        val icon = if (isToggled) toggledIcon else unToggledIcon
        Icon(
            icon,
            contentDescription = if (isToggled) toggledDescription else unToggledDescription,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun AddSiteList(listState: ScalingLazyListState, context: Context, onAddSite: () -> Unit){

    var inputText by remember { mutableStateOf("") }
    var showInputField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    // 1. 建立一個標記，用來追蹤是否「曾經」獲得過焦點
    var hasGainedFocus by remember { mutableStateOf(false) }

    var siteList = remember { mutableStateListOf<Site>() }

    LaunchedEffect(Unit){
        siteList.clear()

        for(site in LolimiBusApi.getList(context.dataStore)){
            siteList.add(site)
        }

    }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item {
            ListHeader{
                if (!showInputField) {
                    hasGainedFocus = false
                    AddButton({
                        showInputField = true
                    })
                }
                else{
                    // 顯示輸入框
                    Card(
                        onClick = { /* 防止點擊穿透 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        var text by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester).onFocusChanged{ focusState ->
                                    if (focusState.isFocused) {
                                        // 2. 只要一獲得焦點，就將標記設為 true
                                        hasGainedFocus = true
                                    }

                                    if(!focusState.isFocused && hasGainedFocus && showInputField)
                                        showInputField = false;
                                },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, // 回車鍵顯示為「完成」
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    inputText = text

                                    keyboardController?.hide()
                                    siteList.add(Site(inputText, false))

                                    coroutineScope.launch {
                                        LolimiBusApi.saveList( context.dataStore, siteList)
                                    }

                                    showInputField = false
                                }
                            ),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.input_site_name)) }
                        )

                        // 3. 使用 LaunchedEffect 在 Composable 首次出現時請求焦點
                        //    這會在這個 OutlinedTextField 被組合*之後*才運行
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
                }
            }
        }
        items(siteList.size){ index ->
            val site = siteList[index] // 獲取當前 site

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(site.name)

                RoundToggleButton(site.isReverse, painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.forward), painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.reverse),
                    {
                        siteList[index] = site.copy(isReverse = it)
                        coroutineScope.launch {
                            LolimiBusApi.saveList( context.dataStore, siteList)
                        }
                } )

                DeleteButton({
                    siteList.removeAt(index)
                    coroutineScope.launch {
                        LolimiBusApi.saveList( context.dataStore, siteList)
                    }
                })
            }
        }
    }
}

@Composable
fun ErrorScreen(errorText:String){
    val listState = rememberTransformingLazyColumnState()

    TransformingLazyColumn( modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(stringResource(R.string.nearby)) }
        }
       item{
           Box(
               modifier = Modifier
                   .fillMaxSize()
                   .padding(16.dp),
               contentAlignment = Alignment.Center
           ) {
               Text(
                   text = "GetError: ${errorText}"
               )
           }
        }
    }
}

@Composable
fun ShowLineList(listState: ScalingLazyListState, responseList:MutableList<BusResponse?>, onItemClicked: () -> Unit){

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        state = listState
    ) {
        item {
            ListHeader { Text(stringResource(R.string.bus_stop_list)) }
        }
        items(responseList.size) { index ->
            Card(onClick = onItemClicked) {
                LineItem(responseList[index])
            }
        }
    }
}

@Composable
fun LineItem(response:BusResponse?){
    Column {
        /*Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            response?.line?.let { Text(it) }
            RoundToggleButton(false, painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.forward), painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.reverse), { } )
        }*/

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
            response?.data?.forEach { busInfo ->
                Row(modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text(busInfo.lines, fontSize = 12.sp)
                        Text("->")
                        Text(busInfo.endSn, fontSize = 12.sp)
                    }
                    Column {
                        Text(busInfo.surplus, fontSize = 12.sp)
                        Text(busInfo.travelTime, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.6f).width(24.dp).height(24.dp)
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.delete_24px),
                contentDescription = stringResource(R.string.delete),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun AddButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.add_24px),
                contentDescription = stringResource(R.string.search),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchPage(onSubmit: (String) -> Unit){
    val listState = rememberTransformingLazyColumnState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }

    TransformingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally){
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
                    .focusRequester(focusRequester)
                    .fillMaxWidth(0.9f) // 限制宽度，防止内容溢出
                    .padding(horizontal = 8.dp),
                // 在搜索框左侧添加搜索图标
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.search_24px),
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

        }
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {

}