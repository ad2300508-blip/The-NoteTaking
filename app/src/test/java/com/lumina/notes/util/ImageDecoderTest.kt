package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageDecoderTest {

    @Test fun noDownsampleWhenSmall() {
        assertEquals(1, ImageDecoder.sampleSize(1000, 800, maxEdge = 2048))
    }

    @Test fun downsamplesLargePhoto() {
        // 4000x3000 with maxEdge 2048 -> /2 = 2000x1500 (< 2048) -> sample 2
        assertEquals(2, ImageDecoder.sampleSize(4000, 3000, maxEdge = 2048))
    }

    @Test fun downsamplesVeryLargePhoto() {
        // 9000x6000, maxEdge 2048: /2=4500 (>=2048), /4=2250(>=2048), /8=1125 -> sample 8
        assertEquals(8, ImageDecoder.sampleSize(9000, 6000, maxEdge = 2048))
    }

    @Test fun blankPathDecodesNull() {
        assertEquals(null, ImageDecoder.decodeSampled(""))
    }
}
