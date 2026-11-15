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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReviewScreen(result: RunResult?, settings: Settings, onAgain: () -> Unit, onGrowth: () -> Unit) {
    // 주행 기록이 없으면 예시 값으로 보여 줍니다 (시연용)
    val r = result ?: RunResult(24, 7.8f, 1, 52, 12, 3, 1, 18, 17)
    var shared by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // ------------------------------------------------ 왼쪽: 안정성 분석
        Column(
            Modifier.weight(0.5f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(Pose.PRAISE, 170.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Tag("P 주차 중 · 주행 완료", AyNavy)
                    Spacer(Modifier.height(8.dp))
                    BigTitle("오늘도 안전하게\n잘 도착했어요", "평택대학교 → 안성팜랜드")
                }
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("주행 안정성", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk, modifier = Modifier.weight(1f))
                    Text("${r.total}", fontSize = 56.sp, fontWeight = FontWeight.Light, color = AyNavy, letterSpacing = (-2).sp)
                    Text(" / 100", fontSize = 16.sp, color = AySub, modifier = Modifier.padding(bottom = 10.dp))
                }
                Spacer(Modifier.height(10.dp))
                Metric("급제동 제어", r.brakeScore, AySky)
                Metric("조향·차선 변경", r.steerScore, AyBlue)
                Metric("속도 준수율", r.speedScore, AyGreen)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Stat("주행", "${r.minutes}분 · ${"%.1f".format(r.km)}km", Modifier.weight(1f))
                    Stat("최고 속도", "${r.maxSpeed}km/h", Modifier.weight(1f))
                    Stat("간격 알림", "${r.alerts}회", Modifier.weight(1f))
                    Stat("가장 가까운 앞차", "${r.minGap}m", Modifier.weight(1f))
                }
                Text("※ 모든 수치는 시연용 가상 데이터예요.", fontSize = 12.sp, color = AyMute, modifier = Modifier.padding(top = 8.dp))
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Text("연곰 아저씨의 한마디", fontSize = 14.sp, color = AySky, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Tag("잘한 점", AyGreen)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (r.goodTurns > 0) "회전 구간에서 속도를 충분히 줄이고 부드럽게 돌았어요." else "차분하게 끝까지 달렸어요.",
                        fontSize = 18.sp, color = AyInk, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Tag("개선 팁", AyAmber)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (r.alerts > 0) "앞차와 간격을 조금만 더 벌려 봐요. 브레이크를 미리 밟으면 더 편해요." else "지금처럼 앞차와 간격을 유지해요.",
                        fontSize = 18.sp, color = AyInk
                    )
                }
            }
        }

        // ------------------------------------------------ 오른쪽: 끼어들기 복기 + 멘토
        Column(
            Modifier.weight(0.5f).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("끼어들기 복기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk, modifier = Modifier.weight(1f))
                    Tag("잘한 장면", AyGreen)
                }
                Text("합류 구간에서 앞차와 뒷차 사이로 들어간 장면이에요.", fontSize = 14.sp, color = AySub)
                Spacer(Modifier.height(12.dp))
                MergeDiagram(r.mergeFront, r.mergeRear, Modifier.fillMaxWidth().height(290.dp))
                Spacer(Modifier.height(14.dp))
                if (settings.myGapFront == 0) {
                    PillButton("이 장면으로 나만의 기준 간격 만들기", {
                        settings.myGapFront = r.mergeFront
                        settings.myGapRear = r.mergeRear
                    }, Modifier.fillMaxWidth(), size = 16)
                } else {
                    Text(
                        "나만의 기준 간격  앞 ${settings.myGapFront}m · 뒤 ${settings.myGapRear}m  (저장됨)",
                        fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AyNavy,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                            .background(Color(0xFFE5EEF9)).padding(16.dp)
                    )
                    Text("다음 끼어들기 때 이 간격을 기준으로 알려 드려요.", fontSize = 13.sp, color = AySub, modifier = Modifier.padding(top = 6.dp))
                }
            }

            Panel(Modifier.fillMaxWidth(), radius = 30.dp, pad = 22.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("멘토 코멘트", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AyInk, modifier = Modifier.weight(1f))
                    Text("아버지 · 오늘 16:42", fontSize = 13.sp, color = AySub)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"속도 맞춰서 합류 잘했네. 급제동만 조금 일찍 밟는 연습 해보자. 수고했어!\"",
                    fontSize = 18.sp, color = AyInk, lineHeight = 26.sp
                )
                Spacer(Modifier.height(14.dp))
                if (!shared) {
                    PillButton("멘토에게 결과 공유", { shared = true }, Modifier.fillMaxWidth(), filled = false, size = 16)
                } else {
                    Text("공유했어요 ✓  선택한 항목만 전달돼요 (성장·멘토 탭에서 변경)", fontSize = 15.sp, color = AyGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillButton("다시 연습하기", onAgain, Modifier.weight(1f))
                PillButton("성장 기록 보기", onGrowth, Modifier.weight(1f), filled = false)
            }
        }
    }
}

