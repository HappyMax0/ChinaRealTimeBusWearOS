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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScrollIndicator
import kotlinx.coroutines.delay
import kotlin.math.sin


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
            HomeScreen(LocalContext.current, { navController.navigate("search") }, { site, isReverse -> navController.navigate("site/${site}/${isReverse}")},{ navController.navigate("citiesList") }, onLanguageSettingClick = { navController.navigate("language") }, onAboutClick = { navController.navigate("about") })
        }
        composable("site/{site}/{reverse}") {
                backStackEntry ->
            // 从 backStackEntry 中获取参数
            val site = backStackEntry.arguments?.getString("site")
            val isReverse = backStackEntry.arguments?.getBoolean("reverse")?:false
            site?.let{
                SitePage(LocalContext.current, site, isReverse)
            }
        }
        composable("citiesList"){
            CitiesList(LocalContext.current)
        }
        composable("about"){
            AboutScreen()
        }
        composable("language"){
            LanguageScreen(LocalContext.current)
        }
    }
}
}

@Composable
fun HomeScreen(context: Context, onAddSiteClick: () -> Unit, onSiteListItemClicked: (String, Boolean) -> Unit, onSelectCityClick:() -> Unit, onLanguageSettingClick: () -> Unit, onAboutClick:() -> Unit, viewModel: BusViewModel = viewModel()){
    // 收集 ViewModel 中的 UI 狀態
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val busList = mutableListOf<LolimiBusApiParam>()

    var city by remember { mutableStateOf("上海") }

    // 使用 LaunchedEffect 在 Composable 首次加載時觸發一次數據請求
    // 這裡我們硬編碼了參數，你也可以換成 TextField 讓用戶輸入
    LaunchedEffect(Unit) {
        city = LolimiBusApi.getCity(context.dataStore)?:"上海"

        for(site in LolimiBusApi.getList(context.dataStore)){
            busList.add(LolimiBusApiParam(type = "json", // 範例參數
                city = city,
                line = site.name,
                o = if(site.isReverse) "2" else "1" ))
        }

        while (true){
            viewModel.fetchBusListData(busList)
            delay(60000) // 60 秒
        }
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
                                    ShowLineList(city, listState, state.responseList)
                                }
                                is BusUiState.Error -> {
                                    // 3. 錯誤狀態
                                    ErrorScreen(city, state.message)
                                }
                            }
                        }
                        1 -> AddSiteList(listState, context, onAddSiteClick, onSiteListItemClicked)
                        2 -> SettingsPage(listState, onCityiesSettingClick = onSelectCityClick, onLanguageSettingClick, onAboutClick)
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
fun SettingsPage(listState: ScalingLazyListState, onCityiesSettingClick: ()->Unit, onLanguageSettingClick: ()->Unit, onAboutClick: ()->Unit){

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(stringResource(R.string.settings)) }
        }
        item{
            OvalButton(painterResource(R.drawable.location_city_24px), stringResource(R.string.city), onCityiesSettingClick)
        }
       /* item{
            OvalButton(painterResource(R.drawable.g_translate_24px), stringResource(R.string.language), onLanguageSettingClick)
        }*/
        item{
            OvalButton(painterResource(R.drawable.info_24px), stringResource(R.string.about), onAboutClick)
        }
    }
}

