package com.example.mealflow

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mealflow.utils.DateUtils
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
class DateUtilsWeekCalculationTest {

    @Test
    fun testGetNumberOfWeeksInMonthView_January2024() {
        // January 2024: starts on Monday, ends on Wednesday
        // Should need 5 weeks to display full month view
        val weeks = DateUtils.getNumberOfWeeksInMonthView(2024, 1)
        assertEquals(5, weeks)
    }

    @Test
    fun testGetNumberOfWeeksInMonthView_February2024() {
        // February 2024: starts on Thursday, ends on Thursday (leap year)
        // Should need 5 weeks to display full month view
        val weeks = DateUtils.getNumberOfWeeksInMonthView(2024, 2)
        assertEquals(5, weeks)
    }

    @Test
    fun testGetNumberOfWeeksInMonthView_March2024() {
        // March 2024: starts on Friday, ends on Sunday
        // Should need 6 weeks to display full month view
        val weeks = DateUtils.getNumberOfWeeksInMonthView(2024, 3)
        assertEquals(6, weeks)
    }

    @Test
    fun testGetFirstMondayOfMonthView() {
        // Test January 2024 - first day is Monday, Jan 1
        val firstMonday = DateUtils.getFirstMondayOfMonthView(2024, 1)
        assertEquals(LocalDate.of(2024, 1, 1), firstMonday)

        // Test February 2024 - first day is Thursday, so first Monday should be Jan 29
        val firstMondayFeb = DateUtils.getFirstMondayOfMonthView(2024, 2)
        assertEquals(LocalDate.of(2024, 1, 29), firstMondayFeb)

        // Test March 2024 - first day is Friday, so first Monday should be Feb 26
        val firstMondayMar = DateUtils.getFirstMondayOfMonthView(2024, 3)
        assertEquals(LocalDate.of(2024, 2, 26), firstMondayMar)
    }

    @Test
    fun testGetLastSundayOfMonthView() {
        // Test January 2024 - last day is Wednesday, so last Sunday should be Feb 4
        val lastSunday = DateUtils.getLastSundayOfMonthView(2024, 1)
        assertEquals(LocalDate.of(2024, 2, 4), lastSunday)

        // Test February 2024 - last day is Thursday, so last Sunday should be Mar 3
        val lastSundayFeb = DateUtils.getLastSundayOfMonthView(2024, 2)
        assertEquals(LocalDate.of(2024, 3, 3), lastSundayFeb)
    }

    @Test
    fun testGetCalendarWeekNumber() {
        // Test various dates in January 2024
        // Jan 1 (Monday) should be week 1
        assertEquals(1, DateUtils.getCalendarWeekNumber(LocalDate.of(2024, 1, 1), 2024, 1))
        
        // Jan 7 (Sunday) should be week 1
        assertEquals(1, DateUtils.getCalendarWeekNumber(LocalDate.of(2024, 1, 7), 2024, 1))
        
        // Jan 8 (Monday) should be week 2
        assertEquals(2, DateUtils.getCalendarWeekNumber(LocalDate.of(2024, 1, 8), 2024, 1))
        
        // Jan 31 (Wednesday) should be week 5
        assertEquals(5, DateUtils.getCalendarWeekNumber(LocalDate.of(2024, 1, 31), 2024, 1))
    }

    @Test
    fun testGetDatesForWeek() {
        // Test week 1 of January 2024
        val week1Dates = DateUtils.getDatesForWeek(2024, 1, 1)
        assertEquals(7, week1Dates.size)
        assertEquals("2024-01-01", week1Dates[0]) // Monday
        assertEquals("2024-01-07", week1Dates[6]) // Sunday

        // Test week 2 of January 2024
        val week2Dates = DateUtils.getDatesForWeek(2024, 1, 2)
        assertEquals(7, week2Dates.size)
        assertEquals("2024-01-08", week2Dates[0]) // Monday
        assertEquals("2024-01-14", week2Dates[6]) // Sunday
    }

    @Test
    fun testGetWeekRangeString() {
        // Test week 1 of January 2024
        val range1 = DateUtils.getWeekRangeString(2024, 1, 1)
        assertEquals("1 - Jan 7", range1)

        // Test a week that spans months (last week of January 2024)
        val range5 = DateUtils.getWeekRangeString(2024, 1, 5)
        assertEquals("Jan 29 - Feb 4", range5)
    }

    @Test
    fun testIsDateInMonth() {
        assertTrue(DateUtils.isDateInMonth("2024-01-15", 2024, 1))
        assertFalse(DateUtils.isDateInMonth("2024-02-15", 2024, 1))
        assertTrue(DateUtils.isDateInMonth("2024-01-01", 2024, 1))
        assertTrue(DateUtils.isDateInMonth("2024-01-31", 2024, 1))
    }

