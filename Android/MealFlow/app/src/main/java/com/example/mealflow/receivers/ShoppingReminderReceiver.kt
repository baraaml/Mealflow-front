package com.example.mealflow.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mealflow.MainActivity
import com.example.mealflow.R
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.utils.ShoppingReminderManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * BroadcastReceiver for handling shopping trip reminders
 */
class ShoppingReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received shopping reminder broadcast with action: ${intent.action}")
        
        // Handle notification deletion action
        if (intent.action == ACTION_NOTIFICATION_DISMISSED) {
            val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
            val shoppingDayIndex = intent.getIntExtra("SHOPPING_DAY_INDEX", -1)
            if (notificationId != -1) {
                Log.d(TAG, "Handling notification dismissal for ID: $notificationId, shopping day: $shoppingDayIndex")
                // Clean up any resources if needed
                return
            }
        }
        
        // Extract data from intent
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: ""
        val shoppingDateStr = intent.getStringExtra("SHOPPING_DATE") ?: ""
        val shoppingDayIndex = intent.getIntExtra("SHOPPING_DAY_INDEX", 0)
        val notes = intent.getStringExtra("NOTES") ?: ""
        val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: ""
        
        Log.d(TAG, "Reminder data: ID=$reminderId, Date=$shoppingDateStr, DayIndex=$shoppingDayIndex, Notes=$notes, Time=$reminderTime")
        
        // Parse the shopping date
        val shoppingDate = DateUtils.parseLocalDate(shoppingDateStr)
        
        if (shoppingDate == null) {
            Log.e(TAG, "Failed to parse shopping date: $shoppingDateStr")
            return
        }
        
        // Format the date for display
        val formattedDate = shoppingDate.format(
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
        )
        
        // Create a notification title and content
        val title = "Shopping Trip Reminder"
        val content = "You have a shopping trip scheduled for $formattedDate"
        
        Log.d(TAG, "Creating notification with title: $title, content: $content")
        
        // Ensure notification channel exists
        createNotificationChannelIfNeeded(context)
        
        // Create an intent to open the shopping list screen
        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("NAVIGATE_TO", NavRoutes.ShoppingListPage.route)
            putExtra("SHOPPING_DAY_INDEX", shoppingDayIndex)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            shoppingDayIndex,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create a deletion intent
        val notificationId = NOTIFICATION_ID_BASE + shoppingDayIndex
        val deleteIntent = Intent(context, ShoppingReminderReceiver::class.java).apply {
            action = ACTION_NOTIFICATION_DISMISSED
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("SHOPPING_DAY_INDEX", shoppingDayIndex)
        }
        
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000, // Use different request code to avoid collision
            deleteIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, ShoppingReminderManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shopping_cart) // Use your shopping cart icon
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$content" + if (notes.isNotBlank()) "\n\nNotes: $notes" else ""
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setSound(defaultSoundUri)
            .setTimeoutAfter(3600000) // Auto-cancel after 1 hour if not interacted with
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Shopping reminder notification sent with ID $notificationId for day $shoppingDayIndex on $formattedDate")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ShoppingReminderManager.CHANNEL_ID,
                "Shopping Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming shopping trips"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created/updated: ${ShoppingReminderManager.CHANNEL_ID}")
        }
    }
    
    companion object {
        private const val TAG = "ShoppingReminderReceiver"
        private const val NOTIFICATION_ID_BASE = 2000
        const val ACTION_NOTIFICATION_DISMISSED = "com.example.mealflow.ACTION_NOTIFICATION_DISMISSED"
    }
} 