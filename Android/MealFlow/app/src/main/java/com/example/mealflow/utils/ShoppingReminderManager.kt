package com.example.mealflow.utils

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mealflow.R
import com.example.mealflow.receivers.ShoppingReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/**
 * Manages shopping trip reminders - scheduling and cancellation
 */
class ShoppingReminderManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "shopping_reminders_channel"
        const val REQUEST_CODE_PREFIX = 1000 // Base request code for shopping reminders
        
        // Reminder times (hours before shopping day)
        val REMINDER_TIMES = listOf(
            Triple("Later today (6:00 PM)", 0, 18), // Today at 6:00 PM
            Triple("Tomorrow morning (8:00 AM)", 1, 8),  // Tomorrow at 8:00 AM
            Triple("Wednesday morning (8:00 AM)", 2, 8),  // Day after tomorrow at 8:00 AM
            Triple("Pick a date & time", -1, 0)           // Custom time (special value)
        )
        
        // Default shopping time (assumed for "hours before" calculations)
        val DEFAULT_SHOPPING_TIME = LocalTime.of(10, 0) // 10:00 AM
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for shopping reminders
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shopping Reminders"
            val descriptionText = "Reminders for upcoming shopping trips"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("ShoppingReminderManager", "Created notification channel: $CHANNEL_ID")
        }
    }
    
    /**
     * Checks if the app has permission to schedule exact alarms
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required for Android 11 and below
        }
    }
    
    /**
     * Gets the intent to request the exact alarm permission
     */
    fun getExactAlarmPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null // Not needed for Android 11 and below
        }
    }
    
    /**
     * Schedules a reminder for a shopping trip
     * 
     * @param shoppingDate The date of the shopping trip
     * @param shoppingDayIndex The index of the shopping day (0-based)
     * @param reminderTimeIndex The index of the reminder time (0-1-2-3 from REMINDER_TIMES)
     * @param notes Optional notes for the shopping trip
     * @param customTime Optional custom time for the reminder (used if reminderTimeIndex == 3)
     * @return The ID of the reminder or null if scheduling failed
     */
    fun scheduleReminder(
        shoppingDate: LocalDate,
        shoppingDayIndex: Int,
        reminderTimeIndex: Int = 0,
        notes: String? = null,
        customTime: LocalTime? = null
    ): String? {
        val reminderId = UUID.randomUUID().toString()
        
        // Get the reminder time configuration
        val timeConfig = REMINDER_TIMES[reminderTimeIndex.coerceIn(0, REMINDER_TIMES.size - 1)]
        val (_, daysBefore, hoursBefore) = timeConfig
        
        // Calculate reminder date and time
        val reminderDate = shoppingDate.minusDays(daysBefore.toLong().coerceAtLeast(0))
        
        // Determine the reminder time
        val reminderTime = when {
            // Custom time selected
            reminderTimeIndex == 3 && customTime != null -> {
                customTime
            }
            // Same day reminder with hours before format
            daysBefore == 0 && hoursBefore < 12 -> {
                // Use the default shopping time and subtract hours
                DEFAULT_SHOPPING_TIME.minusHours(hoursBefore.toLong())
            }
            // Specific time of day (e.g. 8 PM = 20:00)
            else -> {
                LocalTime.of(hoursBefore, 0)
            }
        }
        
        val reminderDateTime = LocalDateTime.of(reminderDate, reminderTime)
        val timeInMillis = reminderDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        // Create the pending intent
        val intent = Intent(context, ShoppingReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("SHOPPING_DATE", DateUtils.formatToISO(shoppingDate))
            putExtra("SHOPPING_DAY_INDEX", shoppingDayIndex)
            putExtra("NOTES", notes ?: "")
            putExtra("REMINDER_TIME", reminderTime.toString())
        }
        
        // Generate a unique request code based on the shopping day index
        val requestCode = REQUEST_CODE_PREFIX + shoppingDayIndex
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12+, check permission first
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact alarm if we don't have permission
                    Log.w("ShoppingReminderManager", "No permission to set exact alarms, using inexact alarm instead")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6-11
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                // For older Android versions
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d("ShoppingReminderManager", "Scheduled reminder $reminderId for shopping day $shoppingDayIndex on $shoppingDate at $reminderDateTime")
            
            // Save reminder metadata to preferences or database
            saveReminderMetadata(reminderId, shoppingDate, shoppingDayIndex, reminderTimeIndex, notes, customTime)
            
            return reminderId
        } catch (e: SecurityException) {
            Log.e("ShoppingReminderManager", "Failed to set reminder: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Cancels a reminder for a shopping trip
     * 
     * @param shoppingDayIndex The index of the shopping day
     */
    fun cancelReminder(shoppingDayIndex: Int) {
        // Generate the request code
        val requestCode = REQUEST_CODE_PREFIX + shoppingDayIndex
        
        // Create a matching pending intent
        val intent = Intent(context, ShoppingReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel the alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        
        // Also cancel any active notification for this reminder
        cancelNotification(shoppingDayIndex)
        
        // Remove reminder metadata
        removeReminderMetadata(shoppingDayIndex)
        
        Log.d("ShoppingReminderManager", "Cancelled reminder for shopping day $shoppingDayIndex")
    }
    
    /**
     * Cancels any active notification for the shopping trip
     */
    fun cancelNotification(shoppingDayIndex: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = REQUEST_CODE_PREFIX + shoppingDayIndex
        notificationManager.cancel(notificationId)
        Log.d("ShoppingReminderManager", "Cancelled notification for shopping day $shoppingDayIndex")
    }
    
    /**
     * Saves reminder metadata to shared preferences
     */
    private fun saveReminderMetadata(
        reminderId: String,
        shoppingDate: LocalDate,
        shoppingDayIndex: Int,
        reminderTimeIndex: Int,
        notes: String?,
        customTime: LocalTime?
    ) {
        val sharedPrefs = context.getSharedPreferences("shopping_reminders", Context.MODE_PRIVATE)
        
        sharedPrefs.edit().apply {
            putString("reminder_id_$shoppingDayIndex", reminderId)
            putString("shopping_date_$shoppingDayIndex", DateUtils.formatToISO(shoppingDate))
            putInt("reminder_time_index_$shoppingDayIndex", reminderTimeIndex)
            putString("notes_$shoppingDayIndex", notes ?: "")
            putString("custom_time_$shoppingDayIndex", customTime?.toString() ?: "")
            apply()
        }
    }
    
    /**
     * Removes reminder metadata from shared preferences
     */
    private fun removeReminderMetadata(shoppingDayIndex: Int) {
        val sharedPrefs = context.getSharedPreferences("shopping_reminders", Context.MODE_PRIVATE)
        
        sharedPrefs.edit().apply {
            remove("reminder_id_$shoppingDayIndex")
            remove("shopping_date_$shoppingDayIndex")
            remove("reminder_time_index_$shoppingDayIndex")
            remove("notes_$shoppingDayIndex")
            remove("custom_time_$shoppingDayIndex")
            apply()
        }
    }
    
    /**
     * Gets reminder metadata from shared preferences
     * 
     * @return Quadruple(reminderTimeIndex, date, notes, customTime) or null if no reminder exists
     */
    fun getReminderMetadata(shoppingDayIndex: Int): ReminderMetadata? {
        val sharedPrefs = context.getSharedPreferences("shopping_reminders", Context.MODE_PRIVATE)
        
        val dateString = sharedPrefs.getString("shopping_date_$shoppingDayIndex", null) ?: return null
        val date = DateUtils.parseLocalDate(dateString) ?: return null
        val reminderTimeIndex = sharedPrefs.getInt("reminder_time_index_$shoppingDayIndex", 0)
        val notes = sharedPrefs.getString("notes_$shoppingDayIndex", "") ?: ""
        val customTimeStr = sharedPrefs.getString("custom_time_$shoppingDayIndex", null)
        val customTime = if (customTimeStr.isNullOrEmpty()) null else LocalTime.parse(customTimeStr)
        
        return ReminderMetadata(reminderTimeIndex, date, notes, customTime)
    }
    
    /**
     * Checks if a reminder exists for a shopping day
     */
    fun hasReminder(shoppingDayIndex: Int): Boolean {
        val sharedPrefs = context.getSharedPreferences("shopping_reminders", Context.MODE_PRIVATE)
        return sharedPrefs.contains("reminder_id_$shoppingDayIndex")
    }
    
    /**
     * Data class to hold reminder metadata
     */
    data class ReminderMetadata(
        val reminderTimeIndex: Int,
        val date: LocalDate,
        val notes: String,
        val customTime: LocalTime?
    )
} 