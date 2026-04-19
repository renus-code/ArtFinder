package com.humber.artfinder

import com.humber.artfinder.data.model.VisitedArtwork
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtLogicTest {

    /**
     * Requirement: 
     * 1-5 photos = 10 points
     * 6-10 photos = 20 points
     * Max 20 points per artwork.
     */
    private fun calculatePoints(visitedArtworks: List<VisitedArtwork>): Int {
        var totalPoints = 0
        visitedArtworks.forEach { art ->
            val count = art.photos.size
            if (count >= 6) {
                totalPoints += 20
            } else if (count >= 1) {
                totalPoints += 10
            }
        }
        return totalPoints
    }

    private fun getBadgeForPoints(points: Int): String {
        return when {
            points <= 100 -> "Explorer"
            points <= 250 -> "Curator"
            else -> "Archivist"
        }
    }

    @Test
    fun testPointCalculation() {
        val visited = listOf(
            VisitedArtwork(id = 1, photos = listOf("p1", "p2")), // 10 pts
            VisitedArtwork(id = 2, photos = listOf("p1", "p2", "p3", "p4", "p5", "p6")), // 20 pts
            VisitedArtwork(id = 3, photos = emptyList()) // 0 pts
        )
        
        val total = calculatePoints(visited)
        assertEquals(30, total)
    }

    @Test
    fun testBadgeAssignment() {
        assertEquals("Explorer", getBadgeForPoints(50))
        assertEquals("Explorer", getBadgeForPoints(100))
        assertEquals("Curator", getBadgeForPoints(150))
        assertEquals("Curator", getBadgeForPoints(250))
        assertEquals("Archivist", getBadgeForPoints(300))
        assertEquals("Archivist", getBadgeForPoints(600))
    }
}
