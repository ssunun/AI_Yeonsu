package com.example.aiyeonsu

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

// =====================================================================
//  평택대학교 -> 안성팜랜드 가상 주행 경로와 지도
//  * 경로선: 지도 앱에서 확인한 코스(7.8km)를 따라 그린 점 목록입니다. (그림 좌표)
//  * 배경 지도(도로망·공원·강)는 코드로 직접 그린 시연용 그림이며 실제 지도 데이터가 아닙니다.
//  * 속도, 앞차 거리, 합류·사고다발·주유소 지점은 모두 시연용 가상 데이터입니다.
// =====================================================================

internal const val PIF = 3.1415927f
const val ROUTE_METERS = 7800f

val ROUTE = floatArrayOf(
        68.0f, 482.0f, 83.8f, 484.5f, 99.5f, 485.4f, 115.1f, 483.0f,
        130.5f, 478.8f, 145.6f, 473.6f, 160.5f, 467.8f, 175.1f, 461.4f,
        189.3f, 454.0f, 203.3f, 446.3f, 217.1f, 438.2f, 230.4f, 429.3f,
        243.4f, 420.0f, 256.7f, 411.2f, 270.7f, 403.7f, 285.3f, 397.2f,
        300.3f, 392.2f, 316.0f, 389.4f, 331.9f, 387.8f, 347.8f, 386.4f,
        363.8f, 385.4f, 379.8f, 384.7f, 395.8f, 384.0f, 411.7f, 383.4f,
        427.7f, 383.0f, 443.7f, 382.7f, 459.7f, 382.4f, 475.7f, 382.2f,
        491.7f, 381.7f, 507.7f, 380.7f, 523.6f, 379.6f, 539.0f, 376.8f,
        552.6f, 370.2f, 564.7f, 360.2f, 575.2f, 348.3f, 584.2f, 335.2f,
        592.5f, 321.5f, 600.8f, 307.8f, 609.4f, 294.4f, 619.1f, 281.8f,
        630.8f, 271.2f, 644.3f, 263.2f, 659.3f, 258.9f, 675.0f, 258.0f,
        691.0f, 258.6f, 706.9f, 259.6f, 722.9f, 260.5f, 738.9f, 261.4f,
        754.9f, 262.4f, 770.8f, 263.3f, 786.6f, 263.5f, 802.0f, 261.2f,
        817.2f, 256.6f, 832.4f, 251.8f, 847.7f, 247.3f, 863.0f, 242.8f,
        878.4f, 238.4f, 893.9f, 234.5f, 909.5f, 231.0f, 925.2f, 227.6f,
        940.8f, 224.3f, 956.5f, 221.0f, 972.1f, 217.6f, 987.6f, 214.3f,
        1002.5f, 214.0f, 1015.1f, 220.5f, 1025.6f, 232.0f, 1036.0f, 244.0f,
        1045.6f, 256.4f, 1051.6f, 270.5f, 1053.7f, 286.0f, 1055.0f, 301.9f,
        1056.5f, 317.7f, 1057.3f, 333.6f, 1057.7f, 349.6f, 1055.9f, 364.9f,
        1049.1f, 378.7f, 1039.1f, 391.2f, 1029.1f, 403.6f, 1020.8f, 417.0f,
        1016.3f, 431.4f, 1018.0f, 445.9f, 1022.6f, 460.3f, 1022.8f, 474.7f,
        1016.7f, 487.3f, 1008.0f, 496.9f
)

val AyMapBg = Color(0xFFEEF1F5)
val AyRoute = Color(0xFF1B5FD6)

const val K_TURN = 0
const val K_MERGE = 1
const val K_DANGER = 2
const val K_GAS = 3
const val K_ARRIVE = 4

class Track(raw: FloatArray) {
    val n = raw.size / 2
    val xs = FloatArray(n) { raw[it * 2] }
    val ys = FloatArray(n) { raw[it * 2 + 1] }
    val cum = FloatArray(n)

    init {
        for (i in 1 until n) cum[i] = cum[i - 1] + hypot(xs[i] - xs[i - 1], ys[i] - ys[i - 1])
    }

    val total = cum[n - 1]
    val mpp = ROUTE_METERS / total   // 그림 1픽셀이 몇 미터인지

    fun pos(d0: Float): Offset {
        val d = d0.coerceIn(0f, total)
        var i = 0
        while (i < n - 2 && cum[i + 1] < d) i++
        val seg = cum[i + 1] - cum[i]
        val f = if (seg == 0f) 0f else (d - cum[i]) / seg
        return Offset(xs[i] + (xs[i + 1] - xs[i]) * f, ys[i] + (ys[i + 1] - ys[i]) * f)
    }

