package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {

    private val now = 1_700_000_000_000L

    @Test fun justNow() {
        assertEquals("adesso", RelativeTime.format(now - 5_000, now))
    }

    @Test fun minutes() {
        assertEquals("5 min fa", RelativeTime.format(now - 5 * 60_000, now))
    }

    @Test fun hours() {
        assertEquals("3 h fa", RelativeTime.format(now - 3 * 3_600_000, now))
    }

    @Test fun yesterday() {
        assertEquals("ieri", RelativeTime.format(now - 25 * 3_600_000, now))
    }

    @Test fun days() {
        assertEquals("3 g fa", RelativeTime.format(now - 3 * 24 * 3_600_000L, now))
    }

    @Test fun futureClampsToNow() {
        assertEquals("adesso", RelativeTime.format(now + 10_000, now))
    }
}
