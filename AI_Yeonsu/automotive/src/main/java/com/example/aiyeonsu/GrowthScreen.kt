package com.example.aiyeonsu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GrowthScreen(settings: Settings) {
    var graduated by remember { mutableStateOf(false) }
    val done = MISSIONS.count { it.done }
    val percent = done * 100 / MISSIONS.size

    Row(
        Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // ------------------------------------------------ 왼쪽: 성장 기록
        Column(
            Modifier.weight(0.5f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(Pose.CHEER, 160.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    BigTitle("지민님의 성장 기록", "함께한 지 32일 · 7번 주행 · 248km")
                }
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("초보 졸업까지", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stroke = 20f
                            val inset = stroke / 2f
                            val d = Size(size.width - stroke, size.height - stroke)
                            drawArc(Color(0xFFE3E8EF), -90f, 360f, false, Offset(inset, inset), d, style = Stroke(stroke, cap = StrokeCap.Round))
                            drawArc(AyNavy, -90f, 360f * percent / 100f, false, Offset(inset, inset), d, style = Stroke(stroke, cap = StrokeCap.Round))
                        }
                        Text("$percent%", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = AyNavy)
                    }
                    Spacer(Modifier.width(22.dp))
                    Column(Modifier.weight(1f)) {
                        MISSIONS.forEachIndexed { i, m ->
                            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(24.dp).clip(CircleShape).background(if (m.done) AyGreen else Color(0xFFDDE3EA)),
                                    contentAlignment = Alignment.Center
                                ) { Text(if (m.done) "✓" else "${i + 1}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(m.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (m.done) AyInk else AySub)
                                    Text(m.sub, fontSize = 12.sp, color = AyMute)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                PillButton(if (graduated) "졸업 시연 닫기" else "졸업 시연 보기", { graduated = !graduated }, Modifier.fillMaxWidth(), filled = false, size = 15)
                if (graduated) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFE5EEF9)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Mascot(Pose.PRAISE, 110.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("초보 졸업을 축하해요!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AyNavy)
                            Text("이제 후배 운전자를 이끄는 멘토가 될 수 있어요.", fontSize = 15.sp, color = AyInk)
                        }
                    }
                }
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("이번 주 미션", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AySky)
                Text("합류 3회 안정적으로 진입하기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk)
                Spacer(Modifier.height(10.dp))
                Meter(2f / 3f, AyBlue, Modifier.fillMaxWidth())
                Text("2 / 3회 달성", fontSize = 14.sp, color = AySub, modifier = Modifier.padding(top = 6.dp))
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("회차별 점수", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk)
                Spacer(Modifier.height(8.dp))
                HISTORY.forEach { h ->
                    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${h.no}회", fontSize = 15.sp, color = AyMute, modifier = Modifier.width(44.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${h.date} · ${h.goal}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AyInk)
                            Text(h.line, fontSize = 12.sp, color = AySub)
                        }
                        Text("${h.score}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AyNavy)
                    }
                }
            }
        }

        // ------------------------------------------------ 오른쪽: 멘토 연결
        Column(
            Modifier.weight(0.5f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("멘토 연결", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AyInk, modifier = Modifier.weight(1f))
                    Tag("아버지 · 연결됨", AyGreen)
                }
                Spacer(Modifier.height(12.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFF1F4F8)).padding(18.dp)
                ) {
                    Text("초대 코드", fontSize = 13.sp, color = AySub)
                    Text("H-8294", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AyNavy, letterSpacing = 4.sp)
                    Text("가족·지인에게 코드를 알려 주면 멘토로 연결돼요.", fontSize = 13.sp, color = AySub)
                }
                Spacer(Modifier.height(14.dp))
                Segmented(listOf("동승 모드", "원격 모드"), settings.mentorCoRide, { settings.mentorCoRide = it })
                Spacer(Modifier.height(8.dp))
                Text(
                    if (settings.mentorCoRide == 0) "같이 탈 때: 멘토가 옆자리에서 아저씨와 함께 코칭해요. 말은 줄이고 중요한 순간만 짚어 줘요."
                    else "멀리 있을 때: 멘토가 주행 리뷰를 보고 코멘트를 남겨요. 실시간 위치는 보지 않아요.",
                    fontSize = 15.sp, color = AyInk, lineHeight = 22.sp
                )
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("공유할 항목", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk)
                Text("항목마다 직접 정해요. 실시간 GPS 위치는 보내지 않아요.", fontSize = 13.sp, color = AySub)
                Spacer(Modifier.height(6.dp))
                SwitchRow("급제동·간격 알림 횟수", "멘토가 안전 습관을 같이 볼 수 있어요", settings.shareEvents) { settings.shareEvents = it }
                SwitchRow("주행 안정성 점수", "회차별 점수를 보여 줘요", settings.shareScore) { settings.shareScore = it }
                SwitchRow("경로 요약", "어느 구간을 연습했는지만 보여 줘요", settings.shareRoute) { settings.shareRoute = it }
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("최근 멘토 코멘트", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AySky)
                Spacer(Modifier.height(6.dp))
                Text(
                    "\"속도 맞춰서 합류 잘했네. 급제동만 조금 일찍 밟는 연습 해보자. 수고했어!\"",
                    fontSize = 18.sp, color = AyInk, lineHeight = 26.sp
                )
                Text("아버지 · 오늘 16:42", fontSize = 13.sp, color = AySub, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
