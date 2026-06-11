package com.example.data.model

object Translator {
    fun get(key: String, lang: String): String {
        return when (lang) {
            "zh" -> zhMap[key] ?: enMap[key] ?: ""
            "ja" -> jaMap[key] ?: enMap[key] ?: ""
            "ko" -> koMap[key] ?: enMap[key] ?: ""
            else -> enMap[key] ?: ""
        }
    }

    private val zhMap = mapOf(
        "title" to "智慧船班與即時預測",
        "subTitle" to "到離站提醒及動態發船管理系統",
        "weatherCardTitle" to "多元港口氣象資料整合",
        "windLabel" to "即時觀測風速",
        "waveLabel" to "即時觀測波高",
        "densityLabel" to "接駁港區搭乘人流",
        "emergencyTitle" to "突發狀況應變安全管控",
        "routeHeaderTitle" to "動態船班預估時間 & 提醒設置",
        "alertHeaderTitle" to "動態通知歷史紀錄",
        "aiCardTitle" to "Gemini AI 航行與安全智慧分析",
        "btnAskAI" to "詢問智慧中心 (AI 分析)",
        "activeEmergencyLabel" to "目前狀態",
        "clearLogBtn" to "清除紀錄",
        "sec" to "秒",
        "min" to "分鐘",
        "lowPassenger" to "低 (Low)",
        "mediumPassenger" to "中 (Medium)",
        "highPassenger" to "高 (High)",
        "seatingLoad" to "客艙乘載率",
        "predictiveInterval" to "預估間隔",
        "nextDeparture" to "預測發船倒數",
        "estArrival" to "預估到港倒數",
        "selectedRoute" to "分析路線：",
        "aiOverviewHeader" to "💡 AI 航運分析建議",
        "pushHistoryHeader" to "🔔 通知推送歷史",
        "noPushLogs" to "尚無任何推送或提醒紀錄。",
        "clear" to "清除",
        "simulateEmergency" to "注入突發事件以測試調度："
    )

    private val enMap = mapOf(
        "title" to "Smart Ferry Tracker",
        "subTitle" to "Dynamic Forecast & Disruption System",
        "weatherCardTitle" to "Multi-Source Harbor Weather Data",
        "windLabel" to "Real-time Wind Speed",
        "waveLabel" to "Real-time Wave Height",
        "densityLabel" to "Passenger Queue Density",
        "emergencyTitle" to "Emergency Disruption Security",
        "routeHeaderTitle" to "Dynamic Arrivals & Shift Predictor",
        "alertHeaderTitle" to "Live Reminders & Notifications Log",
        "aiCardTitle" to "Gemini AI Maritime Intel Report",
        "btnAskAI" to "Ask Maritime AI Assistant",
        "activeEmergencyLabel" to "Status",
        "clearLogBtn" to "Clear Logs",
        "sec" to "sec",
        "min" to "mins",
        "lowPassenger" to "Low",
        "mediumPassenger" to "Medium",
        "highPassenger" to "High",
        "seatingLoad" to "Seating Load",
        "predictiveInterval" to "Predictive Interval",
        "nextDeparture" to "Next Departure",
        "estArrival" to "Est. Arrival",
        "selectedRoute" to "Selected Route:",
        "aiOverviewHeader" to "💡 AI Dispatch Overview",
        "pushHistoryHeader" to "🔔 Notification Logs",
        "noPushLogs" to "No notification logs received.",
        "clear" to "Clear",
        "simulateEmergency" to "Simulate harbor emergencies:"
    )

