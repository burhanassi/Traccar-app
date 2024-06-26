package com.logestechs.driver.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import java.util.*
import android.provider.Settings
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    var listForMin = listOf(1)
    var listForSec = listOf(60L)

    override fun onReceive(context: Context, intent: Intent?) {
        setAlarm(context)
        val `in` = Intent(context, MyLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(`in`)
        } else {
            context.startService(`in`)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pi: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeInMinToMultiply = listForMin[Random().nextInt(listForMin.size)]
        val timeInSecToMultiply = listForSec[Random().nextInt(listForSec.size)]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !am.canScheduleExactAlarms()) {
                Intent().also {
                    it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    val uri = Uri.fromParts("package", context.packageName, null)
                    it.data = uri
                    context.startActivity(it)
                }
        } else {
            am.setAlarmClock(
                AlarmClockInfo(
                    System.currentTimeMillis() + timeInMinToMultiply * timeInSecToMultiply * 1000L,
                    pi
                ), pi
            )
        }
    }
}