@Composable
fun CitiesList(context: Context) {
    var citiesList = listOf<String>("上海","南京","苏州","北京","天津","广州","深圳")
    var selectedCity by remember { mutableStateOf<String>("上海") }
    var showInputField by remember { mutableStateOf(false) }
    val state = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        selectedCity = LolimiBusApi.getCity(context.dataStore)
    }

    ScreenScaffold(
        scrollState = state,
        contentPadding = contentPadding,
        timeText = { TimeText() },
        scrollIndicator = { ScrollIndicator(state) },
        edgeButton = { /* 可选边缘按钮 */ }
    ) { innerPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = innerPadding
        ) {
            item{
                ListHeader{
                    Text(stringResource(R.string.city))
                }
            }
            item {
                // 顯示輸入框
                Card(
                    onClick = { showInputField = !showInputField },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if(showInputField)
                    {
                        TextField(
                            value = selectedCity,
                            onValueChange = { selectedCity = it },
                            placeholder = { Text(stringResource(R.string.input_city_name))},
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.height(32.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, // 回車鍵顯示為「完成」
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {

                                    if(!selectedCity.isNullOrEmpty())
                                    {
                                        coroutineScope.launch {
                                            LolimiBusApi.saveCity( context.dataStore, selectedCity.trim())
                                        }
                                    }

                                    keyboardController?.hide()

                                    showInputField = false
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                disabledLabelColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White,
                                disabledBorderColor = Color.White,
                                errorBorderColor = Color.Red,
                                errorLabelColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.Red
                            )
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
                    else{
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selectedCity)
                            Icon(painterResource(R.drawable.edit_24px), contentDescription = stringResource(R.string.edit))
                        }

                    }
                }
            }
            items(citiesList){ city->

                ToggleChip(
                    checked = selectedCity == city,
                    onCheckedChange = {
                        selectedCity = if (it) city else "上海"

                        coroutineScope.launch {
                            LolimiBusApi.saveCity( context.dataStore, city?:"上海")
                        }
                    },
                    label = {
                        Text(text = city, style = MaterialTheme.typography.body1)
                    },
                    toggleControl = {
                        Checkbox(checked = selectedCity == city)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AboutScreen(){
    val state = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = state,
        contentPadding = contentPadding,
        timeText = { TimeText() },
        scrollIndicator = { ScrollIndicator(state) },
        edgeButton = { /* 可选边缘按钮 */ }
    ) { innerPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = innerPadding
        ) {
            item{
                ListHeader{
                    Text(stringResource(R.string.about))
                }
            }
            item{
                Column {
                    Text(stringResource(R.string.about_content))
                    Text("https://api.lolimi.cn/")
                    Text(stringResource(R.string.developer))
                    Text("HappyMax")
                    Text("https://github.com/HappyMax0")
                }
            }
        }
    }
}

@Composable
fun LanguageScreen(context: Context){
    var languageList = listOf<String>("system", "en-us", "zh-cn", "zh-hk", "zh-mo", "zh-tw")
    var selectedLanguage by remember { mutableStateOf<String>("system") }
    val state = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        selectedLanguage = LolimiBusApi.getLanguage(context.dataStore)
    }

    ScreenScaffold(
        scrollState = state,
        contentPadding = contentPadding,
        timeText = { TimeText() },
        scrollIndicator = { ScrollIndicator(state) },
        edgeButton = { /* 可选边缘按钮 */ }
    ) { innerPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = innerPadding
        ) {
            item{
                ListHeader{
                    Text(stringResource(R.string.language))
                }
            }
            items(languageList){ lang->

                ToggleChip(
                    checked = selectedLanguage == lang,
                    onCheckedChange = {
                        selectedLanguage = if (it) lang else "system"

                        coroutineScope.launch {
                            LolimiBusApi.saveLanguage( context.dataStore, selectedLanguage)
                        }
                    },
                    label = {
                        Text(text = lang, style = MaterialTheme.typography.body1)
                    },
                    toggleControl = {
                        Checkbox(checked = selectedLanguage == lang)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
fun SitePage(context:Context, siteName:String, isReverse: Boolean){
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberScalingLazyListState()

    var siteList = mutableListOf<Site>()

    var site:Site? = null

    var city by remember { mutableStateOf("上海") }

    // 使用 LaunchedEffect 在 Composable 首次加載時觸發一次數據請求
    // 這裡我們硬編碼了參數，你也可以換成 TextField 讓用戶輸入
    LaunchedEffect(Unit) {
        city = LolimiBusApi.getCity(context.dataStore)?:"上海"
        siteList = LolimiBusApi.getList(context.dataStore)
        site = siteList.firstOrNull{ it != null && it.name.equals(siteName) && it.isReverse == isReverse }
    }

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            DeleteButton({
                siteList.remove(site)
                coroutineScope.launch {
                    LolimiBusApi.saveList( context.dataStore, siteList)
                }
            })
        }
    ) { paddingValues ->
        ScalingLazyColumn(state = listState,
            contentPadding = contentPadding) {
            item{
                ListHeader {
                    Text(siteName, fontSize = 20.sp)
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
        modifier = Modifier.size(26.dp)
    ) {
        // 根据选中状态显示不同图标
        val icon = if (isToggled) toggledIcon else unToggledIcon
        Icon(
            icon,
            contentDescription = if (isToggled) toggledDescription else unToggledDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AddSiteList(listState: ScalingLazyListState, context: Context, onAddSite: () -> Unit, onItemClicked: (String, Boolean) -> Unit){

    var inputText by remember { mutableStateOf(" ") }
    var showInputField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    // 1. 建立一個標記，用來追蹤是否「曾經」獲得過焦點
    var hasGainedFocus by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
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
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.height(32.dp)
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
                                    inputText = text.trim()

                                    keyboardController?.hide()

                                    if(!inputText.isNullOrEmpty())
                                    {
                                        siteList.add(Site(inputText, false))

                                        coroutineScope.launch {
                                            LolimiBusApi.saveList( context.dataStore, siteList)
                                        }
                                    }

                                    showInputField = false
                                }
                            ),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.input_site_name)) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                disabledLabelColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White,
                                disabledBorderColor = Color.White,
                                errorBorderColor = Color.Red,
                                errorLabelColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.Red
                            )

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

            Card(onClick = { onItemClicked(site.name, site.isReverse) }) {
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
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(cityName: String, errorText:String){
    val listState = rememberTransformingLazyColumnState()

    TransformingLazyColumn( modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        item{
            ListHeader { Text(cityName) }
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
fun ShowLineList(cityName:String?, listState: ScalingLazyListState, responseList:MutableList<BusResponse?>){

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        state = listState
    ) {
        item {
            ListHeader { Text(cityName?:"上海") }
        }
        items(responseList.size) { index ->
            Card(onClick = { }) {
                LineItem(responseList[index])
            }
        }
    }
}

@Composable
fun LineItem(response:BusResponse?){
    Column {
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            response?.line?.let { Text(it) }
//            RoundToggleButton(false, painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.forward), painterResource(R.drawable.swap_horiz_24px), stringResource(R.string.reverse), { } )
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
            response?.data?.forEach { busInfo ->
                Column(modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth()) {
                    Row {
                        Text(busInfo.lines, fontSize = 14.sp)
                        Text("->")
                        Text(busInfo.endSn, fontSize = 14.sp)
                    }
                    Row {
                        Text(busInfo.surplus, fontSize = 14.sp)
                        Text(" - ", fontSize = 14.sp)
                        Text(busInfo.travelTime, fontSize = 14.sp)
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
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.delete_24px),
                contentDescription = stringResource(R.string.delete),
                modifier = Modifier.size(24.dp)
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {

}