    private val jaMap = mapOf(
        "title" to "スマートフェリー運行情報",
        "subTitle" to "到着・出発アラート＆動的運行管理",
        "weatherCardTitle" to "港湾気象データのマルチソース統合",
        "windLabel" to "リアルタイム風速",
        "waveLabel" to "リアルタイム波高",
        "densityLabel" to "乗客の混雑状況",
        "emergencyTitle" to "緊急事態への対応と安全管理",
        "routeHeaderTitle" to "ダイナミック運行予測＆アラート設定",
        "alertHeaderTitle" to "リアルタイム通知の履歴リスト",
        "aiCardTitle" to "Gemini AI 航行と安全の知能分析",
        "btnAskAI" to "スマートAIアシスタントに尋ねる",
        "activeEmergencyLabel" to "現在の状態",
        "clearLogBtn" to "ログを消去",
        "sec" to "秒",
        "min" to "分",
        "lowPassenger" to "低 (Low)",
        "mediumPassenger" to "中 (Medium)",
        "highPassenger" to "高 (High)",
        "seatingLoad" to "客船乗船率",
        "predictiveInterval" to "予測運行間隔",
        "nextDeparture" to "予測発車カウントダウン",
        "estArrival" to "予測到着カウントダウン",
        "selectedRoute" to "分析ルート：",
        "aiOverviewHeader" to "💡 AI 航行分析アドバイス",
        "pushHistoryHeader" to "🔔 発信済みの通知履歴",
        "noPushLogs" to "通知履歴はありません。",
        "clear" to "クリア",
        "simulateEmergency" to "ダイヤテスト用の緊急事態シミュレーション："
    )

    private val koMap = mapOf(
        "title" to "스마트 페리 운항 정보",
        "subTitle" to "도착 및 출발 알림 & 동적 운항 관리",
        "weatherCardTitle" to "다중 소스 통합 항만 기상 데이터",
        "windLabel" to "실시간 관측 풍속",
        "waveLabel" to "실시간 관측 파고",
        "densityLabel" to "승객 대기 혼잡도",
        "emergencyTitle" to "돌발 상황 대처 및 안전 관리",
        "routeHeaderTitle" to "동적 페리 예측 시간 및 알림 설정",
        "alertHeaderTitle" to "실시간 알림 및 운항 기록 로그",
        "aiCardTitle" to "Gemini AI 운항 및 안전 지능 분석",
        "btnAskAI" to "스마트 AI 어시스턴트 분석 요청",
        "activeEmergencyLabel" to "현재 상태",
        "clearLogBtn" to "로그 지우기",
        "sec" to "초",
        "min" to "분",
        "lowPassenger" to "낮음 (Low)",
        "mediumPassenger" to "보통 (Medium)",
        "highPassenger" to "높음 (High)",
        "seatingLoad" to "객실 탑승률",
        "predictiveInterval" to "예상 간격",
        "nextDeparture" to "예측 출발 카운트다운",
        "estArrival" to "예측 도착 카운트다운",
        "selectedRoute" to "분석 경로:",
        "aiOverviewHeader" to "💡 AI 운항 분석 권장 사항",
        "pushHistoryHeader" to "🔔 발송된 알림 및 로그 기록",
        "noPushLogs" to "받은 알림 또는 로그 기록이 없습니다.",
        "clear" to "지우기",
        "simulateEmergency" to "돌발 상황 주입 테스트:"
    )
}

fun getLocalizedRouteName(route: FerryRouteEntity, lang: String): String {
    return when (lang) {
        "zh" -> route.routeNameZh
        "en" -> route.routeNameEn
        "ja" -> when (route.id) {
            1 -> "鼓山 ⇄ 旗津 (高雄)"
            2 -> "淡水 ⇄ 八里 (新北)"
            3 -> "東港 ⇄ 小琉球 (屏東)"
            4 -> "基隆 ⇄ 馬祖 (基馬客船)"
            else -> route.routeNameZh
        }
        "ko" -> when (route.id) {
            1 -> "구산 ⇄ 치진 (가오슝)"
            2 -> "단수이 ⇄ 빠리 (신베이)"
            3 -> "둥강 ⇄ 소류구 (핑둥)"
            4 -> "지룽 ⇄ 마주 (지마객선)"
            else -> route.routeNameZh
        }
        else -> route.routeNameEn
    }
}

fun getLocalizedDepartPort(route: FerryRouteEntity, lang: String): String {
    return when (lang) {
        "zh" -> route.departingPortZh
        "en" -> route.departingPortEn
        "ja" -> when (route.id) {
            1 -> "鼓山フェリー乗り場"
            2 -> "淡水老街フェリー乗り場"
            3 -> "東港フェリー乗り場"
            4 -> "基隆西二フェリー乗り場"
            else -> route.departingPortZh
        }
        "ko" -> when (route.id) {
            1 -> "구산 페리 선착장"
            2 -> "단수이 라오지에 선착장"
            3 -> "둥강 선착장"
            4 -> "지룽항 서이 선착장"
            else -> route.departingPortZh
        }
        else -> route.departingPortEn
    }
}

