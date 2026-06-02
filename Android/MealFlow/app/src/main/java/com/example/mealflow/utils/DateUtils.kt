// File: app/src/main/java/com/example/mealflow/utils/DateUtils.kt
package com.example.mealflow.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object DateUtils {
    // Fixed date format constants to ensure consistency across the app
    private const val ISO_DATE_FORMAT = "yyyy-MM-dd"
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
    private val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())

    /**
     * Generate a list of dates starting from the given date
     */
    fun generatePlanDates(startDateString: String, daysCount: Int): List<String> {
        return try {
            val startDate = LocalDate.parse(startDateString, dateFormatter)
            (0 until daysCount).map {
                startDate.plusDays(it.toLong()).format(dateFormatter)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Format date string to a human-readable format with day name
     */
    fun formatDateWithDay(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            // Example: "Mon, Jan 1, 2025" or customize as needed
            date.format(dayMonthYearFormatter)
        } catch (e: Exception) {
            dateString // fallback
        }
    }

    /**
     * Format a LocalDate to month and year format
     */
    fun formatMonth(date: LocalDate): String {
        return try {
            date.format(monthYearFormatter)
        } catch (e: Exception) {
            "Error"
        }
    }

    /**
     * Parse a date string to LocalDate
     */
    fun parseLocalDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format a LocalDate to ISO format (YYYY-MM-DD)
     */
    fun formatToISO(date: LocalDate): String {
        return date.format(dateFormatter)
    }

    /**
     * Get the current date as a string in ISO format
     */
    fun getCurrentDate(): String {
        return formatToISO(LocalDate.now())
    }

    /**
     * Extract just the day number from a date string
     */
    fun getDayOfMonth(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.format(dayOfMonthFormatter)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Check if a date is today
     */
    fun isToday(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.isEqual(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the first day of the given month and year
     */
    fun getFirstDayOfMonth(year: Int, month: Int): LocalDate {
        return LocalDate.of(year, month, 1)
    }

    /**
     * Get all dates for a specific calendar week within a month (based on day of month)
     * Week 1: days 1-7, Week 2: days 8-14, etc.
     */
    fun getDatesForWeek(year: Int, month: Int, weekNumber: Int): List<String> {
        try {
            val yearMonth = YearMonth.of(year, month)
            val daysInMonth = yearMonth.lengthOfMonth()
            
            // Calculate the start day of the selected week
            val firstDayOfWeek = ((weekNumber - 1) * 7) + 1
            
            // Calculate the end day of the selected week
            val lastDayOfWeek = Math.min(firstDayOfWeek + 6, daysInMonth)
            
            // Create a list of dates for the selected week
            return (firstDayOfWeek..lastDayOfWeek).map { day ->
                val date = LocalDate.of(year, month, day)
                formatToISO(date)
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }

    /**
     * Get the first Monday of the calendar week view for a month
     */
    fun getFirstMondayOfMonthView(year: Int, month: Int): LocalDate {
        val firstDayOfMonth = getFirstDayOfMonth(year, month)
        return firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * Get the last Sunday of the calendar week view for a month
     */
    fun getLastSundayOfMonthView(year: Int, month: Int): LocalDate {
        val yearMonth = YearMonth.of(year, month)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        return lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    /**
     * Calculate the number of calendar weeks needed to display a month
     * Week 1: days 1-7, Week 2: days 8-14, etc.
     */
    fun getNumberOfWeeksInMonthView(year: Int, month: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        // Calculate number of weeks based on days in month
        // Week 1: days 1-7, Week 2: days 8-14, etc.
        return Math.ceil(daysInMonth / 7.0).toInt()
    }

    /**
     * Get the calendar week number for a specific date within a month view
     * Week 1: days 1-7, Week 2: days 8-14, etc.
     */
    fun getCalendarWeekNumber(date: LocalDate, year: Int, month: Int): Int {
        // Ensure the date is in the specified month
        if (date.year != year || date.monthValue != month) {
            return 1 // Default to first week if date is outside the month
        }
        
        // Calculate week number based on day of month
        // Week 1: days 1-7, Week 2: days 8-14, etc.
        val dayOfMonth = date.dayOfMonth
        return ((dayOfMonth - 1) / 7) + 1
    }

    /**
     * Get the first day of a specific week in a month
     * Week 1: days 1-7, Week 2: days 8-14, etc.
     */
    private fun getFirstDayOfWeek(year: Int, month: Int, weekNumber: Int): LocalDate {
        // Calculate the first day of the selected week
        val firstDayOfWeek = ((weekNumber - 1) * 7) + 1
        
        // Ensure we don't exceed the days in the month
        val yearMonth = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        val actualDay = Math.min(firstDayOfWeek, daysInMonth)
        return LocalDate.of(year, month, actualDay)
    }

    /**
     * Get the number of days in a month
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return YearMonth.of(year, month).lengthOfMonth()
    }

    /**
     * Get the day name (e.g., "Monday") from a date string
     */
    fun getDayName(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the short day name (e.g., "Mon") from a date string
     */
    fun getShortDayName(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the calendar week number within a month for a specific date string
     */
    fun getWeekOfMonth(dateString: String): Int {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            getCalendarWeekNumber(date, date.year, date.monthValue)
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Get the calendar week number within a month for a LocalDate
     */
    fun getWeekOfMonth(date: LocalDate): Int {
        return getCalendarWeekNumber(date, date.year, date.monthValue)
    }

    /**
     * Check if a date falls within the current calendar week (Monday to Sunday)
     */
    fun isCurrentWeek(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            val today = LocalDate.now()
            
            // Get the Monday of the week containing today
            val startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            // Get the Sunday of the week containing today
            val endOfCurrentWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            
            !date.isBefore(startOfCurrentWeek) && !date.isAfter(endOfCurrentWeek)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if a LocalDate falls within the current calendar week
     */
    fun isCurrentWeek(date: LocalDate): Boolean {
        val today = LocalDate.now()
        val startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfCurrentWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        return !date.isBefore(startOfCurrentWeek) && !date.isAfter(endOfCurrentWeek)
    }

    /**
     * Format a date as a short date string (e.g., "Jan 1")
     */
    fun formatShortDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            date.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Get week range string for display (e.g., "Dec 30 - Jan 5")
     */
    fun getWeekRangeString(year: Int, month: Int, weekNumber: Int): String {
        return try {
            val dates = getDatesForWeek(year, month, weekNumber)
            if (dates.isEmpty()) return ""
            
            val startDate = parseLocalDate(dates.first())
            val endDate = parseLocalDate(dates.last())
            
            if (startDate == null || endDate == null) return ""
            
            val startFormat = if (startDate.year != endDate.year) {
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            } else if (startDate.month != endDate.month) {
                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            } else {
                DateTimeFormatter.ofPattern("d", Locale.getDefault())
            }
            
            val endFormat = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            
            "${startDate.format(startFormat)} - ${endDate.format(endFormat)}"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Check if a date belongs to the specified month
     */
    fun isDateInMonth(dateString: String, year: Int, month: Int): Boolean {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.year == year && date.monthValue == month
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the current week number for today in the current month view
     */
    fun getCurrentWeekNumber(): Int {
        val today = LocalDate.now()
        return getCalendarWeekNumber(today, today.year, today.monthValue)
    }

    /**
     * Check if a specific week contains today's date
     */
    fun weekContainsToday(year: Int, month: Int, weekNumber: Int): Boolean {
        val today = LocalDate.now()
        val weekDates = getDatesForWeek(year, month, weekNumber)
        val todayString = formatToISO(today)
        return weekDates.contains(todayString)
    }

    /**
     * Format date string to a concise format (day and month only)
     * Example: "2 Jun" or "15 Dec"
     */
    fun formatDateConcise(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            // Format as "2 Jun" - day and abbreviated month name
            date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
        } catch (e: Exception) {
            dateString // fallback
        }
    }

    /**
     * Format a date range from two ISO date strings
     * Example: "Jan 1 - Jan 7, 2025" or "Jan 28 - Feb 3, 2025"
     */
    fun formatDateRange(startDateStr: String, endDateStr: String): String {
        return try {
            val startDate = parseLocalDate(startDateStr)
            val endDate = parseLocalDate(endDateStr)
            
            if (startDate == null || endDate == null) return "$startDateStr - $endDateStr"
            
            val startFormat = if (startDate.year != endDate.year) {
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            } else if (startDate.month != endDate.month) {
                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            } else {
                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            }
            
            val endFormat = if (startDate.year != endDate.year) {
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            } else {
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            }
            
            "${startDate.format(startFormat)} - ${endDate.format(endFormat)}"
        } catch (e: Exception) {
            "$startDateStr - $endDateStr" // fallback
        }
    }
}