    // 진행 방향 (위쪽 기준 시계방향 라디안)
    fun heading(d: Float, l: Float = 20f): Float {
        val a = pos(d - l)
        val b = pos(d + l)
        return atan2(b.x - a.x, -(b.y - a.y))
    }

    fun fullPath(): Path {
        val p = Path()
        p.moveTo(xs[0], ys[0])
        for (i in 1 until n) p.lineTo(xs[i], ys[i])
        return p
    }

    fun pathFrom(d0: Float): Path {
        val d = d0.coerceIn(0f, total)
        val p = Path()
        val a = pos(d)
        p.moveTo(a.x, a.y)
        for (i in 0 until n) if (cum[i] > d) p.lineTo(xs[i], ys[i])
        return p
    }

    fun pathUpTo(d0: Float): Path {
        val d = d0.coerceIn(0f, total)
        val p = Path()
        p.moveTo(xs[0], ys[0])
        for (i in 1 until n) if (cum[i] < d) p.lineTo(xs[i], ys[i])
        val a = pos(d)
        p.lineTo(a.x, a.y)
        return p
    }
}

fun normAngle(a: Float): Float {
    var x = a
    while (x > PIF) x -= 2f * PIF
    while (x <= -PIF) x += 2f * PIF
    return x
}

// 경로 위 연습 지점
class Ev(
    val at: Float,        // 경로 시작부터의 거리(픽셀)
    val kind: Int,
    val tag: String,      // 지도 마커에 쓰는 한 글자
    val title: String,
    val voice: String,    // 300m 앞에서 들려주는 한 문장
    val narr: String,     // 자율주행 해설 모드 문장
    val praise: String    // 지나간 뒤 칭찬 한마디
)

fun buildEvents(t: Track): List<Ev> {
    val list = mutableListOf<Ev>()
    val thr = 35f * PIF / 180f
    var inGroup = false
    var bestAbs = 0f
    var bestAt = 0f
    var bestDelta = 0f

    fun addTurn() {
        val right = bestDelta > 0
        val name = if (right) "우회전" else "좌회전"
        list.add(
            Ev(
                bestAt, K_TURN, if (right) "우" else "좌", "$name 연습",
                "300미터 앞에서 $name 이에요. 속도를 줄이고 천천히 들어가요.",
                "차가 스스로 $name 하는 중이에요. 손은 핸들 가까이 두세요.",
                "$name 부드러웠어요. 잘했어요!"
            )
        )
    }

    var d = 60f
    while (d <= t.total - 60f) {
        val delta = normAngle(t.heading(d + 50f) - t.heading(d - 50f))
        if (abs(delta) > thr) {
            if (!inGroup) {
                inGroup = true
                bestAbs = 0f
            }
            if (abs(delta) > bestAbs) {
                bestAbs = abs(delta)
                bestAt = d
                bestDelta = delta
            }
        } else if (inGroup) {
            inGroup = false
            addTurn()
        }
        d += 4f
    }
    if (inGroup) addTurn()

    list.add(
        Ev(
            t.total * 0.25f, K_MERGE, "합", "합류·끼어들기 (시연용 가상 지점)",
            "곧 합류 구간이에요. 사이드미러로 뒤차 간격을 보고 들어가요.",
            "본선 속도에 맞춰 차가 합류하는 중이에요. 뒤차 간격을 같이 봐요.",
            "합류 깔끔했어요. 간격이 딱 좋아요!"
        )
    )
    list.add(
        Ev(
            t.total * 0.43f, K_GAS, "주", "주유소 (시연용 가상 지점)",
            "앞에 주유소가 있어요. 잠깐 들러서 주유 방법을 알려 드릴게요.",
            "주유소에 잠깐 들를게요. 차가 알아서 천천히 들어가요.",
            "출발이 부드러웠어요."
        )
    )
    list.add(
        Ev(
            t.total * 0.62f, K_DANGER, "!", "사고다발 구간 (시연용 가상 지점)",
            "사고가 잦은 구간이에요. 속도를 줄이고 앞차와 간격을 넓혀요.",
            "사고가 잦은 구간이라 차가 속도를 줄이고 있어요.",
            "조심해서 잘 지나갔어요!"
        )
    )
    list.add(Ev(t.total, K_ARRIVE, "도", "목적지 도착", "도착했어요. 오늘 정말 잘했어요.", "목적지에 도착했어요.", ""))
    list.sortBy { it.at }
    return list
}

