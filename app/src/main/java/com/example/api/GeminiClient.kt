package com.example.api

import com.example.BuildConfig
import com.example.data.model.getLocalizedRouteName
import com.example.data.model.FerryRouteEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Call Gemini 3.5 Flash to generate weather analysis and safety prediction report
     */
    suspend fun getFerryAIPrediction(
        lang: String,
        routeZh: String,
        routeEn: String,
        windKnots: Float,
        waveMeters: Float,
        density: String,
        emergencyId: String
    ): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            // Graceful Local Fallback when API key is missing or is the placeholder
            return generateLocalDynamicPrediction(lang, routeZh, routeEn, windKnots, waveMeters, density, emergencyId)
        }

        val detectedId = if (routeEn.contains("Gushan", ignoreCase = true)) 1
                         else if (routeEn.contains("Tamsui", ignoreCase = true)) 2
                         else if (routeEn.contains("Donggang", ignoreCase = true)) 3
                         else 4

        val dummyRoute = FerryRouteEntity(
            id = detectedId,
            routeNameZh = routeZh,
            routeNameEn = routeEn,
            departingPortZh = "",
            departingPortEn = "",
            arrivingPortZh = "",
            arrivingPortEn = "",
            baseIntervalMinutes = 10,
            currentIntervalMinutes = 10,
            nextDepartureSeconds = 0,
            nextArrivalSeconds = 0,
            passengerFlowPct = 0
        )

        val routeNameLocal = getLocalizedRouteName(dummyRoute, lang)

        val prompt = when (lang) {
            "zh" -> {
                """
                請擔任專業的航海調度工程師與氣象預測助手。
                分析以下船班路線的即時狀態：
                - 路線：$routeNameLocal
                - 風速：$windKnots 節 (Knots)
                - 浪高：$waveMeters 公尺 (Meters)
                - 乘客人潮：$density
                - 突發狀況/緊急事件種類：$emergencyId (其中 none 表示正常)
                
                請提供一份簡明扼要(不超過150字)的「智慧船班分析及預估報告」：
                1. 給予一項目前的適航評等 (安全、警惕、危險)。
                2. 計算該路線在目前氣候下合適的發船「預估間隔時間」(通常基礎發船間隔為 10-30 分鐘，如果浪高>1.5m或風速>15節或有突發事件，應大幅增加間隔)。
                3. 提供搭乘建議與突發狀況處理措施。
                繁體中文呈現，不要有過多囉唆的敬語。
                """.trimIndent()
            }
            "ja" -> {
                """
                専門の航海運行エンジニアおよび気象予測アシスタントとしてお答えください。
                以下のフェリー航路のリアルタイムステータスを分析してください：
                - ルート名：$routeNameLocal
                - 風速：$windKnots ノット (Knots)
                - 波高：$waveMeters メートル (Meters)
                - 乗客の混雑度：$density
                - 突発事態/緊急事態の種類：$emergencyId (none は正常を示します)
                
                150字以内で簡潔な「スマートフェリー運行分析および安全予測レポート」を提供してください：
                1. 現在の適航評価（安全、注意、危険）。
                2. 現在の気象下での適切な「出発予測間隔」（通常、基本間隔は10〜30分で、高波や強風、緊急事態がある場合は大幅に延長します）。
                3. 乗車候補生へのアドバイス。
                日本語で出力し、冗長な表現や敬語は避けてください。
                """.trimIndent()
            }
            "ko" -> {
                """
                전문 항해 운항 엔지니어 및 기상 예측 어시스턴트로 역할해 주십시오.
                다음 페리 노선의 실시간 상태를 분석하십시오:
                - 경로명: $routeNameLocal
                - 풍속: $windKnots 노트 (Knots)
                - 파고: $waveMeters 미터 (Meters)
                - 대기 탑승객 혼잡도: $density
                - 예상 긴급 상황 종류: $emergencyId (none은 정상을 뜻함)
                
                150자 이내로 간결한 '스마트 페리 운항 분석 및 안전 예측 보고서'를 제공하십시오:
                1. 현재 운항 적합성 정합 평가 (안전, 주의, 위험).
                2. 현재 날씨에 알맞은 '예측 출발 간격' (기준은 10~30분이며, 파도가 높거나 바람이 강하고 돌발 상황 시 크게 연장).
                3. 승객 탑승 권장 사항.
                한국어로만 직관적이고 군더더기 없이 출력하십시오.
                """.trimIndent()
            }
            else -> {
                """
                Act as a maritime harbor controller & weather predictor.
                Analyze the following ferry route status:
                - Route: $routeNameLocal
                - Wind Speed: $windKnots Knots
                - Wave Height: $waveMeters meters
                - Passenger Density: $density
                - Emergency Disruption Event: $emergencyId (none means normal status)
                
                Provide a concise dynamic forecasting report (max 150 words):
                1. Suggest a Marine Safety Rating (Safe, Caution, High Danger).
                2. Predict the interval adjustment (Base schedule is 10-30 mins, adjust outwards significantly if wind > 15kt, waves > 1.5m, or emergency occurs).
                3. Provide prompt advice for boarding passengers.
                Output in professional clear English. No redundant phrases.
                """.trimIndent()
            }
        }

        return try {
            val systemMsg = Content(
                parts = listOf(Part(text = "You are a specialized maritime weather intelligence and dock routing assistant."))
            )
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.5f, maxOutputTokens = 350),
                systemInstruction = systemMsg
            )

            val rawResponse = service.generateContent(apiKey, request)
            val textResult = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (textResult.isNullOrBlank()) {
                generateLocalDynamicPrediction(lang, routeZh, routeEn, windKnots, waveMeters, density, emergencyId)
            } else {
                textResult
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback for network issues
            generateLocalDynamicPrediction(lang, routeZh, routeEn, windKnots, waveMeters, density, emergencyId) + 
            when (lang) {
                "zh" -> "\n(附帶提醒：已啟用本地端動態規則引擎)"
                "ja" -> "\n(注：通信環境によりローカル推論エンジンで作成されました)"
                "ko" -> "\n(참고: 네트워크 접속 지연으로 로컬 예측 엔진으로 생성됨)"
                else -> "\n(Note: Generated via local backup engine due to connectivity)"
            }
        }
    }

    /**
     * High-fidelity dynamic analysis engine to back up Gemini should the key be empty or invalid.
     */
    fun generateLocalDynamicPrediction(
        lang: String,
        routeZh: String,
        routeEn: String,
        windKnots: Float,
        waveMeters: Float,
        density: String,
        emergencyId: String
    ): String {
        // Calculate dynamic values
        var baseInterval = if (routeEn.contains("Gushan")) 10 else if (routeEn.contains("Tamsui")) 15 else if (routeEn.contains("Donggang")) 30 else 240
        var intervalModifier = 0
        var safetyLevel = when (lang) {
            "zh" -> "🟢 安全通航"
            "ja" -> "🟢 安全運航 (低リスク)"
            "ko" -> "🟢 안전 운항 (낮은 위험)"
            else -> "🟢 Low Risk (Safe)"
        }
        var recommendation = ""
        
        // Weather impacts
        if (windKnots > 12f) {
            intervalModifier += 3
            safetyLevel = when (lang) {
                "zh" -> "🟡 謹慎慢行"
                "ja" -> "🟡 注意航行 (中等リスク)"
                "ko" -> "🟡 주의 서행 (보통 위험)"
                else -> "🟡 Moderate Risk (Caution)"
            }
        }
        if (waveMeters > 0.8f) {
            intervalModifier += 5
            safetyLevel = when (lang) {
                "zh" -> "🟡 浪高注意"
                "ja" -> "🟡 波高注意 (中等リスク)"
                "ko" -> "🟡 파고 주의 (보통 위험)"
                else -> "🟡 Rough Sea Alert (Caution)"
            }
        }
        if (windKnots >= 18f || waveMeters >= 1.5f) {
            intervalModifier += 15
            safetyLevel = when (lang) {
                "zh" -> "🔴 惡劣天候 (危險)"
                "ja" -> "🔴 悪天候警告 (高リスク・危険)"
                "ko" -> "🔴 악천후 경보 (높은 위험・위험)"
                else -> "🔴 High Risk (Severe)"
            }
        }

        // Emergency impacts
        when (emergencyId) {
            "gust" -> {
                intervalModifier += 8
                safetyLevel = when (lang) {
                    "zh" -> "🔴 突然強風 (警戒)"
                    "ja" -> "🔴 突風警告 (急警戒)"
                    "ko" -> "🔴 돌풍 경보 (경계)"
                    else -> "🔴 Storm Gust Alert (Danger)"
                }
                recommendation = when (lang) {
                    "zh" -> "港口測得瞬間強烈風陣，中小型船班暫緩離港，已啟動防風安全調度。"
                    "ja" -> "港湾内で瞬間突風を検出しました。中小型船は一時見合わせ、風害軽減ダイヤ稼働中。"
                    "ko" -> "선착장에서 순간 돌풍이 관측되었습니다. 중소형 선박 출항 일시 보류 및 적극 안전 운항 조치 가동."
                    else -> "Sudden strong gusts measured at pier. Small vessels are on temporary standby, wind mitigation is active."
                }
            }
            "engine" -> {
                intervalModifier += 12
                safetyLevel = when (lang) {
                    "zh" -> "🟡 船隻檢修"
                    "ja" -> "🟡 船舶点検 (注意)"
                    "ko" -> "🟡 선박 점검 (주의)"
                    else -> "🟡 Maintenance Standby (Caution)"
                }
                recommendation = when (lang) {
                    "zh" -> "主力航次進行緊急定期機件降溫檢修，目前由備用渡輪提供降頻服務。"
                    "ja" -> "主力船の機関冷却および緊急点検期間のため、予備船の臨時運航を実施中（減便対応）。"
                    "ko" -> "주력 선박 엔진 정기 냉각 및 일시 점검으로, 예비 선박을 통해 감편 운항을 지원 중입니다."
                    else -> "Vessel under cooling and mechanical check. Backup catamaran is active under lowered frequency."
                }
            }
            "congestion" -> {
                intervalModifier += 6
                safetyLevel = when (lang) {
                    "zh" -> "🟡 港口塞船"
                    "ja" -> "🟡 航路混雑 (注意)"
                    "ko" -> "🟡 입항 정체 (주의)"
                    else -> "🟡 Waterway Congestion (Caution)"
                }
                recommendation = when (lang) {
                    "zh" -> "前站有大台散裝貨輪進出，渡輪限速避讓，發船稍有滯留。"
                    "ja" -> "前方に大型バルク貨物船の入出港があるため、渡船は安全速度に規制。発着に少し遅れが生じています。"
                    "ko" -> "전방에 대형 화물선 교행으로 승선 선박이 감속 대피 중이며, 운항 출발이 지연될 수 있습니다."
                    else -> "Large bulk carrier crossing ahead. Speed safety restriction applied, expect minor harbor delay."
                }
            }
            "rain" -> {
                intervalModifier += 4
                safetyLevel = when (lang) {
                    "zh" -> "🟡 局部暴雨"
                    "ja" -> "🟡 局地的な豪雨 (視界低下)"
                    "ko" -> "🟡 국지적 호우 (시야 저하)"
                    else -> "🟡 Rain Squall (Reduced Visibility)"
                }
                recommendation = when (lang) {
                    "zh" -> "對流雨胞移入，能見度降低，全體船班啟動雷達防碰撞慢行機制。"
                    "ja" -> "対流性降雨セルの接近により視界減退。レーダー衝突回避自動基準を適用し全船徐行運行。"
                    "ko" -> "대류성 집중호우 구름 진입으로 시야가 감퇴되어, 전 선박 레이더 가동 및 서행 운항 중입니다."
                    else -> "Heavy rain squalls passing. Radar collision avoidance standards applied, ships traveling at slow speed."
                }
            }
            else -> {
                recommendation = when (lang) {
                    "zh" -> {
                        when (density) {
                            "high" -> "目前乘客客流較大，登船排隊時間約增長 5-10 分鐘，建議提早到達碼頭。"
                            "low" -> "候船區人流充裕，發船按標準時間，適航度高。"
                            else -> "天候清爽，水色與風向均符合航行標準，適合日常通勤或觀光搭乘。"
                        }
                    }
                    "ja" -> {
                        when (density) {
                            "high" -> "現在乗客が多数のため、乗船案内待ち時間が約5-10分長引きます。早めの到着をお勧めします。"
                            "low" -> "待合室の混雑はほとんどなく、定刻に出発します。快適な航行に適しています。"
                            else -> "快晴で波風ともに航行基準を満たしています。日常の移動や観光に最適な気象条件です。"
                        }
                    }
                    "ko" -> {
                        when (density) {
                            "high" -> "현재 승객 혼잡도가 높아 탑승 대기 시간이 약 5-10분 연기됩니다. 선착장에 미리가 계시길 권유합니다."
                            "low" -> "대기 정원이 여유로워 정시 출발하며, 바다 운항에 매우 적합한 상태입니다."
                            else -> "날씨가 맑고 조류 및 풍향이 운항 기준에 충분히 부합하며, 일상 이동 및 관광에 이상적입니다."
                        }
                    }
                    else -> {
                        when (density) {
                            "high" -> "High passenger volume. Expect 5-10 mins boarding queue delay. Please arrive early."
                            "low" -> "Low passenger density, normal boarding process. Ideal transit wave."
                            else -> "Pleasant conditions. Sea tide and visual navigation paths are fully clear. Safe for transit."
                        }
                    }
                }
            }
        }

        val finalInterval = baseInterval + intervalModifier

        return when (lang) {
            "zh" -> {
                """
                【智慧船班分析報告 (規則引擎)】
                ● 適航評級：$safetyLevel
                ● 推薦班次間隔：$finalInterval 分鐘 (基準: $baseInterval 分鐘)
                ● 即時乘載與海況：風力 ${windKnots}kt，浪高 ${waveMeters}m。
                ● 調度指南：$recommendation
                """.trimIndent()
            }
            "ja" -> {
                """
                【スマートフェリー分析報告（ローカルエンジン）】
                ● 安全評価：$safetyLevel
                ● 推奨運行間隔：$finalInterval 分（基準：$baseInterval 分）
                ● リアルタイム海況：風速 ${windKnots}kt、波高 ${waveMeters}m。
                ● 運行アドバイス：$recommendation
                """.trimIndent()
            }
            "ko" -> {
                """
                【스마트 페리 분석 보고서 (로컬 엔진)】
                ● 안전 등급: $safetyLevel
                ● 권장 운항 간격: $finalInterval 분 (기준: $baseInterval 분)
                ● 실시간 해상 정보: 풍속 ${windKnots}kt, 파고 ${waveMeters}m。
                ● 운항 안내 사항: $recommendation
                """.trimIndent()
            }
            else -> {
                """
                [Ferry Intel Analytics Report (Local Engine)]
                ● Safety Rating: $safetyLevel
                ● Dynamic Departure Interval: $finalInterval mins (Base: $baseInterval mins)
                ● Current Marine Values: Wind ${windKnots}kt, Wave ${waveMeters}m.
                ● Harbor Advisory: $recommendation
                """.trimIndent()
            }
        }
    }
}
