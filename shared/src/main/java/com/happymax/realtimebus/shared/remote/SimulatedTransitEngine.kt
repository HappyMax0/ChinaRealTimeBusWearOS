package com.happymax.realtimebus.shared.remote

import com.happymax.realtimebus.shared.model.ArrivalStatusType
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.shared.model.CrowdednessLevel
import com.happymax.realtimebus.shared.model.RealtimeArrivalInfo
import java.util.Calendar
import kotlin.math.abs

object SimulatedTransitEngine {

    /**
     * Compute dynamic real-time arrival predictions for a bus line based on current time and station seed.
     */
    fun calculateRealtimeArrival(lineName: String, stationId: String, currentTimestamp: Long = System.currentTimeMillis()): RealtimeArrivalInfo {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Check night / service window (typical 05:30 to 23:00)
        val isNight = (hour < 5 || (hour == 5 && minute < 30)) || (hour >= 23 && !lineName.contains("夜"))
        if (isNight) {
            return RealtimeArrivalInfo(
                stopsAway = 0,
                etaMinutes = 0,
                etaDistanceMeters = 0,
                statusText = "已停运 (首班 05:30)",
                statusType = ArrivalStatusType.OUT_OF_SERVICE,
                crowdedness = CrowdednessLevel.COMFORTABLE,
                busPlate = "暂无班车",
                nextBusEtaMinutes = 0
            )
        }

        // Pseudo-deterministic calculation using current time + line/station hash
        val seed = abs((lineName.hashCode() xor stationId.hashCode()) + (calendar.get(Calendar.DAY_OF_YEAR) * 31))
        val cycleMinutes = 10 + (seed % 6) // Bus headway 10-15 mins
        val totalSecondsInCycle = (minute * 60 + second + (seed % 100)) % (cycleMinutes * 60)
        val secondsUntilArrival = (cycleMinutes * 60) - totalSecondsInCycle
        val etaMinutes = (secondsUntilArrival / 60).coerceAtLeast(1)

        val stopsAway = when {
            etaMinutes <= 2 -> 1
            etaMinutes <= 5 -> 2
            etaMinutes <= 9 -> 3
            etaMinutes <= 13 -> 5
            else -> 7
        }

        val distanceMeters = (stopsAway * 650) + (secondsUntilArrival % 300)

        val statusType = when {
            etaMinutes <= 2 -> ArrivalStatusType.ARRIVING_SOON
            stopsAway > 6 -> ArrivalStatusType.WAITING_DEPARTURE
            else -> ArrivalStatusType.ON_WAY
        }

        val statusText = when (statusType) {
            ArrivalStatusType.ARRIVING_SOON -> "即将到站 (<2分钟)"
            ArrivalStatusType.ON_WAY -> "还有 $stopsAway 站 · 约 $etaMinutes 分钟"
            ArrivalStatusType.WAITING_DEPARTURE -> "等待发车 · 约 $etaMinutes 分钟"
            ArrivalStatusType.OUT_OF_SERVICE -> "已停运"
        }

        val crowdedness = when ((minute + seed) % 3) {
            0 -> CrowdednessLevel.COMFORTABLE
            1 -> CrowdednessLevel.MODERATE
            else -> CrowdednessLevel.CROWDED
        }

        val platePrefix = when {
            stationId.startsWith("BJ") || stationId.contains("010") -> "京A·"
            stationId.startsWith("SH") || stationId.contains("021") -> "沪A·"
            stationId.startsWith("GZ") || stationId.contains("020") -> "粤A·"
            stationId.startsWith("SZ") || stationId.contains("0755") -> "粤B·"
            stationId.startsWith("HZ") || stationId.contains("0571") -> "浙A·"
            else -> "京B·"
        }
        val plateNumber = "$platePrefix${10000 + (seed % 89999)}"

        return RealtimeArrivalInfo(
            stopsAway = stopsAway,
            etaMinutes = etaMinutes,
            etaDistanceMeters = distanceMeters,
            statusText = statusText,
            statusType = statusType,
            crowdedness = crowdedness,
            busPlate = plateNumber,
            nextBusEtaMinutes = etaMinutes + cycleMinutes
        )
    }

