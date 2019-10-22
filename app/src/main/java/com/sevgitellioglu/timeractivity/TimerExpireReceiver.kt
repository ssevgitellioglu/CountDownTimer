package com.sevgitellioglu.timeractivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sevgitellioglu.timeractivity.util.PrefUtil

class TimerExpireReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        TODO("TimerExpireReceiver.onReceive() is not implemented")
        PrefUtil.setTimerState(MainActivity.TimerState.Stopped,context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}