@Composable
private fun Metric(name: String, score: Int, color: Color) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row {
            Text(name, fontSize = 16.sp, color = AyInk, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("${score}점", fontSize = 16.sp, color = AyInk, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Meter(score / 100f, color, Modifier.fillMaxWidth())
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFFF1F4F8)).padding(12.dp)) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AyInk)
        Text(label, fontSize = 11.sp, color = AySub)
    }
}

// 합류한 장면을 위에서 내려다본 그림: 앞차·뒷차와의 간격
@Composable
private fun MergeDiagram(front: Int, rear: Int, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(Color(0xFFE9EEF4), Offset.Zero, Size(w, h), CornerRadius(28f, 28f))
        val roadL = w * 0.22f
        val roadR = w * 0.62f
        val mid = (roadL + roadR) / 2f
        drawRect(Color(0xFFC9D2DE), Offset(roadL, 0f), Size(roadR - roadL, h))
        var y = 0f
        while (y < h) {
            drawLine(Color.White, Offset(mid, y), Offset(mid, y + 16f), 4f)
            y += 32f
        }
        val laneL = roadL + (mid - roadL) / 2f
        val laneR = mid + (roadR - mid) / 2f
        val k = h * 0.0140f
        val carH = h * 0.13f
        val carW = (roadR - roadL) * 0.30f
        val gf = (front * k).coerceIn(h * 0.06f, h * 0.28f)
        val gr = (rear * k).coerceIn(h * 0.06f, h * 0.28f)
        val total = carH * 3f + gf + gr
        val top = (h - total) / 2f
        val myTop = top + carH + gf
        val rearTop = myTop + carH + gr

        fun car(cx: Float, ty: Float, body: Color) {
            drawRoundRect(body, Offset(cx - carW / 2f, ty), Size(carW, carH), CornerRadius(14f, 14f))
            drawRoundRect(Color(0x99FFFFFF), Offset(cx - carW * 0.36f, ty + carH * 0.2f), Size(carW * 0.72f, carH * 0.26f), CornerRadius(8f, 8f))
        }
        car(laneL, top, Color(0xFFF4F6F9))
        car(laneR, myTop + carH * 0.55f, Color(0x552B313B))          // 합류 전 내 차 (흐리게)
        drawLine(Color(0x551B5FD6), Offset(laneR, myTop + carH * 0.55f), Offset(laneL, myTop + carH * 0.2f), 4f)
        car(laneL, myTop, Color(0xFF2B313B))                          // 합류한 내 차
        car(laneL, rearTop, Color(0xFFF4F6F9))

        val x = roadR + 28f
        val y1 = top + carH
        val y2 = myTop
        drawLine(AyBlue, Offset(x, y1), Offset(x, y2), 4f)
        drawLine(AyBlue, Offset(x - 10f, y1), Offset(x + 10f, y1), 4f)
        drawLine(AyBlue, Offset(x - 10f, y2), Offset(x + 10f, y2), 4f)
        label(tm, "앞 ${front}m", Offset(x + 90f, (y1 + y2) / 2f), AyNavy, 20f, FontWeight.Bold)

        val y3 = myTop + carH
        val y4 = rearTop
        drawLine(AyGreen, Offset(x, y3), Offset(x, y4), 4f)
        drawLine(AyGreen, Offset(x - 10f, y3), Offset(x + 10f, y3), 4f)
        drawLine(AyGreen, Offset(x - 10f, y4), Offset(x + 10f, y4), 4f)
        label(tm, "뒤 ${rear}m", Offset(x + 90f, (y3 + y4) / 2f), AyNavy, 20f, FontWeight.Bold)
    }
}
