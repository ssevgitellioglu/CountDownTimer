package com.sevgitellioglu.timeractivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.sevgitellioglu.timeractivity.util.PrefUtil

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object{
        fun setAlarm(context: Context,nowSeconds : Long,secondsRemaining:Long) : Long{
            val wakeUpTime=(nowSeconds+secondsRemaining)*1000
            val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent= Intent(context,TimerExpireReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent=Intent(context,TimerExpireReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            val alarmMAnager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMAnager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)

        }
        val nowSeconds:Long
        get() = Calendar.getInstance().timeInMillis/1000

    }
    enum class TimerState {
        Stopped,Paused,Runnig
    }
    private lateinit var timer:CountDownTimer
    private  var timerLengthSeconds=0L
    private var timerState=TimerState.Stopped
    private var secondsRemaining=0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title="   Timer"

        fab_start.setOnClickListener { v ->
            startTimer()
            timerState=TimerState.Runnig
            updateButtons()
        }
        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timerState=TimerState.Paused
            updateButtons()

        }
        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()


        }
    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)
    }

    override fun onPause() {
        super.onPause()
        if(timerState==TimerState.Runnig)
        {
            timer.cancel()
            val wakeUpTime= setAlarm(this, nowSeconds,secondsRemaining)
        }
        else if(timerState==TimerState.Paused)
        {

        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timerState,this)
    }

    private fun initTimer(){
    timerState=PrefUtil.getTimerState(this)
        if(timerState==TimerState.Stopped)
        {
            setNewTimerLength()
        }

        else{
            setPreviousTimerLength()
        }
        secondsRemaining=if(timerState==TimerState.Runnig || timerState==TimerState.Paused)
        {
            PrefUtil.getSecondsRemaining(this)
        }
        else{
            timerLengthSeconds
        }

        val alarmSetTime=PrefUtil.getAlarmSetTime(this)
        if(alarmSetTime>0){
            secondsRemaining -= nowSeconds-alarmSetTime
        }
        if(secondsRemaining <= 0)
        {
            onTimerFinished()
        }
      else if(timerState==TimerState.Runnig)
        {
            startTimer()
            updateButtons()
            updateCountDownUI()

        }

        //
}

    private fun updateButtons() {
        when(timerState){
            TimerState.Runnig -> {
                fab_start.isEnabled=false
                fab_stop.isEnabled=true
                fab_pause.isEnabled=true
            }
            TimerState.Paused ->{
                fab_start.isEnabled=true
                fab_stop.isEnabled=true
                fab_pause.isEnabled=false
            }
            TimerState.Stopped->{
                fab_start.isEnabled=true
                fab_stop.isEnabled=true
                fab_pause.isEnabled=false
            }
        }
    }

    private fun onTimerFinished(){

        timerState=TimerState.Stopped
        setNewTimerLength()
        progressbar_countdown.progress=0

        PrefUtil.setSecondsRemaining(timerLengthSeconds,this)

        secondsRemaining=timerLengthSeconds

        updateButtons()
        updateCountDownUI()
    }

    private fun startTimer(){
        timerState=TimerState.Runnig
        timer = object :CountDownTimer(secondsRemaining*1000,1000){
            override fun onFinish()=onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining=millisUntilFinished/1000
                updateCountDownUI()
            }
        }.start()
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished=secondsRemaining /60
        val secondsInMinuteUntilFinished=secondsRemaining-minutesUntilFinished*60
        val secondsStr=secondsInMinuteUntilFinished.toString()
        textView_countdown.text="$minutesUntilFinished:${
            if(secondsStr.length==2) secondsStr
            else "0" + secondsStr }"
        progressbar_countdown.progress=(timerLengthSeconds-secondsRemaining).toInt()

    }

    private fun setNewTimerLength(){
        val lengthInMinutes=PrefUtil.getTimerLength(this)
        timerLengthSeconds=(lengthInMinutes*60L)
        progressbar_countdown.max=timerLengthSeconds.toInt()
    }
    private fun setPreviousTimerLength(){
        timerLengthSeconds=PrefUtil.getPreviousTimerLengthSeconds(this)
        progressbar_countdown.max=timerLengthSeconds.toInt()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