fun getLocalizedArrivePort(route: FerryRouteEntity, lang: String): String {
    return when (lang) {
        "zh" -> route.arrivingPortZh
        "en" -> route.arrivingPortEn
        "ja" -> when (route.id) {
            1 -> "旗津フェリー乗り場"
            2 -> "八里フェリー乗り場"
            3 -> "小琉球白沙尾フェリー乗り場"
            4 -> "馬祖福澳港乗り場"
            else -> route.arrivingPortZh
        }
        "ko" -> when (route.id) {
            1 -> "치진 페리 선착장"
            2 -> "빠리 선착장"
            3 -> "소류구 바이샤웨이 선착장"
            4 -> "마주 푸아오 선착장"
            else -> route.arrivingPortZh
        }
        else -> route.arrivingPortEn
    }
}

fun getLocalizedAlertTitle(alert: AlertHistoryEntity, lang: String): String {
    return when (lang) {
        "zh" -> alert.titleZh
        "en" -> alert.titleEn
        "ja" -> {
            when {
                alert.titleZh.contains("提醒設置更新") || alert.titleEn.contains("Alert Toggled") -> "アラート設定更新"
                alert.titleZh.contains("離站") || alert.titleEn.contains("Departure") -> "🚢 船便出発のお知らせ"
                alert.titleZh.contains("到站") || alert.titleEn.contains("Arriving") -> "⚓ まもなく到着します"
                alert.titleZh.contains("強風") || alert.titleEn.contains("Gust") -> "🚨 突風警報"
                alert.titleZh.contains("維修") || alert.titleEn.contains("Repair") -> "⚙️ 船舶点検通知"
                alert.titleZh.contains("堵塞") || alert.titleEn.contains("Congestion") -> "⚓ 航路混雑アラート"
                alert.titleZh.contains("暴雨") || alert.titleEn.contains("Squall") -> "🌧️ 局地的な豪雨通知"
                else -> alert.titleZh
            }
        }
        "ko" -> {
            when {
                alert.titleZh.contains("提醒設置更新") || alert.titleEn.contains("Alert Toggled") -> "알림 설정 업데이트"
                alert.titleZh.contains("離站") || alert.titleEn.contains("Departure") -> "🚢 선박 출발 안내"
                alert.titleZh.contains("到站") || alert.titleEn.contains("Arriving") -> "⚓ 선박 곧 도착"
                alert.titleZh.contains("強風") || alert.titleEn.contains("Gust") -> "🚨 갑작스러운 강풍 경보"
                alert.titleZh.contains("維修") || alert.titleEn.contains("Repair") -> "⚙️ 선박 점검 안내"
                alert.titleZh.contains("堵塞") || alert.titleEn.contains("Congestion") -> "⚓ 항만 정체 경보"
                alert.titleZh.contains("暴雨") || alert.titleEn.contains("Squall") -> "🌧️ 국지적 집중호우 안내"
                else -> alert.titleZh
            }
        }
        else -> alert.titleEn
    }
}

