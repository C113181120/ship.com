package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AlertHistoryEntity
import com.example.data.model.FerryRouteEntity
import com.example.data.model.PreferenceEntity
import com.example.data.model.Translator
import com.example.data.model.getLocalizedRouteName
import com.example.data.model.getLocalizedDepartPort
import com.example.data.model.getLocalizedArrivePort
import com.example.data.model.getLocalizedAlertTitle
import com.example.data.model.getLocalizedAlertMessage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CoralRed
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.WaveMint
import com.example.ui.viewmodel.FerryViewModel
import com.example.ui.viewmodel.FerryViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FerryDashboardScreen()
                }
            }
        }
    }
}

@Composable
fun FerryDashboardScreen() {
    val context = LocalContext.current
    val viewModel: FerryViewModel = viewModel(factory = FerryViewModelFactory(context))

    val pref by viewModel.preferences.collectAsStateWithLifecycle()
    val routes by viewModel.routeList.collectAsStateWithLifecycle()
    val alertLogs by viewModel.alertLogs.collectAsStateWithLifecycle()
    val isLoadingAI by viewModel.isLoadingAI.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiReportResult.collectAsStateWithLifecycle()
    val selectedAIId by viewModel.selectedRouteIdForAI.collectAsStateWithLifecycle()

    val lang = pref.language

    // String Resources Dictionary Mapping
    val title = Translator.get("title", lang)
    val subTitle = Translator.get("subTitle", lang)
    val weatherCardTitle = Translator.get("weatherCardTitle", lang)
    val windLabel = Translator.get("windLabel", lang)
    val waveLabel = Translator.get("waveLabel", lang)
    val densityLabel = Translator.get("densityLabel", lang)
    val emergencyTitle = Translator.get("emergencyTitle", lang)
    val routeHeaderTitle = Translator.get("routeHeaderTitle", lang)
    val aiCardTitle = Translator.get("aiCardTitle", lang)
    val btnAskAI = Translator.get("btnAskAI", lang)
    val activeEmergencyLabel = Translator.get("activeEmergencyLabel", lang)

    var expandedNotifications by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Elegant simplified dynamic notifications bar at bottom, expand on tap
            NotificationBottomBar(
                alertLogs = alertLogs,
                routes = routes,
                language = lang,
                expanded = expandedNotifications,
                onToggle = { expandedNotifications = !expandedNotifications },
                onClear = { viewModel.clearAllAlerts() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (MaterialTheme.colorScheme.primary == Color(0xFF006684)) {
                            listOf(Color(0xFFEAF5FA), Color(0xFFF4F9FC))
                        } else {
                            listOf(Color(0xFF081216), Color(0xFF0E1A20))
                        }
                    )
                )
                .padding(innerPadding)
        ) {
            // MAIN SCROLLABLE DASHBOARD CONTENT
            LazyColumn(
                modifier = Modifier
                    .weight(1.dp.value) // matches remaining space above bottom notifications bar
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: HEADER CARD WITH LANGUAGE FLIPPER
                item {
                    DashboardHeaderCard(
                        title = title,
                        subTitle = subTitle,
                        language = lang,
                        onLanguageToggle = { viewModel.toggleLanguage() }
                    )
                }

                // SECTION 2: WEATHER INTEGRATION SLIDERS (多元資料整合)
                item {
                    HarborWeatherPanel(
                        cardTitle = weatherCardTitle,
                        windLabel = windLabel,
                        waveLabel = waveLabel,
                        densityLabel = densityLabel,
                        pref = pref,
                        language = lang,
                        onPrefChanged = { wind, wave, density ->
                            viewModel.onUpdateEnvironment(wind, wave, density)
                        }
                    )
                }

                // SECTION 3: EMERGENCY DISRUPTION PANEL (處理突發狀況，及時提醒)
                item {
                    EmergencyInjectionPanel(
                        title = emergencyTitle,
                        activeLabel = activeEmergencyLabel,
                        currentDisruption = pref.activeEmergencyId,
                        language = lang,
                        onDisruptionSelected = { eId ->
                            viewModel.onUpdateEnvironment(
                                windSpeed = pref.windSpeedKnots,
                                waveHeight = pref.waveHeightMeters,
                                density = pref.passengerDensity,
                                emergencyId = eId
                            )
                        }
                    )
                }

                // SECTION 4: DETAILED DYNAMIC SCHEDULER & DEPARTURES (船班時間間隔預測、到離站提醒)
                item {
                    Text(
                        text = routeHeaderTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                items(routes, key = { it.id }) { route ->
                    FerryRouteCard(
                        route = route,
                        language = lang,
                        isSelectedForAI = selectedAIId == route.id,
                        onAlertToggled = { enabled ->
                            viewModel.toggleRouteReminder(route.id, enabled)
                        },
                        onSelectForAI = {
                            viewModel.setSelectedRouteForAI(route.id)
                        }
                    )
                }

                // SECTION 5: GEMINI AI SMART ADVISORY (多元資料整合 + AI 決策)
                item {
                    val selectedRoute = routes.find { it.id == selectedAIId } ?: routes.firstOrNull()
                    GeminiAIPanel(
                        cardTitle = aiCardTitle,
                        btnLabel = btnAskAI,
                        selectedRouteName = if (selectedRoute != null) getLocalizedRouteName(selectedRoute, lang) else "",
                        isLoading = isLoadingAI,
                        reportResult = aiResult,
                        language = lang,
                        onRequestReport = { viewModel.requestAISmartReport() },
                        onDismissReport = { viewModel.dismissAIReport() }
                    )
                }
            }
        }
    }
}

