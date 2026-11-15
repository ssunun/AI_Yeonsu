package com.example.aiyeonsu

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.rememberTextMeasurer
import kotlin.math.min

// 주행 중 지도: 차는 같은 자리에 있고, 지도가 진행 방향이 위를 향하도록 돌면서 움직입니다.
@Composable
fun DriveMap(track: Track, events: List<Ev>, dist: Float, heading: Float, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val anchor = Offset(size.width / 2f, size.height * 0.72f)
        val cam = MapCam(track.pos(dist), -heading * 180f / PIF, 2.2f, anchor)
        drawMap(track, events, cam, dist, tm)

        // 내 차 위치 표시
        drawCircle(Color(0x331B5FD6), 46f, anchor)
        drawCircle(Color(0x221B5FD6), 70f, anchor)
        val arrow = Path().apply {
            moveTo(anchor.x, anchor.y - 30f)
            lineTo(anchor.x - 22f, anchor.y + 22f)
            lineTo(anchor.x, anchor.y + 10f)
            lineTo(anchor.x + 22f, anchor.y + 22f)
            close()
        }
        drawPath(arrow, Color.White, style = Stroke(width = 12f, join = StrokeJoin.Round))
        drawPath(arrow, AyNavy)
    }
}

// 출발 전 미리보기 지도: 코스 전체가 한눈에 보입니다.
@Composable
fun PreviewMap(track: Track, events: List<Ev>, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val sc = min(size.width / 1020f, size.height / 330f)
        val cam = MapCam(Offset(540f, 385f), 0f, sc, Offset(size.width / 2f, size.height / 2f))
        drawMap(track, events, cam, 0f, tm)
    }
}
