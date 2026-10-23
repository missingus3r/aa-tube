package io.github.missingus3r.aatube.media

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import io.github.missingus3r.aatube.player.PlayerCmd
import io.github.missingus3r.aatube.player.PlayerControls
import io.github.missingus3r.aatube.player.PlayerState

class KeyMapTest {
    @Test fun mapsAllCallbacks() {
        assertEquals(PlayerCmd.TOGGLE, KeyMap.toCmd("play"))
        assertEquals(PlayerCmd.TOGGLE, KeyMap.toCmd("pause"))
        assertEquals(PlayerCmd.NEXT, KeyMap.toCmd("skipToNext"))
        assertEquals(PlayerCmd.PREV, KeyMap.toCmd("skipToPrevious"))
        assertEquals(PlayerCmd.FWD10, KeyMap.toCmd("fastForward"))
        assertEquals(PlayerCmd.BACK10, KeyMap.toCmd("rewind"))
    }

    @Test fun unknownIsNull() = assertNull(KeyMap.toCmd("stop"))

    @Test fun commandsBeforeReadyAreDropped() {
        val fake = FakeControls(PlayerState(ready = false))
        MediaDispatcher(fake).dispatch("play")
        assertTrue(fake.sent.isEmpty())
    }

    @Test fun commandsWhenReadyAreSent() {
        val fake = FakeControls(PlayerState(ready = true))
        MediaDispatcher(fake).dispatch("skipToNext")
        assertEquals(listOf(PlayerCmd.NEXT), fake.sent)
    }

    @Test fun seekWhenReadyIsSent() {
        val fake = FakeControls(PlayerState(ready = true))
        MediaDispatcher(fake).seekTo(95_000)
        assertEquals(listOf(95_000L), fake.seeks)
    }

    @Test fun seekBeforeReadyIsDropped() {
        val fake = FakeControls(PlayerState(ready = false))
        MediaDispatcher(fake).seekTo(95_000)
        assertTrue(fake.seeks.isEmpty())
    }

    private class FakeControls(initial: PlayerState) : PlayerControls {
        override val state = MutableStateFlow(initial)
        val sent = mutableListOf<PlayerCmd>()
        val seeks = mutableListOf<Long>()
        override fun send(cmd: PlayerCmd) { sent += cmd }
        override fun seekTo(ms: Long) { seeks += ms }
    }
}