// ==========================================
// SUB-COMPOSABLES & COMPONENT WIDGETS
// ==========================================

@Composable
fun DashboardHeaderCard(
    title: String,
    subTitle: String,
    language: String,
    onLanguageToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Language custom toggle Target with large clickable surface (>=48dp)
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onLanguageToggle)
                    .testTag("language_toggle"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val langBtnLabel = when (language) {
                        "zh" -> "繁"
                        "en" -> "En"
                        "ja" -> "日"
                        "ko" -> "韓"
                        else -> "En"
                    }
                    Text(
                        text = langBtnLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun HarborWeatherPanel(
    cardTitle: String,
    windLabel: String,
    waveLabel: String,
    densityLabel: String,
    pref: PreferenceEntity,
    language: String,
    onPrefChanged: (Float, Float, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Weather Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = cardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // WIND SPEED SLIDER
            val windUnit = when (language) {
                "zh" -> "節"
                "ja" -> "ノット"
                "ko" -> "노트"
                else -> "Knots"
            }
            Text(
                text = "$windLabel: ${String.format("%.1f", pref.windSpeedKnots)} $windUnit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = pref.windSpeedKnots,
                onValueChange = { onPrefChanged(it, pref.waveHeightMeters, pref.passengerDensity) },
                valueRange = 0f..25f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wind_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            )

            // WAVE HEIGHT SLIDER
            Spacer(modifier = Modifier.height(8.dp))
            val waveUnit = when (language) {
                "zh" -> "公尺"
                "ja" -> "メートル"
                "ko" -> "미터"
                else -> "Meters"
            }
            Text(
                text = "$waveLabel: ${String.format("%.1f", pref.waveHeightMeters)} $waveUnit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = pref.waveHeightMeters,
                onValueChange = { onPrefChanged(pref.windSpeedKnots, it, pref.passengerDensity) },
                valueRange = 0f..2.5f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wave_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            )

            // PASSENGER DENSITY OPTION GROUP
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = densityLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val densities = listOf("low", "medium", "high")
                densities.forEach { d ->
                    val selected = pref.passengerDensity == d
                    val label = when (d) {
                        "low" -> Translator.get("lowPassenger", language)
                        "medium" -> Translator.get("mediumPassenger", language)
                        else -> Translator.get("highPassenger", language)
                    }
                    Button(
                        onClick = { onPrefChanged(pref.windSpeedKnots, pref.waveHeightMeters, d) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("density_btn_$d"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyInjectionPanel(
    title: String,
    activeLabel: String,
    currentDisruption: String,
    language: String,
    onDisruptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Safety and disruption controls",
                    tint = if (currentDisruption == "none") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Current Emergency State Badge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (currentDisruption == "none") {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (currentDisruption) {
                        "none" -> when (language) {
                            "zh" -> "🟢 全港通常發船，適航程度：高"
                            "ja" -> "🟢 全ての埠頭は通常通り運行中。運航ステータス：最適"
                            "ko" -> "🟢 모든 선착장 정상 운항. 운항 상태: 최적"
                            else -> "🟢 All Piers Clear. Nav State: Optimal"
                        }
                        "gust" -> when (language) {
                            "zh" -> "🔴 限制發船：突發強烈風陣"
                            "ja" -> "🔴 運行制限中：突発的な強風による風防制御作動"
                            "ko" -> "🔴 운항 제한: 돌풍 발생으로 인한 안전 운항조치 가동"
                            else -> "🔴 Shift Blocked: Heavy Gust Inflows"
                        }
                        "engine" -> when (language) {
                            "zh" -> "🟡 降頻航次：主體輪船機件檢修"
                            "ja" -> "🟡 減便運行中：主力客船の緊急定期メンテナンス"
                            "ko" -> "🟡 감편 운항: 핵심 선박의 긴급 일시 점검"
                            else -> "🟡 Limited Flow: Catamaran Standby Maintenance"
                        }
                        "congestion" -> when (language) {
                            "zh" -> "🟡 減速避讓：航道大型散裝貨客輪壅塞"
                            "ja" -> "🟡 減速回避運転：航路上の大型貨物船との距離維持"
                            "ko" -> "🟡 감속 양보 운항: 항로 내 대형 화물선 밀집 우려"
                            else -> "🟡 Slow Transit: Waterway Container Congestion"
                        }
                        "rain" -> when (language) {
                            "zh" -> "🟡 雷達引航：局部雷雨對流低能見度"
                            "ja" -> "🟡 レーダー航行：局地的な雷雨による視界不良"
                            "ko" -> "🟡 레이더 유도 항행: 국지적 뇌우로 인한 시야 제한"
                            else -> "🟡 Radar Cruising: Low Visibility Cold Front"
                        }
                        else -> ""
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentDisruption == "none") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Selection Row for Emergencies
            Text(
                text = Translator.get("simulateEmergency", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val disruptions = listOf("none", "gust", "engine", "congestion", "rain")
                disruptions.forEach { e ->
                    val isSelected = currentDisruption == e
                    val label = when (e) {
                        "none" -> when (language) {
                            "zh" -> "正常"
                            "ja" -> "正常"
                            "ko" -> "정상"
                            else -> "Clear"
                        }
                        "gust" -> when (language) {
                            "zh" -> "強風"
                            "ja" -> "強風"
                            "ko" -> "강풍"
                            else -> "Gust"
                        }
                        "engine" -> when (language) {
                            "zh" -> "檢修"
                            "ja" -> "点検"
                            "ko" -> "점검"
                            else -> "Repair"
                        }
                        "congestion" -> when (language) {
                            "zh" -> "塞船"
                            "ja" -> "混雑"
                            "ko" -> "정체"
                            else -> "Traffic"
                        }
                        else -> when (language) {
                            "zh" -> "暴雨"
                            "ja" -> "豪雨"
                            "ko" -> "폭우"
                            else -> "Squall"
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDisruptionSelected(e) }
                            .testTag("emergency_btn_$e"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (e == "none") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FerryRouteCard(
    route: FerryRouteEntity,
    language: String,
    isSelectedForAI: Boolean,
    onAlertToggled: (Boolean) -> Unit,
    onSelectForAI: () -> Unit
) {
    val routeName = getLocalizedRouteName(route, language)
    val departingDock = getLocalizedDepartPort(route, language)
    val arrivingDock = getLocalizedArrivePort(route, language)

    // Formatter helpers
    val mDep = route.nextDepartureSeconds / 60
    val sDep = route.nextDepartureSeconds % 60
    val depStr = String.format("%02d:%02d", mDep, sDep)

    val mArr = route.nextArrivalSeconds / 60
    val sArr = route.nextArrivalSeconds % 60
    val arrStr = String.format("%02d:%02d", mArr, sArr)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onSelectForAI)
            .testTag("route_card_${route.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForAI) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // First Row: Route Title & Quick Bell target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⚓ $departingDock ⇄ $arrivingDock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // BELL TOGGLE WITH SECURE ACCESSIBILITY PADDING (>=48dp)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onAlertToggled(!route.isAlertSet) }
                        .testTag("reminder_bell_${route.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (route.isAlertSet) Icons.Default.Notifications else Icons.Default.Refresh,
                        contentDescription = "Notification Alarm Toggle",
                        tint = if (route.isAlertSet) MaterialTheme.colorScheme.primary else Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)

            // Second Row: Next Depart & Next Arrival Countdown Tickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Departure Box
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (route.nextDepartureSeconds <= 180) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Translator.get("nextDeparture", language),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = depStr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (route.nextDepartureSeconds <= 180) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                // Arrival Box
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Translator.get("estArrival", language),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = arrStr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Passenger occupancy visual progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Translator.get("seatingLoad", language)}: ${route.passengerFlowPct}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${Translator.get("predictiveInterval", language)}: ${route.currentIntervalMinutes} ${Translator.get("min", language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = route.passengerFlowPct / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    route.passengerFlowPct > 75 -> CoralRed
                    route.passengerFlowPct > 50 -> SafetyOrange
                    else -> WaveMint
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun GeminiAIPanel(
    cardTitle: String,
    btnLabel: String,
    selectedRouteName: String,
    isLoading: Boolean,
    reportResult: String?,
    language: String,
    onRequestReport: () -> Unit,
    onDismissReport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_panel"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI Star Icon",
                    tint = WaveMint
                )
                Text(
                    text = cardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val selectedRouteLabel = when (language) {
                "zh" -> "分析路線："
                "ja" -> "分析ルート："
                "ko" -> "분석 경로:"
                else -> "Selected Route:"
            }
            Text(
                text = "$selectedRouteLabel $selectedRouteName",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // AI Trigger Button (minimum 48dp height)
            Button(
                onClick = onRequestReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("request_ai_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run Icon")
                        Text(text = btnLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expandable report section
            AnimatedVisibility(
                visible = reportResult != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                reportResult?.let { text ->
                    Surface(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Translator.get("aiOverviewHeader", language),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onDismissReport)
                                        .testTag("dismiss_ai"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss Report",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationBottomBar(
    alertLogs: List<AlertHistoryEntity>,
    routes: List<FerryRouteEntity>,
    language: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit
) {
    val bottomBg = MaterialTheme.colorScheme.surface
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp)
            .navigationBarsPadding(),
        color = bottomBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // TAP PREVIEW BANNER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .heightIn(min = 40.dp)
                    .testTag("notifications_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val alertCount = alertLogs.size
                val label = when (language) {
                    "zh" -> "🔔 通知推送歷史 (${alertCount} 筆)"
                    "ja" -> "🔔 発信済みの通知履歴 (${alertCount} 件)"
                    "ko" -> "🔔 발송된 알림 기록 (${alertCount} 개)"
                    else -> "🔔 Notification Logs (${alertCount})"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Push History Drawer",
                        tint = if (alertCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (expanded && alertCount > 0) {
                        Text(
                            text = Translator.get("clear", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onClear() }
                                .testTag("clear_logs_btn")
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.Menu else Icons.Default.Menu,
                        contentDescription = "Expand/Collapse Indicator",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // EXPANDED SCROLL LOG
            if (expanded) {
                if (alertLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translator.get("noPushLogs", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(alertLogs) { alert ->
                            val titleText = getLocalizedAlertTitle(alert, language)
                            val msgText = getLocalizedAlertMessage(alert, language, routes)
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (alert.isEmergency) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = titleText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (alert.isEmergency) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msgText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
