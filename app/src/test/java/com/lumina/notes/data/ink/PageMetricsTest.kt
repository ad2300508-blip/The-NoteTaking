package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Test

class PageMetricsTest {

    @Test fun emptyCanvasIsAtLeastMinPages() {
        assertEquals(1000f, PageMetrics.canvasHeight(0f, pageHeight = 1000f, minPages = 1), 0.001f)
        assertEquals(2000f, PageMetrics.canvasHeight(0f, pageHeight = 1000f, minPages = 2), 0.001f)
    }

    @Test fun growsToWholePages() {
        // content bottom 1500 within 1000-tall pages -> 2 pages
        assertEquals(2000f, PageMetrics.canvasHeight(1500f, pageHeight = 1000f), 0.001f)
    }

    @Test fun exactPageBoundaryStaysOnePage() {
        assertEquals(1000f, PageMetrics.canvasHeight(1000f, pageHeight = 1000f), 0.001f)
    }

    @Test fun growMarginAddsPageEarly() {
        // bottom 950 + margin 100 = 1050 -> 2 pages
        assertEquals(2000f, PageMetrics.canvasHeight(950f, pageHeight = 1000f, growMargin = 100f), 0.001f)
    }

    @Test fun pageCountMatches() {
        assertEquals(3, PageMetrics.pageCount(3000f, 1000f))
        assertEquals(1, PageMetrics.pageCount(0f, 1000f))
    }
}
