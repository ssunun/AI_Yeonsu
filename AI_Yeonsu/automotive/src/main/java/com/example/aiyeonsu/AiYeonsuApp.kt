package com.example.aiyeonsu

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiYeonsuApp() {
    val ctx = LocalContext.current
    val voice = remember { Voice(ctx) }
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }

    val settings = remember { Settings() }
    val track = remember { Track(ROUTE) }
    val events = remember { buildEvents(track) }
    var tab by remember { mutableStateOf(Tab.PRE) }
    var result by remember { mutableStateOf<RunResult?>(null) }
    var driveKey by remember { mutableStateOf(0) }

    AyBackground {
        Column(Modifier.fillMaxSize()) {
            // ---- 상단 바
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mascot(Pose.STANDING, 48.dp)
                Spacer(Modifier.width(10.dp))
                Text("나의 AI 연수 아저씨", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AyInk, letterSpacing = (-0.5).sp)
                Spacer(Modifier.weight(1f))
                if (tab != Tab.DRIVE) {
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.85f)).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(Tab.PRE, Tab.REVIEW, Tab.GROWTH).forEach { t ->
                            val sel = t == tab
                            Text(
                                t.label, fontSize = 16.sp,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (sel) Color.White else AySub,
                                modifier = Modifier.clip(RoundedCornerShape(50))
                                    .background(if (sel) AyNavy else Color.Transparent)
                                    .clickable { tab = t }
                                    .padding(horizontal = 22.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                if (tab == Tab.DRIVE) {
                    Text("주행 중에는 화면 조작 없이 음성으로 안내해요", fontSize = 14.sp, color = AySub, modifier = Modifier.padding(end = 12.dp))
                    Tag("D 주행 중", AyNavy)
                } else {
                    Tag("P 주차 중", AyGreen)
                }
            }

            // ---- 본문
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (tab) {
                    Tab.PRE -> PreDriveScreen(track, events, settings) {
                        driveKey++
                        tab = Tab.DRIVE
                    }
                    Tab.DRIVE -> key(driveKey) {
                        DriveScreen(track, events, settings, voice) { r ->
                            result = r
                            tab = Tab.REVIEW
                        }
                    }
                    Tab.REVIEW -> ReviewScreen(result, settings, { tab = Tab.PRE }, { tab = Tab.GROWTH })
                    Tab.GROWTH -> GrowthScreen(settings)
                }
            }
        }
    }
}