    /**
     * Preset database of real popular stations across major cities with authentic AMap bus lines.
     */
    fun getPreloadedStations(): List<BusStation> {
        return listOf(
            BusStation(
                id = "BJ_DESHENGMEN_01",
                name = "德胜门",
                city = "北京",
                adcode = "110102",
                location = "116.379124,39.948216",
                address = "北京市西城区德胜门东大街",
                lines = listOf(
                    createLine(
                        "BJ_L_919",
                        "919路",
                        "919",
                        "德胜门 ➔ 延庆南菜园总站",
                        "05:00",
                        "21:00",
                        "5元",
                        "八达岭高速快线",
                        "BJ_DESHENGMEN_01"
                    ),
                    createLine(
                        "BJ_L_5",
                        "5路",
                        "5",
                        "北土城公交场站 ➔ 菜户营桥",
                        "05:30",
                        "22:00",
                        "2元",
                        "贯穿中轴线",
                        "BJ_DESHENGMEN_01"
                    ),
                    createLine(
                        "BJ_L_27",
                        "27路",
                        "27",
                        "安定门外 ➔ 西直门",
                        "05:30",
                        "23:00",
                        "2元",
                        "核心干线",
                        "BJ_DESHENGMEN_01"
                    ),
                    createLine(
                        "BJ_L_345K",
                        "345路快",
                        "345快",
                        "德胜门西 ➔ 朝凤庵村",
                        "05:30",
                        "22:00",
                        "2元",
                        "昌平快速通勤线",
                        "BJ_DESHENGMEN_01"
                    ),
                    createLine(
                        "BJ_L_88",
                        "88路",
                        "88",
                        "草桥 ➔ 大钟寺",
                        "05:30",
                        "22:30",
                        "2元",
                        "常规干线",
                        "BJ_DESHENGMEN_01"
                    )
                )
            ),
            BusStation(
                id = "BJ_ZHONGGUANCUN_02",
                name = "中关村南",
                city = "北京",
                adcode = "110108",
                location = "116.316833,39.982821",
                address = "北京市海淀区中关村大街",
                lines = listOf(
                    createLine("BJ_L_302", "302路", "302", "辛庄 ➔ 巴沟村", "05:30", "22:00", "2元", "海淀核心干线", "BJ_ZHONGGUANCUN_02"),
                    createLine("BJ_L_320", "320路", "320", "北京西站 ➔ 西苑枢纽站", "05:00", "23:00", "2元", "高铁接驳专线", "BJ_ZHONGGUANCUN_02"),
                    createLine("BJ_L_332", "332路", "332", "前门 ➔ 颐和园", "05:00", "23:00", "2元", "高校文旅干线", "BJ_ZHONGGUANCUN_02"),
                    createLine("BJ_L_T8", "特8路内环", "特8内", "城铁大钟寺站 ➔ 城铁大钟寺站", "05:30", "22:00", "2元", "三环快速环线", "BJ_ZHONGGUANCUN_02")
                )
            ),
            BusStation(
                id = "BJ_TIANANMEN_03",
                name = "天安门东",
                city = "北京",
                adcode = "110101",
                location = "116.401124,39.907721",
                address = "北京市东城区东长安街",
                lines = listOf(
                    createLine("BJ_L_1", "1路", "1", "四惠枢纽站 ➔ 老山公交场站", "05:00", "23:00", "2元", "长安街大十路", "BJ_TIANANMEN_03"),
                    createLine("BJ_L_52", "52路", "52", "平乐园 ➔ 靛厂新村", "05:00", "23:00", "2元", "东西横贯线", "BJ_TIANANMEN_03"),
                    createLine("BJ_L_120", "120路", "120", "左家庄 ➔ 天坛南门", "05:30", "22:00", "2元", "核心观光线", "BJ_TIANANMEN_03")
                )
            ),
            BusStation(
                id = "SH_RENMIN_01",
                name = "人民广场(福州路)",
                city = "上海",
                adcode = "310101",
                location = "121.473701,31.230416",
                address = "上海市黄浦区福州路西藏中路",
                lines = listOf(
                    createLine("SH_L_49", "49路", "49", "汉口路江西中路 ➔ 上海体育馆", "05:30", "23:30", "2元", "模范标杆线", "SH_RENMIN_01"),
                    createLine("SH_L_167", "167路", "167", "伊敏河路巴林路 ➔ 上海植物园", "05:30", "22:30", "2元", "南北主干线", "SH_RENMIN_01"),
                    createLine("SH_L_934", "934路", "934", "普陀路陕西北路 ➔ 国顺东路翔殷路", "05:30", "22:00", "2元", "黄浦江两岸线", "SH_RENMIN_01"),
                    createLine("SH_L_451", "451路", "451", "南京西路西藏中路 ➔ 周东南路", "05:30", "23:00", "2元", "浦东穿梭干线", "SH_RENMIN_01")
                )
            ),
            BusStation(
                id = "SH_LUJIAZUI_02",
                name = "陆家嘴地铁站",
                city = "上海",
                adcode = "310115",
                location = "121.503612,31.238914",
                address = "上海市浦东新区陆家嘴环路世纪大道",
                lines = listOf(
                    createLine("SH_L_82", "82路", "82", "陆家嘴 ➔ 通耀路耀龙路", "05:30", "23:00", "2元", "滨江干线", "SH_LUJIAZUI_02"),
                    createLine("SH_L_961", "961路", "961", "陆家嘴 ➔ 申江路金海路", "05:45", "22:30", "2元", "金桥快线", "SH_LUJIAZUI_02"),
                    createLine("SH_L_JR1", "陆家嘴金融城1路", "金融城1", "陆家嘴 ➔ 东昌路渡口", "07:00", "21:30", "1元", "CBD环线微循环", "SH_LUJIAZUI_02")
                )
            ),
            BusStation(
                id = "GZ_TIYU_01",
                name = "体育中心(BRT)",
                city = "广州",
                adcode = "440106",
                location = "113.327812,23.134211",
                address = "广州市天河区天河路",
                lines = listOf(
                    createLine("GZ_L_B1", "BRT B1路", "B1", "体育中心 ➔ 夏茅客运站", "05:30", "22:30", "2元", "天河BRT主线", "GZ_TIYU_01"),
                    createLine("GZ_L_B2", "BRT B2路", "B2", "广州火车站 ➔ 东圃客运站", "05:30", "23:00", "2元", "快速通道", "GZ_TIYU_01"),
                    createLine("GZ_L_245", "245路", "245", "员村总站 ➔ 动物园南门", "06:00", "22:00", "2元", "核心市区线", "GZ_TIYU_01")
                )
            ),
            BusStation(
                id = "SZ_KEJI_01",
                name = "高新科技园",
                city = "深圳",
                adcode = "440305",
                location = "113.953812,22.540112",
                address = "深圳市南山区深南大道科技南一路",
                lines = listOf(
                    createLine("SZ_L_19", "19路", "19", "桃源村总站 ➔ 南头火车西站", "06:30", "22:30", "2元", "南山通勤线", "SZ_KEJI_01"),
                    createLine("SZ_L_M299", "M299路", "M299", "深圳北站 ➔ 南山中心区", "06:00", "22:00", "2.5元", "高铁跨区特快", "SZ_KEJI_01"),
                    createLine("SZ_L_113", "113路", "113", "蛇口港 ➔ 莲塘梧桐苑", "06:10", "23:00", "2.5元", "深南干线", "SZ_KEJI_01")
                )
            ),
            BusStation(
                id = "HZ_XIHU_01",
                name = "西湖断桥",
                city = "杭州",
                adcode = "330102",
                location = "120.155812,30.258912",
                address = "杭州市西湖区北山路",
                lines = listOf(
                    createLine("HZ_L_7", "7路", "7", "城站火车站 ➔ 灵隐", "05:30", "21:30", "2元", "百年经典景区线", "HZ_XIHU_01"),
                    createLine("HZ_L_27", "27路", "27", "岳坟 ➔ 龙井茶室", "06:00", "19:00", "2元", "茶山观光线", "HZ_XIHU_01"),
                    createLine("HZ_L_51", "51路(西湖内环线)", "51内", "吴山广场 ➔ 吴山广场", "08:00", "18:30", "3元", "西湖环湖专线", "HZ_XIHU_01")
                )
            )
        )
    }

