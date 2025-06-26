package com.example.aitoolpoc.utils

import com.example.aitoolpoc.data.DiaryNote
import com.example.aitoolpoc.repository.NoteResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date formatting and note grouping
 */
object DateUtils {
    
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    
    /**
     * Convert timestamp (milliseconds) to formatted time string
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * Convert timestamp (milliseconds) to formatted date string
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Get day from timestamp
     */
    fun getDay(timestamp: Long): String {
        return dayFormat.format(Date(timestamp))
    }
    
    /**
     * Get month from timestamp
     */
    fun getMonth(timestamp: Long): String {
        return monthFormat.format(Date(timestamp))
    }
    
    /**
     * Get date group label (Today, Yesterday, or specific date)
     */
    fun getDateGroupLabel(timestamp: Long): String {
        val noteDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return when {
            noteDate.timeInMillis == today.timeInMillis -> "Today"
            noteDate.timeInMillis == yesterday.timeInMillis -> "Yesterday"
            else -> "${getDay(timestamp)} ${getMonth(timestamp)}"
        }
    }
    
    /**
     * Convert NoteResponse to DiaryNote
     */
    fun convertToDialyNote(noteResponse: NoteResponse): DiaryNote {
        return DiaryNote(
            id = noteResponse.id,
            title = noteResponse.title,
            content = noteResponse.description,
            timestamp = noteResponse.createdAt,
            imageId = noteResponse.imageId
        )
    }
    
    /**
     * Group notes by date and return grouped map
     */
    fun groupNotesByDate(notes: List<NoteResponse>): Map<String, List<DiaryNote>> {
        android.util.Log.d("DateUtils", "groupNotesByDate called with ${notes.size} notes")
        android.util.Log.d("DateUtils", "Input notes: $notes")

        val diaryNotes = notes.map { noteResponse ->
            android.util.Log.d("DateUtils", "Converting note: ${noteResponse.id} - ${noteResponse.title}")
            convertToDialyNote(noteResponse)
        }

        android.util.Log.d("DateUtils", "Converted to ${diaryNotes.size} DiaryNotes")
        android.util.Log.d("DateUtils", "DiaryNotes: $diaryNotes")

        val groupedNotes = diaryNotes
            .sortedByDescending { it.timestamp } // Sort by newest first
            .groupBy { note ->
                val dateLabel = getDateGroupLabel(note.timestamp)
                android.util.Log.d("DateUtils", "Note ${note.id} (${note.title}) grouped under: $dateLabel")
                dateLabel
            }
            .toSortedMap { date1, date2 ->
                // Custom sorting: Today first, then Yesterday, then chronological
                when {
                    date1 == "Today" -> -1
                    date2 == "Today" -> 1
                    date1 == "Yesterday" -> -1
                    date2 == "Yesterday" -> 1
                    else -> date2.compareTo(date1) // Reverse chronological for other dates
                }
            }

        android.util.Log.d("DateUtils", "Final grouped notes: $groupedNotes")
        android.util.Log.d("DateUtils", "Number of date groups: ${groupedNotes.size}")

        return groupedNotes
    }
    
    /**
     * Check if timestamp is today
     */
    fun isToday(timestamp: Long): Boolean {
        val noteDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val today = Calendar.getInstance()
        
        return noteDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                noteDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if timestamp is yesterday
     */
    fun isYesterday(timestamp: Long): Boolean {
        val noteDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return noteDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                noteDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Get relative time description (e.g., "2 hours ago", "Yesterday")
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days}d ago"
            else -> formatDate(timestamp)
        }
    }
}
