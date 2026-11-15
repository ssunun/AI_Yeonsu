package com.example.aiyeonsu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun PreDriveScreen(track: Track, events: List<Ev>, settings: Settings, onStart: () -> Unit) {
    Row(
        Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // ------------------------------------------------ 왼쪽: 인사 + 단계 선택
        Column(
            Modifier.weight(0.38f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(Pose.GUIDE, 190.dp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Tag("P 주차 중 · 출발 전", AyNavy)
                    Spacer(Modifier.height(8.dp))
                    Text("안녕! 나는 연곰 아저씨야.", fontSize = 15.sp, color = AySub)
                }
            }
            BigTitle("오늘, 어디까지\n연습해 볼까요?", "목표를 고르면 길 위의 연습 지점을 미리 알려 드려요")

            Panel(Modifier.fillMaxWidth(), radius = 28.dp, pad = 10.dp) {
                STAGES.forEachIndexed { i, st ->
                    val sel = i == settings.stage
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(if (sel) AyNavy else Color.Transparent)
                            .clickable { settings.stage = i }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape)
                                .background(if (sel) Color.White else Color(0xFFE6EBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${st.no}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (sel) AyNavy else AySub)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(st.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (sel) Color.White else AyInk)
                            Text(st.desc, fontSize = 13.sp, color = if (sel) Color(0xFFC9D8EA) else AySub)
                        }
                    }
                }
            }
            Text(
                "아저씨의 한마디  \"" + STAGES[settings.stage].tip + "\"",
                fontSize = 16.sp, color = AyNavy, fontWeight = FontWeight.Medium, lineHeight = 22.sp
            )
        }

        // ------------------------------------------------ 오른쪽: 코스 + 설정
        Column(
            Modifier.weight(0.62f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val course = COURSES[settings.course]
            Panel(Modifier.fillMaxWidth(), radius = 32.dp, pad = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("오늘의 연습 코스", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AyInk, modifier = Modifier.weight(1f))
                    Text("${course.km}km · 약 ${course.minutes}분", fontSize = 16.sp, color = AySub, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp))) {
                    PreviewMap(track, events, Modifier.fillMaxSize())
                    Text(
                        "시연용 지도", fontSize = 11.sp, color = AySub,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                            .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))

                // 경로 위 연습 지점 미리 보기
                val turns = events.count { it.kind == K_TURN }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("회전 ${turns}곳", AyBlue)
                    Tag("합류 1곳", AyAmber)
                    Tag("사고다발 1곳", AyRed)
                    Tag("주유소 1곳", Color(0xFF0E9AA7))
                }
                Spacer(Modifier.height(10.dp))
                events.filter { it.kind != K_ARRIVE }.forEach { ev ->
                    val km = ev.at * track.mpp / 1000f
                    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(evColor(ev.kind)))
                        Spacer(Modifier.width(10.dp))
                        Text("출발 ${((km * 10f).roundToInt() / 10f)}km 지점 · ${ev.title}", fontSize = 14.sp, color = AyInk)
                    }
                }
                Spacer(Modifier.height(14.dp))
                // 코스 추천 (10km 안팎)
                Text("10km 안팎 추천 코스", fontSize = 14.sp, color = AySub, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    COURSES.forEachIndexed { i, c ->
                        val sel = i == settings.course
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                                .background(if (sel) Color(0xFFE5EEF9) else Color(0xFFF3F5F8))
                                .border(1.5.dp, if (sel) AyNavy else Color.Transparent, RoundedCornerShape(20.dp))
                                .clickable { settings.course = i }.padding(14.dp)
                        ) {
                            Text(c.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AyInk)
                            Text("${c.km}km · ${c.stages}", fontSize = 12.sp, color = AySub)
                            if (c.sample) Text("예시 코스", fontSize = 11.sp, color = AyMute)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(course.note, fontSize = 13.sp, color = AySub)
                if (course.sample) {
                    Text("※ 이번 시연 주행은 첫 번째 코스(평택대 → 안성팜랜드)로 진행돼요.", fontSize = 12.sp, color = AyMute)
                }
                Spacer(Modifier.height(16.dp))
                PillButton("D 연습 주행 시작 (가상 주행)", onStart, Modifier.fillMaxWidth(), size = 19)
            }

            // 앞차 간격 알림 거리
            Panel(Modifier.fillMaxWidth(), radius = 28.dp, pad = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("앞차 간격 알림 거리", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AyInk)
                        Text("차량 카메라로 앞차를 보다가, 이 거리보다 가까워지면 한 문장으로 알려 줘요.", fontSize = 13.sp, color = AySub)
                    }
                    Text("${settings.gapAlert.roundToInt()}m", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = AyNavy)
                }
                Slider(
                    value = settings.gapAlert,
                    onValueChange = { settings.gapAlert = it },
                    valueRange = 5f..40f,
                    colors = SliderDefaults.colors(
                        thumbColor = AyNavy, activeTrackColor = AyNavy, inactiveTrackColor = Color(0xFFD5DCE6)
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("가깝게 10m" to 10f, "기본 15m" to 15f, "넉넉히 25m" to 25f).forEach { (label, v) ->
                        val on = settings.gapAlert.roundToInt() == v.roundToInt()
                        Text(
                            label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (on) Color.White else AyInk,
                            modifier = Modifier.clip(RoundedCornerShape(50))
                                .background(if (on) AyNavy else Color(0xFFEEF2F6))
                                .clickable { settings.gapAlert = v }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
                Text("※ 앞차 거리는 시연용 가상 데이터예요. 실제 속도에 맞춘 거리 기준은 추후 반영해요.", fontSize = 12.sp, color = AyMute, modifier = Modifier.padding(top = 8.dp))
            }

            // 아저씨의 안심 도우미
            Text("아저씨의 안심 도우미", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk)
            Panel(Modifier.fillMaxWidth(), radius = 28.dp, pad = 20.dp) {
                SwitchRow("겨울 김서림 방지", "출발할 때 외기모드 또는 A/C 프론트로 김을 먼저 없애요", settings.autoDefog) { settings.autoDefog = it }
                if (settings.autoDefog) {
                    Spacer(Modifier.height(6.dp))
                    Segmented(listOf("외기모드", "A/C 프론트"), settings.defogMode, { settings.defogMode = it })
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("온도 ${settings.defogTemp.roundToInt()}°C", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AyInk)
                        Slider(
                            value = settings.defogTemp, onValueChange = { settings.defogTemp = it.roundToInt().toFloat() },
                            valueRange = 22f..24f, steps = 1, modifier = Modifier.weight(1f).padding(start = 12.dp),
                            colors = SliderDefaults.colors(thumbColor = AyNavy, activeTrackColor = AyNavy, inactiveTrackColor = Color(0xFFD5DCE6))
                        )
                    }
                    Text("※ 실제 공조 제어는 차량 SDK 연동 후 가능해요. 지금은 화면과 음성 흐름만 시연해요.", fontSize = 12.sp, color = AyMute)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(AyLine))
                SwitchRow("주유소 안내", "주유소에 서면 시동을 끄기 전에 주유 순서를 알려 줘요", settings.gasGuide) { settings.gasGuide = it }
                Box(Modifier.fillMaxWidth().height(1.dp).background(AyLine))
                SwitchRow("사고 시 보험사 자동 연결", "사고를 확인하면 보험사 연결과 해야 할 일을 안내해요", settings.accidentAuto) { settings.accidentAuto = it }
                Box(Modifier.fillMaxWidth().height(1.dp).background(AyLine))
                Spacer(Modifier.height(8.dp))
                Text("음성 안내", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AyInk)
                Spacer(Modifier.height(6.dp))
                Segmented(listOf("전체 알림", "위험만 알림", "음소거"), settings.voiceMode, { settings.voiceMode = it })
            }
        }
    }
}
