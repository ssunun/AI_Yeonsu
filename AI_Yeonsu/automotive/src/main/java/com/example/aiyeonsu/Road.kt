package com.example.aiyeonsu

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max
import kotlin.math.sin

// 왼쪽 가상 3D 도로 화면 (원근감을 준 2D 그림 : 거리만큼 차선과 가로수가 다가옵니다)
@Composable
fun RoadView(distM: Float, curve: Float, frontGap: Float, t: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val hy = h * 0.34f

        // 하늘과 땅
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFFD6E4F2), Color(0xFFF6F9FC)), startY = 0f, endY = hy),
            Offset.Zero, Size(w, hy)
        )
        drawRect(Color(0xFFE2E9F0), Offset(0f, hy - 12f), Size(w, 12f))
        drawRect(Color(0xFFEDF1F5), Offset(0f, hy), Size(w, h - hy))

        fun zOf(s: Float) = 1f + s * 0.07f
        fun yOf(z: Float) = hy + (h - hy) / z
        fun cxOf(z: Float) = w / 2f + curve * (z - 1f) * w * 0.011f
        fun halfOf(z: Float) = w * 0.46f / z

        // 도로
        val zs = listOf(1f, 1.5f, 2f, 3f, 4f, 6f, 9f, 14f, 20f, 40f)
        val road = Path()
        zs.forEachIndexed { i, z ->
            val x = cxOf(z) - halfOf(z)
            if (i == 0) road.moveTo(x, yOf(z)) else road.lineTo(x, yOf(z))
        }
        zs.reversed().forEach { z -> road.lineTo(cxOf(z) + halfOf(z), yOf(z)) }
        road.close()
        drawPath(road, Color(0xFFC3CBD6))

        // 가로수 (멀리 있는 것부터)
        for (k in 9 downTo 0) {
            val s = k * 28f - (distM % 28f)
            if (s < 4f) continue
            val z = zOf(s)
            val u = 80f / z
            for (side in listOf(-1f, 1f)) {
                val x = cxOf(z) + side * halfOf(z) * 1.55f
                val y = yOf(z)
                drawOval(Color(0x1F000000), Offset(x - u * 0.4f, y - u * 0.06f), Size(u * 0.8f, u * 0.14f))
                drawRect(Color(0xFFA78F76), Offset(x - u * 0.07f, y - u * 0.5f), Size(u * 0.14f, u * 0.5f))
                drawCircle(Color(0xFF93C9A6), u * 0.46f, Offset(x, y - u * 0.98f))
                drawCircle(Color(0xFFB4DCC2), u * 0.26f, Offset(x - u * 0.12f, y - u * 1.1f))
            }
        }

        // 차선 점선 (거리만큼 흘러오는 느낌)
        for (k in 0..24) {
            val s0 = k * 12f - (distM % 12f)
            val s1 = s0 + 5f
            if (s1 < 0.5f) continue
            val a = zOf(max(s0, 0.5f))
            val b = zOf(s1)
            for (o in listOf(-1f / 3f, 1f / 3f)) {
                drawLine(
                    Color.White,
                    Offset(cxOf(a) + o * halfOf(a), yOf(a)),
                    Offset(cxOf(b) + o * halfOf(b), yOf(b)),
                    strokeWidth = max(2f, 10f / a)
                )
            }
        }
        // 도로 가장자리 실선
        for (side in listOf(-1f, 1f)) {
            val p = Path()
            zs.forEachIndexed { i, z ->
                val x = cxOf(z) + side * halfOf(z) * 0.97f
                if (i == 0) p.moveTo(x, yOf(z)) else p.lineTo(x, yOf(z))
            }
            drawPath(p, Color.White, style = Stroke(width = 6f))
        }

        fun car(s: Float, o: Float, body: Color) {
            val z = zOf(s)
            val cw = w * 0.17f / z
            val ch = cw * 0.78f
            val x = cxOf(z) + o * halfOf(z)
            val y = yOf(z)
            drawOval(Color(0x26000000), Offset(x - cw * 0.62f, y - ch * 0.1f), Size(cw * 1.24f, ch * 0.22f))
            drawRoundRect(body, Offset(x - cw / 2f, y - ch), Size(cw, ch), CornerRadius(cw * 0.2f, cw * 0.2f))
            drawRoundRect(Color(0x99FFFFFF), Offset(x - cw * 0.36f, y - ch * 0.86f), Size(cw * 0.72f, ch * 0.34f), CornerRadius(cw * 0.08f, cw * 0.08f))
            drawRoundRect(Color(0xFFE5484D), Offset(x - cw * 0.46f, y - ch * 0.34f), Size(cw * 0.18f, ch * 0.12f), CornerRadius(3f, 3f))
            drawRoundRect(Color(0xFFE5484D), Offset(x + cw * 0.28f, y - ch * 0.34f), Size(cw * 0.18f, ch * 0.12f), CornerRadius(3f, 3f))
        }
        car(130f, 2f / 3f, Color(0xFFDDE3EA))
        car(75f + 25f * sin(t * 0.2f), -2f / 3f, Color(0xFFF1D58F))
        car(frontGap.coerceAtLeast(5f), 0f, Color(0xFFE9EDF2))

        // 내 차 (뒷모습, 그래파이트 + 가는 LED 테일램프)
        val mw = w * 0.23f
        val mh = mw * 0.74f
        val mx = w / 2f
        val my = h * 0.80f
        drawOval(Color(0x33000000), Offset(mx - mw * 0.62f, my - mh * 0.08f), Size(mw * 1.24f, mh * 0.2f))
        drawRoundRect(Color(0xFF2B313B), Offset(mx - mw / 2f, my - mh), Size(mw, mh), CornerRadius(mw * 0.2f, mw * 0.2f))
        drawRoundRect(Color(0xFF10151B), Offset(mx - mw * 0.38f, my - mh * 0.9f), Size(mw * 0.76f, mh * 0.34f), CornerRadius(mw * 0.08f, mw * 0.08f))
        drawRoundRect(Color(0xFFFF4D4D), Offset(mx - mw * 0.44f, my - mh * 0.42f), Size(mw * 0.88f, mh * 0.05f), CornerRadius(4f, 4f))
        drawCircle(Color(0x55FF4D4D), mw * 0.07f, Offset(mx - mw * 0.42f, my - mh * 0.4f))
        drawCircle(Color(0x55FF4D4D), mw * 0.07f, Offset(mx + mw * 0.42f, my - mh * 0.4f))
    }
}
