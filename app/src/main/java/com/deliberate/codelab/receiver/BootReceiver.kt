package com.deliberate.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.deliberate.codelab.worker.DailyAlarmSyncWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Triggering immediate alarm rebuild...")

            // Fire a one-time request immediately to fix the AlarmManager
            val repairRequest = OneTimeWorkRequestBuilder<DailyAlarmSyncWorker>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "BOOT_ALARM_REPAIR",
                ExistingWorkPolicy.REPLACE,
                repairRequest
            )
        }
    }
}