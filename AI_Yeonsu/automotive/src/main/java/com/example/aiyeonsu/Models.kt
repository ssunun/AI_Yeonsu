package com.example.aiyeonsu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Tab(val label: String) {
    PRE("출발 전"), DRIVE("주행 중"), REVIEW("주행 완료"), GROWTH("성장·멘토")
}

// 연곰 자세 (res/drawable-nodpi 의 PNG)
enum class Pose(val res: Int) {
    CLOSEUP(R.drawable.mascot_closeup),
    CHEER(R.drawable.mascot_cheer),
    CAUTION(R.drawable.mascot_caution),
    PRAISE(R.drawable.mascot_praise),
    GUIDE(R.drawable.mascot_guide),
    STANDING(R.drawable.mascot_standing)
}

class Stage(val no: Int, val title: String, val desc: String, val tip: String)

val STAGES = listOf(
    Stage(1, "주차장 직진·정지", "핸들과 브레이크 감각 익히기", "브레이크는 미리, 천천히 밟아요."),
    Stage(2, "일반도로 차선 유지", "속도를 맞추며 차선 가운데 지키기", "차선 가운데를 보고 앞차와 간격을 지켜요."),
    Stage(3, "교차로 좌·우회전", "유도선 따라 크게, 횡단보도 앞 정지", "우회전은 횡단보도 보행자부터 확인해요."),
    Stage(4, "차선 변경", "깜빡이, 사이드미러, 천천히 들어가기", "깜빡이를 켜고 셋을 센 뒤 들어가요."),
    Stage(5, "합류·끼어들기", "본선 속도에 맞춰 부드럽게 들어가기", "뒤차 간격을 보고 속도를 맞춰 들어가요."),
    Stage(6, "고속도로·야간", "긴 거리와 어두운 길에 적응하기", "멀리 보고, 앞차와 간격을 넉넉히 둬요.")
)

class Course(
    val name: String,
    val km: Float,
    val minutes: Int,
    val stages: String,
    val note: String,
    val sample: Boolean
)

val COURSES = listOf(
    Course("평택대학교 → 안성팜랜드", 7.8f, 14, "3 · 5단계", "우회전, 합류, 사고다발 구간을 차례로 연습해요", false),
    Course("캠퍼스 주변 한 바퀴", 3.0f, 8, "1 · 2단계", "한적한 길에서 직진·정지와 차선 유지부터", true),
    Course("읍내 도심 순환", 6.0f, 15, "3 · 4단계", "신호가 많은 길에서 좌·우회전과 차선 변경", true)
)

// 앱 전체에서 같이 쓰는 설정 (화면을 옮겨도 유지)
class Settings {
    var stage by mutableStateOf(4)            // 0 ~ 5  (기본: 5단계 합류·끼어들기)
    var course by mutableStateOf(0)
    var gapAlert by mutableStateOf(15f)       // 앞차 간격 알림 거리(m)
    var autoDefog by mutableStateOf(true)
    var defogMode by mutableStateOf(0)        // 0 외기모드  1 A/C 프론트
    var defogTemp by mutableStateOf(23f)
    var gasGuide by mutableStateOf(true)
    var accidentAuto by mutableStateOf(true)
    var narrate by mutableStateOf(false)      // 자율주행 해설 모드
    var voiceMode by mutableStateOf(0)        // 0 전체  1 위험만  2 음소거
    var myGapFront by mutableStateOf(0)       // 나만의 기준 간격 (0이면 아직 없음)
    var myGapRear by mutableStateOf(0)
    var mentorCoRide by mutableStateOf(0)     // 0 동승  1 원격
    var shareEvents by mutableStateOf(true)
    var shareScore by mutableStateOf(true)
    var shareRoute by mutableStateOf(false)
}

class RunResult(
    val minutes: Int,
    val km: Float,
    val alerts: Int,
    val maxSpeed: Int,
    val minGap: Int,
    val goodTurns: Int,
    val goodMerge: Int,
    val mergeFront: Int,
    val mergeRear: Int
) {
    // 시연용 점수 계산 (실제 센서 데이터가 아님)
    val brakeScore: Int get() = (100 - alerts * 6).coerceIn(60, 100)
    val steerScore: Int get() = (86 + goodTurns * 3).coerceIn(60, 100)
    val speedScore: Int get() = if (maxSpeed <= 60) 100 else (100 - (maxSpeed - 60) * 2).coerceIn(60, 100)
    val total: Int get() = (brakeScore + steerScore + speedScore) / 3
}

class GradMission(val title: String, val sub: String, val done: Boolean)

val MISSIONS = listOf(
    GradMission("첫 안전 주행 완료", "안전의 첫걸음", true),
    GradMission("규정 속도 지키기", "전 구간 속도 준수", true),
    GradMission("부드러운 차선 변경", "주행 안정성 90점 이상", true),
    GradMission("고속도로 합류 도전", "본선 흐름에 맞춰 합류하기", false),
    GradMission("혼자서 주차 성공", "출발부터 주차까지 스스로", false)
)

class History(val no: Int, val date: String, val goal: String, val score: Int, val line: String)

val HISTORY = listOf(
    History(7, "11/09", "합류·끼어들기", 92, "24분 · 12.8km · 급제동 1회"),
    History(6, "11/06", "차선 변경", 88, "19분 · 9.4km · 부드러운 조향 94점"),
    History(5, "11/03", "교차로 좌·우회전", 84, "22분 · 10.2km · 우회전 일시정지 완벽"),
    History(4, "10/30", "합류·끼어들기", 76, "18분 · 8.5km · 급제동 2회")
)

// 주행 중 AI에게 물어보기 (시연용 질문·답변)
class QA(val q: String, val a: String)

val QAS = listOf(
    QA("앞에 도로공사 중이야. 어떻게 해?", "공사 구간이에요. 속도를 줄이고 안내 요원 신호를 따라가요. 차선이 줄면 일찍 깜빡이를 켜요."),
    QA("갑자기 앞 차선이 막혔어.", "침착하게 속도를 줄이고 비상등을 켜요. 뒤차를 확인한 뒤 천천히 옆 차선으로 옮겨요."),
    QA("비가 와서 앞이 잘 안 보여.", "와이퍼를 켜고 앞차와 간격을 두 배로 벌려요. 속도도 평소보다 줄여요.")
)

val GAS_STEPS = listOf(
    "시동을 끄고 기어를 P에 둬요",
    "주유구 버튼을 눌러 덮개를 열어요",
    "내 차 연료(휘발유·경유)를 확인해요",
    "노즐을 끝까지 넣고 레버를 당겨요",
    "자동으로 멈추면 노즐을 빼고 덮개를 닫아요",
    "결제하고 안전하게 출발해요"
)

val ACCIDENT_STEPS = listOf(
    "비상등을 켜고 안전한 곳에 차를 세워요",
    "다친 사람이 있으면 먼저 119에 신고해요",
    "사고 장면과 차량 번호를 사진으로 남겨요",
    "보험사에 사고 접수를 해요",
    "상대 차량 정보를 주고받고, 현장을 정리해요"
)
