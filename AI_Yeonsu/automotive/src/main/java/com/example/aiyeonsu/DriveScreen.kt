package com.example.aiyeonsu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun DriveScreen(
    track: Track,
    allEvents: List<Ev>,
    settings: Settings,
    voice: Voice,
    onFinish: (RunResult) -> Unit
) {
    val events = remember { if (settings.gasGuide) allEvents else allEvents.filter { it.kind != K_GAS } }
    val sim = remember { DriveSim(track, events) }
    var accident by remember { mutableStateOf(false) }
    var qa by remember { mutableStateOf<QA?>(null) }
    var qaIdx by remember { mutableStateOf(0) }
    var qaKey by remember { mutableStateOf(0) }

    val out: (String) -> Unit = { s ->
        val ok = settings.voiceMode == 0 || (settings.voiceMode == 1 && sim.pose == Pose.CAUTION)
        if (ok) voice.speak(s)
    }

    LaunchedEffect(Unit) {
        val first = if (settings.autoDefog) {
            val mode = if (settings.defogMode == 0) "외기모드" else "에어컨"
            "차 안에 김이 서릴 수 있어요. $mode 를 켜고 ${settings.defogTemp.roundToInt()}도로 맞췄어요. 출발할게요."
        } else {
            "출발할게요. 천천히 가요."
        }
        sim.speakOut(first, Pose.GUIDE, 5f, out)
        var last = withFrameNanos { it }
        var acc = 0f
        while (true) {
            val now = withFrameNanos { it }
            acc += ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
            last = now
            if (acc < 0.033f) continue   // 약 30프레임으로 갱신 (에뮬레이터 부담 줄이기)
            val dt = acc
            acc = 0f
            sim.step(dt, settings.gapAlert, settings.narrate, out)
            if (sim.finished) {
                onFinish(sim.result())
                break
            }
        }
    }

    LaunchedEffect(qaKey) {
        if (qa != null) {
            delay(10_000)
            qa = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ------------------------------------------------ 왼쪽: 가상 3D 도로
            val leftShape = RoundedCornerShape(36.dp)
            Box(
                Modifier.weight(0.38f).fillMaxHeight()
                    .shadow(18.dp, leftShape, clip = false, ambientColor = Color(0x14000000), spotColor = Color(0x331B3A5C))
                    .clip(leftShape)
            ) {
                RoadView(sim.dist * track.mpp, sim.curve, sim.frontGap, sim.t, Modifier.fillMaxSize())

                Column(Modifier.align(Alignment.TopStart).padding(26.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${sim.speed.roundToInt()}", fontSize = 68.sp, fontWeight = FontWeight.Light, color = AyInk, letterSpacing = (-2).sp)
                        Text(" km/h", fontSize = 18.sp, color = AySub, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Tag("D 주행 중", AyNavy)
                        val close = sim.frontGap < settings.gapAlert
                        Tag("앞차 ${sim.frontGap.roundToInt()}m", if (close) AyRed else Color.White, if (close) Color.White else AyInk)
                    }
                }

                if (settings.autoDefog) {
                    Text(
                        "김서림 방지 · " + (if (settings.defogMode == 0) "외기모드" else "A/C") + " · ${settings.defogTemp.roundToInt()}°C",
                        fontSize = 13.sp, color = AyNavy, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.TopEnd).padding(26.dp)
                            .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Column(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (sim.gasActive) GasCard(sim.stopLeft)
                    val q = qa
                    if (q != null) {
                        Panel(Modifier.fillMaxWidth(), radius = 24.dp, pad = 16.dp) {
                            Text("나", fontSize = 12.sp, color = AyMute)
                            Text(q.q, fontSize = 16.sp, color = AyInk, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Text("연곰 아저씨", fontSize = 12.sp, color = AySky, fontWeight = FontWeight.SemiBold)
                            Text(q.a, fontSize = 17.sp, color = AyInk, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 14.dp, tint = Color.White.copy(alpha = 0.94f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                                VoiceWave(sim.speaking, if (sim.pose == Pose.CAUTION) AyRed else AySky, 104.dp)
                                Mascot(sim.pose, 92.dp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(sim.msg, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = AyInk, lineHeight = 28.sp)
                        }
                    }
                }
            }

            // ------------------------------------------------ 오른쪽: 지도 (진행 방향이 위)
            val rightShape = RoundedCornerShape(36.dp)
            Box(
                Modifier.weight(0.62f).fillMaxHeight()
                    .shadow(18.dp, rightShape, clip = false, ambientColor = Color(0x14000000), spotColor = Color(0x331B3A5C))
                    .clip(rightShape)
            ) {
                DriveMap(track, events, sim.dist, sim.heading, Modifier.fillMaxSize())

                val next = events.firstOrNull { it.at > sim.dist }
                Panel(Modifier.align(Alignment.TopStart).padding(20.dp), radius = 26.dp, pad = 16.dp) {
                    if (next != null) {
                        val m = (((next.at - sim.dist) * track.mpp) / 10f).roundToInt() * 10
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(evColor(next.kind)),
                                contentAlignment = Alignment.Center
                            ) { Text(next.tag, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                            Spacer(Modifier.width(12.dp))
                            Text("${m}m", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = AyInk, letterSpacing = (-1).sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(next.title, fontSize = 15.sp, color = AySub)
                    } else {
                        Text("도착", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = AyInk)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("연습 ${STAGES[settings.stage].no}단계 · ${STAGES[settings.stage].title}", fontSize = 13.sp, color = AySky, fontWeight = FontWeight.SemiBold)
                }

                // 시연용 컨트롤 (영상 촬영할 때 쓰는 버튼)
                Panel(Modifier.align(Alignment.TopEnd).padding(20.dp), radius = 22.dp, pad = 12.dp) {
                    Text("시연용 컨트롤", fontSize = 11.sp, color = AyMute)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 6, 10).forEach { m -> MiniBtn("${m}x", sim.mult == m) { sim.mult = m } }
                        MiniBtn(if (sim.running) "일시정지" else "재생", false) { sim.running = !sim.running }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MiniBtn("사고 시연", false) {
                            accident = true
                            sim.running = false
                            voice.speak("괜찮으세요? 먼저 안전한 곳에 차를 세워 주세요.")
                        }
                        MiniBtn("AI에게 질문", false) {
                            qa = QAS[qaIdx % QAS.size]
                            qaIdx++
                            qaKey++
                            sim.pose = Pose.CLOSEUP
                            sim.speakUntil = sim.t + 6f
                            voice.speak(qa?.a ?: "")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MiniBtn("자율주행 해설 " + (if (settings.narrate) "ON" else "OFF"), settings.narrate) { settings.narrate = !settings.narrate }
                        MiniBtn("주행 끝내기", false) { onFinish(sim.result()) }
                    }
                }

                Text(
                    "시연용 지도 · 가상 주행 데이터",
                    fontSize = 11.sp, color = AySub,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp)
                        .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (accident) {
            AccidentSheet(settings) {
                accident = false
                sim.running = true
            }
        }
    }
}

@Composable
private fun MiniBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Color.White else AyInk,
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) AyNavy else Color(0xFFEEF2F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

// 주유소에 서 있는 동안 보여 주는 주유 순서
@Composable
private fun GasCard(stopLeft: Float) {
    val idx = (((9f - stopLeft) / 9f) * GAS_STEPS.size).toInt().coerceIn(0, GAS_STEPS.size - 1)
    Panel(Modifier.fillMaxWidth(), radius = 24.dp, pad = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag("주유소 안내", Color(0xFF0E9AA7))
            Spacer(Modifier.width(8.dp))
            Text("시동을 끄기 전에 순서를 알려 드려요", fontSize = 13.sp, color = AySub)
        }
        Spacer(Modifier.height(8.dp))
        GAS_STEPS.forEachIndexed { i, s ->
            val on = i == idx
            Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(22.dp).clip(CircleShape).background(if (i <= idx) AyNavy else Color(0xFFDDE3EA)),
                    contentAlignment = Alignment.Center
                ) { Text("${i + 1}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(10.dp))
                Text(
                    s, fontSize = if (on) 17.sp else 14.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    color = if (on) AyInk else AySub
                )
            }
        }
    }
}

// 사고가 났을 때: 상태 확인 -> 해야 할 일 -> 보험사 연결 (시연)
@Composable
private fun AccidentSheet(settings: Settings, onClose: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var injured by remember { mutableStateOf(false) }
    var conn by remember { mutableStateOf(0) }   // 0 대기  1 연결 중  2 연결됨
    val checks = remember { mutableStateListOf(false, false, false, false, false) }

    LaunchedEffect(step) {
        if (step == 1 && settings.accidentAuto && conn == 0) conn = 1
    }
    LaunchedEffect(conn) {
        if (conn == 1) {
            delay(1800)
            conn = 2
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color(0x66101820)).clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Panel(Modifier.width(680.dp), radius = 36.dp, pad = 30.dp) {
            if (step == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Mascot(Pose.CAUTION, 130.dp)
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("큰 충격이 느껴졌어요", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = AyInk)
                        Text("괜찮으세요? 대답해 주세요.", fontSize = 18.sp, color = AySub)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillButton("괜찮아요", { injured = false; step = 1 }, Modifier.weight(1f))
                    PillButton("도움이 필요해요", { injured = true; step = 1 }, Modifier.weight(1f), color = AyRed)
                }
            } else {
                Text("침착하게, 이 순서대로 해요", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AyInk)
                if (injured) {
                    Spacer(Modifier.height(8.dp))
                    Text("119에 먼저 연결할게요 (시연)", fontSize = 16.sp, color = AyRed, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(14.dp))
                ACCIDENT_STEPS.forEachIndexed { i, s ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .clickable { checks[i] = !checks[i] }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape)
                                .background(if (checks[i]) AyGreen else Color.White)
                                .border(2.dp, if (checks[i]) AyGreen else AyLine, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { if (checks[i]) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        Spacer(Modifier.width(14.dp))
                        Text(s, fontSize = 18.sp, color = AyInk)
                    }
                }
                Spacer(Modifier.height(14.dp))
                val label = when (conn) {
                    1 -> "보험사에 연결 중… (시연)"
                    2 -> "보험사 연결됨 · 위치와 사고 시각을 전달했어요 (시연)"
                    else -> if (settings.accidentAuto) "" else "보험사 연결 (시연)"
                }
                if (conn == 0 && !settings.accidentAuto) {
                    PillButton(label, { conn = 1 }, Modifier.fillMaxWidth(), filled = false)
                } else if (label.isNotEmpty()) {
                    Text(
                        label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                        color = if (conn == 2) AyGreen else AyNavy,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                            .background(Color(0xFFEFF3F8)).padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                PillButton("닫고 계속 주행하기", onClose, Modifier.fillMaxWidth())
            }
        }
    }
}
