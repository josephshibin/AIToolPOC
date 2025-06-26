package com.example.aitoolpoc.data

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a diary note
 */
data class DiaryNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long, // Unix timestamp in milliseconds
    val imageId: String? = null // Optional image ID from API
) {
    /**
     * Format timestamp for display (e.g., "11:23 am")
     */
    fun getFormattedTime(): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Get date for grouping (e.g., "Today", "17th Aug")
     */
    fun getDateGroup(): String {
        val noteDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val today = Calendar.getInstance()

        return when {
            noteDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            noteDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            else -> {
                val dayFormat = SimpleDateFormat("d'th' MMM", Locale.getDefault())
                dayFormat.format(Date(timestamp))
            }
        }
    }
    
    /**
     * Get preview of content (truncated)
     */
    fun getContentPreview(maxLength: Int = 50): String {
        return if (content.length > maxLength) {
            "${content.take(maxLength)}..."
        } else {
            content
        }
    }
}

/**
 * Sample data for testing (will be replaced with API data)
 */
object SampleDiaryData {
    fun getSampleNotes(): List<DiaryNote> {
        val now = System.currentTimeMillis()
        return listOf(
            DiaryNote(
                id = "sample1",
                title = "Mode changing",
                content = "Discomfort in the head, scalp, or neck, and is often associated with muscle tension.",
                timestamp = now - (1 * 60 * 60 * 1000) // 1 hour ago
            ),
            DiaryNote(
                id = "sample2",
                title = "To much paining in head",
                content = "Today after taking cancer medicine my head and neck area is paining a lot.",
                timestamp = now - (3 * 60 * 60 * 1000) // 3 hours ago
            ),
            DiaryNote(
                id = "sample3",
                title = "Headache",
                content = "Today after taking cancer medicine my head and neck area is paining a lot.",
                timestamp = now - (5 * 60 * 60 * 1000) // 5 hours ago
            ),
            DiaryNote(
                id = "sample4",
                title = "Paining Head",
                content = "Discomfort in the head, scalp, or neck, and is often associated with muscle tension.",
                timestamp = now - (26 * 60 * 60 * 1000) // 26 hours ago (yesterday)
            )
        )
    }
}
