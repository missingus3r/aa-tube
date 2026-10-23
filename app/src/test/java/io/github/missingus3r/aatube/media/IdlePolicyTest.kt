package io.github.missingus3r.aatube.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdlePolicyTest {
    private val grace = 10 * 60_000L

    @Test fun keepsRunningWhileCarScreenIsOpen() {
        val p = IdlePolicy(grace)
        assertFalse(p.shouldStop(carVisible = true, playing = false, nowMs = 0))
        assertFalse(p.shouldStop(carVisible = true, playing = false, nowMs = grace * 5))
    }

    @Test fun keepsRunningWhilePlaying() {
        val p = IdlePolicy(grace)
        assertFalse(p.shouldStop(carVisible = false, playing = true, nowMs = 0))
        assertFalse(p.shouldStop(carVisible = false, playing = true, nowMs = grace * 5))
    }

    @Test fun pauseWhileDrivingSurvivesUntilGraceEnds() {
        val p = IdlePolicy(grace)
        p.shouldStop(carVisible = false, playing = true, nowMs = 0)
        assertFalse(p.shouldStop(carVisible = false, playing = false, nowMs = 1_000))
        assertFalse(p.shouldStop(carVisible = false, playing = false, nowMs = 1_000 + grace - 1))
        assertTrue(p.shouldStop(carVisible = false, playing = false, nowMs = 1_000 + grace))
    }

    @Test fun resumingResetsTheIdleClock() {
        val p = IdlePolicy(grace)
        p.shouldStop(carVisible = false, playing = false, nowMs = 0)
        p.shouldStop(carVisible = false, playing = true, nowMs = grace - 1)
        assertFalse(p.shouldStop(carVisible = false, playing = false, nowMs = grace))
        assertTrue(p.shouldStop(carVisible = false, playing = false, nowMs = grace * 2))
    }
}
