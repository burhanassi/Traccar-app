package com.logestechs.driver.utils.location

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

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

    fun setAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        var pi: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pi = PendingIntent.getBroadcast(
                context,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val timeInMinToMultiply = listForMin[Random().nextInt(listForMin.size)]
        val timeInSecToMultiply = listForSec[Random().nextInt(listForSec.size)]
        am.setAlarmClock(
            AlarmClockInfo(
                System.currentTimeMillis() + timeInMinToMultiply * timeInSecToMultiply * 1000L,
                pi
            ), pi
        )
    }
}