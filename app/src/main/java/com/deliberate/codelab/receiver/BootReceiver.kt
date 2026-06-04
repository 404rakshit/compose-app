package com.deliberate.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling today's alarms...")

            // 1. You can trigger a OneTimeWorkRequest here to run your DailyAlarmSyncWorker
            // OR
            // 2. You can inject your TodoRepository and AndroidAlarmScheduler here
            // and do a quick SQLite read for today's tasks.
        }
    }
}