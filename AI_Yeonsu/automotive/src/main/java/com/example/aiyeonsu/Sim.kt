package com.example.aiyeonsu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// =====================================================================
//  가상 주행 시뮬레이터 : 속도, 앞차 거리 등은 모두 시연용 가상 데이터입니다.
// =====================================================================
class DriveSim(val track: Track, val events: List<Ev>) {
    var dist by mutableStateOf(0f)           // 경로 위 진행 거리(픽셀)
    var speed by mutableStateOf(0f)          // km/h
    var heading by mutableStateOf(track.heading(0f))
    var curve by mutableStateOf(0f)          // 앞 도로가 휘는 정도 (-1.2 ~ 1.2)
    var t by mutableStateOf(0f)
    var frontGap by mutableStateOf(30f)      // 앞차와의 거리(m)
    var msg by mutableStateOf("")
    var pose by mutableStateOf(Pose.CLOSEUP)
    var speakUntil by mutableStateOf(0f)
    var running by mutableStateOf(true)
    var mult by mutableStateOf(6)
    var finished by mutableStateOf(false)

    // 주유소 정차
    var gasActive by mutableStateOf(false)
    var stopLeft by mutableStateOf(0f)

    // 기록
    var alerts by mutableStateOf(0)
    var maxSpeed by mutableStateOf(0f)
    var minGap by mutableStateOf(99f)
    var goodTurns by mutableStateOf(0)
    var goodMerge by mutableStateOf(0)
    var mergeFront by mutableStateOf(0)
    var mergeRear by mutableStateOf(0)
    var meters by mutableStateOf(0f)

    private val fired = mutableSetOf<Int>()
    private val praised = mutableSetOf<Int>()
    private var poseUntil = 0f
    private var lastAlertT = -99f

    val speaking: Boolean get() = t < speakUntil

    private fun announce(text: String, p: Pose, dur: Float, say: (String) -> Unit) {
        msg = text
        pose = p
        poseUntil = t + dur
        speakUntil = t + 3.5f
        say(text)
    }

    fun speakOut(text: String, p: Pose, dur: Float, out: (String) -> Unit) = announce(text, p, dur, out)

    fun step(dt: Float, gapLimit: Float, narrate: Boolean, say: (String) -> Unit) {
        if (!running || finished) return
        t += dt
        frontGap = 26f + 14f * sin(t * 0.5f) + 4f * sin(t * 1.7f)
        minGap = min(minGap, frontGap)

        // 주유소에 서 있는 동안
        if (gasActive) {
            speed += (0f - speed) * min(1f, dt * 3f)
            stopLeft -= dt
            if (stopLeft <= 0f) {
                gasActive = false
                val gas = events.firstOrNull { it.kind == K_GAS }
                if (gas != null) dist = max(dist, gas.at + 3f)
                announce("출발할게요. 천천히 가요.", Pose.CHEER, 3f, say)
            }
            return
        }

        val next = events.firstOrNull { it.at > dist }
        val toNext = if (next == null) 999f else (next.at - dist) * track.mpp
        val target = when {
            next != null && next.kind == K_TURN && toNext < 90f -> 20f
            next != null && next.kind == K_DANGER && toNext < 150f -> 30f
            next != null && next.kind == K_GAS && toNext < 120f -> 14f
            next != null && next.kind == K_ARRIVE && toNext < 100f -> 15f
            else -> 45f + 6f * sin(t * 0.15f)
        }
        speed += (target - speed) * min(1f, dt * 1.2f)
        val step = (speed / 3.6f) * dt * mult
        dist += step / track.mpp
        meters += step

        heading += normAngle(track.heading(dist) - heading) * min(1f, dt * 4f)
        val cTarget = (normAngle(track.heading(dist + 60f) - track.heading(dist - 20f)) / 0.8f).coerceIn(-1.2f, 1.2f)
        curve += (cTarget - curve) * min(1f, dt * 3f)
        maxSpeed = max(maxSpeed, speed)

        // 주유소 도착
        if (next != null && next.kind == K_GAS && toNext < 15f) {
            gasActive = true
            stopLeft = 9f
            announce("주유소에 도착했어요. 시동을 끄기 전에 순서를 알려 드릴게요.", Pose.GUIDE, 9f, say)
            return
        }

        events.forEachIndexed { i, ev ->
            // 300m 앞 안내
            val trigger = if (ev.kind == K_ARRIVE) ev.at - 25f else ev.at - 300f / track.mpp
            if (i !in fired && dist >= trigger) {
                fired.add(i)
                val text = if (narrate) ev.narr else ev.voice
                val p = if (ev.kind == K_DANGER) Pose.CAUTION else if (ev.kind == K_ARRIVE) Pose.PRAISE else Pose.CHEER
                announce(text, p, 4f, say)
            }
            // 지나간 뒤 칭찬
            if (i !in praised && ev.kind != K_ARRIVE && dist >= ev.at + 15f) {
                praised.add(i)
                if (ev.kind == K_TURN) goodTurns++
                if (ev.kind == K_MERGE) {
                    goodMerge++
                    mergeFront = frontGap.roundToInt()
                    mergeRear = (16f + 6f * sin(t)).roundToInt()
                }
                if (ev.praise.isNotEmpty() && ev.kind != K_GAS) announce(ev.praise, Pose.PRAISE, 3.5f, say)
            }
        }

        // 앞차 간격 알림 (설정한 거리보다 가까워지면)
        if (frontGap < gapLimit && t - lastAlertT > 12f) {
            lastAlertT = t
            alerts++
            announce("앞차랑 좀 가까워요. 천천히.", Pose.CAUTION, 3f, say)
        }

        if (pose != Pose.CLOSEUP && t > poseUntil) pose = Pose.CLOSEUP

        if (dist >= track.total) {
            dist = track.total
            finished = true
            speed = 0f
        }
    }

    fun result(): RunResult {
        val mins = max(1, (meters / 1000f / 40f * 60f).roundToInt())
        return RunResult(
            minutes = mins,
            km = meters / 1000f,
            alerts = alerts,
            maxSpeed = maxSpeed.roundToInt(),
            minGap = minGap.roundToInt(),
            goodTurns = goodTurns,
            goodMerge = goodMerge,
            mergeFront = if (mergeFront == 0) 18 else mergeFront,
            mergeRear = if (mergeRear == 0) 17 else mergeRear
        )
    }
}
