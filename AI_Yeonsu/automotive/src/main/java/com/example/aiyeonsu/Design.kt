package com.example.aiyeonsu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =====================================================================
//  디자인 토큰 : 밝은 화이트·쿨그레이 그라데이션 + 현대 네이비 포인트 (발표자료 색감)
// =====================================================================
val AyInk = Color(0xFF1D2129)
val AySub = Color(0xFF667085)
val AyMute = Color(0xFF98A2B3)
val AyNavy = Color(0xFF002C5F)
val AySky = Color(0xFF6FA8CC)
val AyBlue = Color(0xFF1B5FD6)
val AyLine = Color(0xFFDDE3EA)
val AyGreen = Color(0xFF22A06B)
val AyAmber = Color(0xFFF2A93B)
val AyRed = Color(0xFFE5484D)
val AyPaper = Color(0xFFF1F4F8)

@Composable
fun AyBackground(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFF8F9FB), Color(0xFFE9EEF3), Color(0xFFC8D3E0)),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    ) { content() }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    radius: Dp = 28.dp,
    pad: Dp = 24.dp,
    tint: Color = Color.White.copy(alpha = 0.90f),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .shadow(16.dp, shape, clip = false, ambientColor = Color(0x14000000), spotColor = Color(0x261B3A5C))
            .clip(shape)
            .background(tint)
            .border(1.dp, Color.White, shape)
            .padding(pad),
        content = content
    )
}

@Composable
fun Tag(text: String, bg: Color = AyNavy, fg: Color = Color.White, size: Int = 13) {
    Text(
        text, color = fg, fontSize = size.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    color: Color = AyNavy,
    size: Int = 17
) {
    val shape = RoundedCornerShape(50)
    val base = modifier.clip(shape).background(if (filled) color else Color.White)
    val bordered = if (filled) base else base.border(1.5.dp, color.copy(alpha = 0.35f), shape)
    Box(
        bordered.clickable(onClick = onClick).padding(horizontal = 26.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (filled) Color.White else color, fontSize = size.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(50)).background(Color(0xFFE6EBF1)).padding(4.dp)) {
        options.forEachIndexed { i, s ->
            val sel = i == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(50))
                    .background(if (sel) Color.White else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    s, fontSize = 15.sp, color = if (sel) AyInk else AySub,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AySwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AyNavy,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFC9D2DE),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun SwitchRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AyInk)
            Text(desc, fontSize = 13.sp, color = AySub)
        }
        AySwitch(checked, onChange)
    }
}

@Composable
fun Meter(value: Float, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.height(10.dp).clip(RoundedCornerShape(50)).background(Color(0xFFE3E8EF))) {
        Box(
            Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight()
                .clip(RoundedCornerShape(50)).background(color)
        )
    }
}

@Composable
fun BigTitle(text: String, sub: String? = null) {
    Text(text, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AyInk, letterSpacing = (-1).sp, lineHeight = 46.sp)
    if (sub != null) {
        Text(sub, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AySky)
    }
}

@Composable
fun Mascot(pose: Pose, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(pose.res),
        contentDescription = "연곰",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

// 연곰이 말할 때 퍼지는 음파
@Composable
fun VoiceWave(active: Boolean, color: Color, size: Dp, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "wave")
    val p = tr.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "waveP"
    )
    Canvas(modifier.size(size)) {
        if (!active) return@Canvas
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxR = this.size.minDimension / 2f
        for (i in 0..2) {
            val f = (p.value + i / 3f) % 1f
            drawCircle(
                color = color.copy(alpha = (1f - f) * 0.45f),
                radius = maxR * (0.55f + 0.45f * f),
                center = c,
                style = Stroke(width = 3f)
            )
        }
    }
}