fun getLocalizedAlertMessage(alert: AlertHistoryEntity, lang: String, routesList: List<FerryRouteEntity> = emptyList()): String {
    return when (lang) {
        "zh" -> alert.messageZh
        "en" -> alert.messageEn
        "ja" -> {
            // Find appropriate route based on name matching
            val route = routesList.find { alert.messageZh.contains(it.routeNameZh) || alert.messageEn.contains(it.routeNameEn) }
            val routeName = if (route != null) getLocalizedRouteName(route, "ja") else "フェリー船便"
            val arrivePort = if (route != null) getLocalizedArrivePort(route, "ja") else "到着港"
            
            when {
                alert.messageZh.contains("已開啟") || alert.messageEn.contains("turned ON") -> {
                    "「$routeName」の出発通知がオンになりました"
                }
                alert.messageZh.contains("已取消") || alert.messageEn.contains("turned OFF") -> {
                    "「$routeName」の出発通知がオフになりました"
                }
                alert.messageZh.contains("已安全離港") || alert.messageEn.contains("has departed") -> {
                    // Extract mins if possible
                    val mRegex = "\\d+".toRegex()
                    val match = mRegex.find(alert.messageZh)
                    val mins = match?.value ?: "15"
                    "【$routeName】は無事に出港しました！次の運行は $mins 分後を予定しています。"
                }
                alert.messageZh.contains("正減速駛入") || alert.messageEn.contains("is decelerating") -> {
                    "お気に入りの【$routeName】が【$arrivePort】に減速しながら進入しています。乗船エリアへお進みください。"
                }
                alert.messageZh.contains("觀測到強烈陣風") || alert.messageEn.contains("Strong visual gust") -> {
                    "港口で強い突風が観測されました。安全のため、一部のルートの運航間隔が自動的に延長されます。"
                }
                alert.messageZh.contains("雙體客輪突發機件檢修") || alert.messageEn.contains("Unexpected machinery") -> {
                    "主力双胴船の突発的な機関点検のため、予備船の稼働を開始しました。運行頻度が下がります。"
                }
                alert.messageZh.contains("航道有其他大型") || alert.messageEn.contains("cargo transits") -> {
                    "航路内に大型貨物船などの低速走行車両が多いため、制限速度を守り巡航します。5〜10分の遅延が見込まれます。"
                }
                alert.messageZh.contains("暴雨导致海上能見度") || alert.messageEn.contains("storm cells lower") -> {
                    "突然の豪雨により海上視界が不良となっています。レーダー引航を有効にし安全な速度に落として航行します。"
                }
                else -> alert.messageZh
            }
        }
        "ko" -> {
            val route = routesList.find { alert.messageZh.contains(it.routeNameZh) || alert.messageEn.contains(it.routeNameEn) }
            val routeName = if (route != null) getLocalizedRouteName(route, "ko") else "페리 노선"
            val arrivePort = if (route != null) getLocalizedArrivePort(route, "ko") else "도착지"

            when {
                alert.messageZh.contains("已開啟") || alert.messageEn.contains("turned ON") -> {
                    "「$routeName」의 동적出発 알림이 켜졌습니다"
                }
                alert.messageZh.contains("已取消") || alert.messageEn.contains("turned OFF") -> {
                    "「$routeName」의 동적 출발 알림이 꺼졌습니다"
                }
                alert.messageZh.contains("已安全離港") || alert.messageEn.contains("has departed") -> {
                    val mRegex = "\\d+".toRegex()
                    val match = mRegex.find(alert.messageZh)
                    val mins = match?.value ?: "15"
                    "【$routeName】이(가) 안전하게 출항하였습니다! 다음 선박은 $mins 분 후에 출발할 예정입니다."
                }
                alert.messageZh.contains("正減速駛入") || alert.messageEn.contains("is decelerating") -> {
                    "관심 등록하신 【$routeName】이(가) 【$arrivePort】(으)로 감속 진입 중입니다. 승선 구역에서 대기해 주세요."
                }
                alert.messageZh.contains("觀測到強烈陣風") || alert.messageEn.contains("Strong visual gust") -> {
                    "선착장에 강한 돌풍이 관측되었습니다. 일부 경로의 운航 간격이 안전을 위해 자동으로 연장됩니다."
                }
                alert.messageZh.contains("雙體客輪突發機件檢修") || alert.messageEn.contains("Unexpected machinery") -> {
                    "주력 쌍동선 긴급 엔진 점검으로 대체 선박을 가동합니다. 이에 따라 운航 횟수가 줄어들 예정입니다."
                }
                alert.messageZh.contains("航道有其他大型") || alert.messageEn.contains("cargo transits") -> {
                    "항로 내 대형 화물선 밀집으로 감속 운航 중입니다. 약 5-10분 정도 연착될 수 있습니다."
                }
                alert.messageZh.contains("暴雨导致海上能見度") || alert.messageEn.contains("storm cells lower") -> {
                    "갑작스러운 폭우로 시야가 제한되어, 레이더 유도 장비를 가동하고 정속 이하로 서행 운航 중입니다."
                }
                else -> alert.messageZh
            }
        }
        else -> alert.messageEn
    }
}