// ---------------------------------------------------------------- 지도 카메라
class MapCam(val center: Offset, val phiDeg: Float, val scale: Float, val anchor: Offset) {
    fun toScreen(p: Offset): Offset {
        val dx = (p.x - center.x) * scale
        val dy = (p.y - center.y) * scale
        val r = phiDeg * PIF / 180f
        val c = cos(r)
        val s = sin(r)
        return Offset(anchor.x + dx * c - dy * s, anchor.y + dx * s + dy * c)
    }
}

private fun circlePath(cx: Float, cy: Float, r: Float): Path {
    val p = Path()
    p.addOval(Rect(Offset(cx, cy), r))
    return p
}

private val TOWN_A = circlePath(190f, 470f, 250f)
private val TOWN_B = circlePath(730f, 430f, 215f)

private val HIGHWAY = Path().apply {
    moveTo(262f, -80f)
    cubicTo(300f, 120f, 350f, 260f, 372f, 330f)
    cubicTo(400f, 420f, 440f, 470f, 470f, 570f)
    cubicTo(490f, 640f, 520f, 720f, 545f, 880f)
}
private val ROAD_N = Path().apply {
    moveTo(695f, -80f)
    cubicTo(690f, 100f, 670f, 220f, 650f, 340f)
}
private val ROAD_NE = Path().apply {
    moveTo(700f, -20f)
    cubicTo(820f, 40f, 940f, 110f, 1010f, 205f)
    cubicTo(1070f, 260f, 1120f, 300f, 1240f, 330f)
}
private val RIVER = Path().apply {
    moveTo(945f, -80f)
    cubicTo(960f, 150f, 900f, 360f, 800f, 520f)
    cubicTo(740f, 610f, 640f, 640f, 560f, 700f)
    lineTo(520f, 900f)
}
private val RIVER2 = Path().apply {
    moveTo(1250f, 660f)
    cubicTo(1130f, 700f, 1060f, 760f, 1000f, 900f)
}
private val COUNTRY = Path().apply {
    moveTo(-120f, 330f); cubicTo(100f, 340f, 230f, 250f, 330f, 140f)
    moveTo(540f, 90f); cubicTo(560f, 180f, 600f, 240f, 640f, 330f)
    moveTo(760f, 640f); cubicTo(820f, 600f, 900f, 640f, 1010f, 600f)
    moveTo(870f, 140f); cubicTo(860f, 260f, 900f, 360f, 940f, 470f)
    moveTo(560f, 560f); cubicTo(660f, 540f, 760f, 560f, 860f, 700f)
    moveTo(80f, 650f); cubicTo(200f, 640f, 330f, 700f, 420f, 780f)
    moveTo(1050f, 440f); cubicTo(1110f, 450f, 1170f, 430f, 1260f, 460f)
}

private fun DrawScope.grid(
    center: Offset, angleDeg: Float, spacing: Float, count: Int,
    w: Float, color: Color, seed: Int, skip: Float
) {
    val a = angleDeg * PIF / 180f
    val ux = cos(a)
    val uy = sin(a)
    val nx = -uy
    val ny = ux
    val rnd = Random(seed)
    val len = 420f
    for (k in -count..count) {
        if (rnd.nextFloat() < skip) continue
        val bx = center.x + nx * k * spacing
        val by = center.y + ny * k * spacing
        drawLine(color, Offset(bx - ux * len, by - uy * len), Offset(bx + ux * len, by + uy * len), w)
    }
}

