package com.android.Logger

import LogcatThread
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    var logcatThread: LogcatThread? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            logcatThread = LogcatThread(context)
            logcatThread?.start()
            logcatThread?.run()
        }
    }
}