    private fun createLine(
        id: String,
        name: String,
        shortName: String,
        direction: String,
        startTime: String,
        endTime: String,
        price: String,
        type: String,
        stationId: String
    ): BusLineInfo {
        return BusLineInfo(
            lineId = id,
            lineName = name,
            lineShortName = shortName,
            direction = direction,
            startStop = direction.split("➔").firstOrNull()?.trim() ?: "始发站",
            endStop = direction.split("➔").lastOrNull()?.trim() ?: "终点站",
            startTime = startTime,
            endTime = endTime,
            price = price,
            type = type,
            realtime = calculateRealtimeArrival(name, stationId)
        )
    }

    /**
     * Search preset database by city and keyword
     */
    fun searchPresetStations(query: String, city: String?): List<BusStation> {
        val all = getPreloadedStations()
        val filteredByCity = if (city.isNullOrBlank() || city == "全国" || city == "所有城市") {
            all
        } else {
            all.filter { it.city.contains(city) || city.contains(it.city) }
        }

        if (query.isBlank()) return filteredByCity

        return filteredByCity.filter { station ->
            station.name.contains(query, ignoreCase = true) ||
                    station.address.contains(query, ignoreCase = true) ||
                    station.lines.any { line ->
                        line.lineName.contains(query, ignoreCase = true) ||
                                line.lineShortName.contains(query, ignoreCase = true)
                    }
        }/*.ifEmpty {
            // Generate a synthetic match station if user searched for something specific not in preloaded
            listOf(
                BusStation(
                    id = "SEARCH_GEN_${abs(query.hashCode())}",
                    name = query.let { if (it.endsWith("站") || it.endsWith("路口") || it.endsWith("桥")) it else "${it}站" },
                    city = city?.ifBlank { "北京" } ?: "北京",
                    adcode = "110100",
                    location = "116.397428,39.90923",
                    address = "${city ?: "城市"}核心交通干道",
                    lines = listOf(
                        createLine("GEN_L_1", "快1路", "快1", "${query} ➔ 城市中心枢纽", "06:00", "22:30", "2元", "快速公交", "GEN_${query.hashCode()}"),
                        createLine("GEN_L_2", "101路", "101", "火车站 ➔ ${query}", "05:30", "23:00", "2元", "主干线", "GEN_${query.hashCode()}"),
                        createLine("GEN_L_3", "社区微巴8路", "微巴8", "${query} ➔ 地铁接驳站", "06:30", "21:00", "1元", "微循环线", "GEN_${query.hashCode()}")
                    )
                )
            )
        }*/
    }
}
