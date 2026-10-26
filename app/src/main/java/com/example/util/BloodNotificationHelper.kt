package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.BloodRequestEntity

object BloodNotificationHelper {

    const val CHANNEL_ID = "emergency_blood_alerts"
    private const val CHANNEL_NAME = "Emergency Blood Alerts"
    private const val CHANNEL_DESCRIPTION = "Alerts donors when emergency blood matching their blood group is requested"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canSendNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showEmergencyBloodAlert(
        context: Context,
        request: BloodRequestEntity
    ) {
        if (!canSendNotification(context)) return

        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            request.id,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to call directly
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.contactPhone}"))
        val pendingCallIntent = PendingIntent.getActivity(
            context,
            request.id + 100000,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚨 Emergency ${request.bloodGroup} Blood Needed!"
        val shortText = "${request.patientName} needs ${request.unitsNeeded} unit(s) at ${request.hospitalName}"
        val bigText = "Hospital: ${request.hospitalName}, ${request.hospitalLocation}\n" +
                "Patient: ${request.patientName} (${request.unitsNeeded} Bag Needed)\n" +
                "Urgency: ${request.urgency}\n" +
                (if (request.notes.isNotBlank()) "Note: ${request.notes}\n" else "") +
                "Contact: ${request.contactPhone}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.blood_donor_icon_1792993431174)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(0xFFB71C1C.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Call (${request.contactPhone})",
                pendingCallIntent
            )
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        val notificationId = if (request.id > 0) request.id else (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
    }
}