    @Test
    fun testIsCurrentWeek() {
        val today = LocalDate.now()
        val todayString = DateUtils.formatToISO(today)
        
        // Today should be in current week
        assertTrue(DateUtils.isCurrentWeek(todayString))
        assertTrue(DateUtils.isCurrentWeek(today))
        
        // A date from last week should not be in current week
        val lastWeek = today.minusWeeks(1)
        val lastWeekString = DateUtils.formatToISO(lastWeek)
        assertFalse(DateUtils.isCurrentWeek(lastWeekString))
        assertFalse(DateUtils.isCurrentWeek(lastWeek))
    }

    @Test
    fun testWeekContainsToday() {
        val today = LocalDate.now()
        val currentYear = today.year
        val currentMonth = today.monthValue
        val currentWeek = DateUtils.getCalendarWeekNumber(today, currentYear, currentMonth)
        
        // Current week should contain today
        assertTrue(DateUtils.weekContainsToday(currentYear, currentMonth, currentWeek))
        
        // Other weeks should not contain today (assuming we're not at month boundary)
        if (currentWeek > 1) {
            assertFalse(DateUtils.weekContainsToday(currentYear, currentMonth, currentWeek - 1))
        }
        
        val maxWeeks = DateUtils.getNumberOfWeeksInMonthView(currentYear, currentMonth)
        if (currentWeek < maxWeeks) {
            assertFalse(DateUtils.weekContainsToday(currentYear, currentMonth, currentWeek + 1))
        }
    }

    @Test
    fun testGetCurrentWeekNumber() {
        val today = LocalDate.now()
        val expectedWeek = DateUtils.getCalendarWeekNumber(today, today.year, today.monthValue)
        val actualWeek = DateUtils.getCurrentWeekNumber()
        
        assertEquals(expectedWeek, actualWeek)
    }

    @Test
    fun testConsistencyBetweenMethods() {
        // Test that different methods return consistent results
        val year = 2024
        val month = 3 // March 2024 - interesting case with 6 weeks
        
        val numberOfWeeks = DateUtils.getNumberOfWeeksInMonthView(year, month)
        
        // Each week should return exactly 7 dates
        for (weekNum in 1..numberOfWeeks) {
            val dates = DateUtils.getDatesForWeek(year, month, weekNum)
            assertEquals(7, dates.size)
            
            // First date should be Monday, last should be Sunday
            val firstDate = DateUtils.parseLocalDate(dates[0])
            val lastDate = DateUtils.parseLocalDate(dates[6])
            
            assertNotNull(firstDate)
            assertNotNull(lastDate)
            
            assertEquals(java.time.DayOfWeek.MONDAY, firstDate!!.dayOfWeek)
            assertEquals(java.time.DayOfWeek.SUNDAY, lastDate!!.dayOfWeek)
            
            // Week range string should not be empty
            val rangeString = DateUtils.getWeekRangeString(year, month, weekNum)
            assertTrue(rangeString.isNotEmpty())
        }
    }

    @Test
    fun testEdgeCasesMonthBoundaries() {
        // Test December 2023 to January 2024 boundary
        val dec2023Weeks = DateUtils.getNumberOfWeeksInMonthView(2023, 12)
        val jan2024Weeks = DateUtils.getNumberOfWeeksInMonthView(2024, 1)
        
        // Both should be valid (positive numbers)
        assertTrue(dec2023Weeks > 0)
        assertTrue(jan2024Weeks > 0)
        
        // Test last week of December 2023
        val lastWeekDec = DateUtils.getDatesForWeek(2023, 12, dec2023Weeks)
        assertEquals(7, lastWeekDec.size)
        
        // Test first week of January 2024
        val firstWeekJan = DateUtils.getDatesForWeek(2024, 1, 1)
        assertEquals(7, firstWeekJan.size)
    }

    @Test
    fun testLeapYearFebruary() {
        // Test February 2024 (leap year)
        val feb2024Weeks = DateUtils.getNumberOfWeeksInMonthView(2024, 2)
        
        // February 2024 starts on Thursday and has 29 days, should need 5 weeks
        assertEquals(5, feb2024Weeks)
        
        // Test February 2023 (non-leap year)
        val feb2023Weeks = DateUtils.getNumberOfWeeksInMonthView(2023, 2)
        
        // February 2023 starts on Wednesday and has 28 days, should need 5 weeks
        assertEquals(5, feb2023Weeks)
    }
}