// 지도 그림 좌표계에서 그리는 배경 + 경로
private fun DrawScope.drawWorld(track: Track, dist: Float) {
    val round = StrokeCap.Round
    drawRect(AyMapBg, Offset(-1500f, -1500f), Size(4400f, 4400f))
    // 공원
    drawOval(Color(0xFFDCEDE0), Offset(930f, 500f), Size(270f, 170f))
    drawOval(Color(0xFFE2F0E4), Offset(30f, 560f), Size(130f, 90f))
    drawOval(Color(0xFFE2F0E4), Offset(380f, 110f), Size(160f, 105f))
    drawOval(Color(0xFFE2F0E4), Offset(600f, 640f), Size(150f, 90f))
    // 강
    drawPath(RIVER, Color(0xFFCFE4F6), style = Stroke(18f, cap = round, join = StrokeJoin.Round))
    drawPath(RIVER2, Color(0xFFCFE4F6), style = Stroke(30f, cap = round, join = StrokeJoin.Round))
    // 마을 골목
    clipPath(TOWN_A) {
        grid(Offset(190f, 470f), 14f, 62f, 9, 3.2f, Color.White, 11, 0.22f)
        grid(Offset(190f, 470f), 104f, 78f, 8, 2.6f, Color.White, 12, 0.28f)
    }
    clipPath(TOWN_B) {
        grid(Offset(730f, 430f), -8f, 66f, 8, 3.2f, Color.White, 21, 0.22f)
        grid(Offset(730f, 430f), 82f, 80f, 7, 2.6f, Color.White, 22, 0.28f)
    }
    drawPath(COUNTRY, Color.White, style = Stroke(4f, cap = round, join = StrokeJoin.Round))
    // 큰 도로
    drawPath(HIGHWAY, Color(0xFFF0C27A), style = Stroke(16f, cap = round, join = StrokeJoin.Round))
    drawPath(HIGHWAY, Color(0xFFF9DFA8), style = Stroke(11f, cap = round, join = StrokeJoin.Round))
    drawPath(ROAD_N, Color(0xFFF3D9A4), style = Stroke(9f, cap = round, join = StrokeJoin.Round))
    drawPath(ROAD_N, Color(0xFFFBEBC8), style = Stroke(6f, cap = round, join = StrokeJoin.Round))
    drawPath(ROAD_NE, Color(0xFFF3D9A4), style = Stroke(9f, cap = round, join = StrokeJoin.Round))
    drawPath(ROAD_NE, Color(0xFFFBEBC8), style = Stroke(6f, cap = round, join = StrokeJoin.Round))
    // 경로: 지나온 길(연한 회색) + 앞으로 갈 길(파랑)
    drawPath(track.fullPath(), Color.White, style = Stroke(14f, cap = round, join = StrokeJoin.Round))
    drawPath(track.pathUpTo(dist), Color(0xFFB9C6D8), style = Stroke(8f, cap = round, join = StrokeJoin.Round))
    drawPath(track.pathFrom(dist), AyRoute, style = Stroke(8f, cap = round, join = StrokeJoin.Round))
}

private class Lm(val name: String, val p: Offset, val bold: Boolean)

private val LANDMARKS = listOf(
    Lm("평택대학교", Offset(68f, 482f), true),
    Lm("경부고속도로", Offset(352f, 230f), false),
    Lm("안성IC", Offset(425f, 445f), false),
    Lm("스타필드", Offset(312f, 462f), false),
    Lm("공도읍", Offset(728f, 470f), false),
    Lm("안성팜랜드", Offset(1008f, 497f), true)
)

fun DrawScope.label(tm: TextMeasurer, text: String, center: Offset, color: Color, size: Float, weight: FontWeight = FontWeight.Medium) {
    val r = tm.measure(text, TextStyle(color = color, fontSize = size.sp, fontWeight = weight))
    drawText(r, topLeft = Offset(center.x - r.size.width / 2f, center.y - r.size.height / 2f))
}

fun evColor(kind: Int): Color = when (kind) {
    K_TURN -> AyBlue
    K_MERGE -> AyAmber
    K_DANGER -> AyRed
    K_GAS -> Color(0xFF0E9AA7)
    else -> AyGreen
}

// 지도 전체 그리기 (회전·이동은 cam 이 정함)
fun DrawScope.drawMap(track: Track, events: List<Ev>, cam: MapCam, dist: Float, tm: TextMeasurer) {
    withTransform({
        translate(cam.anchor.x, cam.anchor.y)
        rotate(cam.phiDeg, Offset.Zero)
        scale(cam.scale, cam.scale, Offset.Zero)
        translate(-cam.center.x, -cam.center.y)
    }) { drawWorld(track, dist) }

    // 글자와 마커는 지도가 돌아가도 똑바로 보이게 화면 좌표로 그림
    val w = size.width
    val h = size.height
    LANDMARKS.forEach { lm ->
        val s = cam.toScreen(lm.p)
        if (s.x > -80f && s.x < w + 80f && s.y > -40f && s.y < h + 40f) {
            val off = if (lm.bold) 34f else 0f
            label(tm, lm.name, Offset(s.x, s.y - off), if (lm.bold) Color(0xFF1D2129) else Color(0xFF8A94A6), if (lm.bold) 15f else 12f,
                if (lm.bold) FontWeight.Bold else FontWeight.Medium)
        }
    }
    events.forEach { ev ->
        val s = cam.toScreen(track.pos(ev.at))
        if (s.x > -40f && s.x < w + 40f && s.y > -40f && s.y < h + 40f) {
            val a = if (ev.at < dist) 0.4f else 1f
            drawCircle(Color(0x26000000), 21f, Offset(s.x, s.y + 3f))
            drawCircle(Color.White.copy(alpha = a), 20f, s)
            drawCircle(evColor(ev.kind).copy(alpha = a), 16f, s)
            label(tm, ev.tag, s, Color.White.copy(alpha = a), 15f, FontWeight.Bold)
        }
    }
}
