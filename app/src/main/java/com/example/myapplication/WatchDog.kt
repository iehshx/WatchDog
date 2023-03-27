package com.example.myapplication

import android.os.Handler
import android.os.HandlerThread
import android.os.MessageQueue
import android.os.SystemClock
import android.util.Log

class HandlerChecker : Runnable {

    var mHandler: Handler? = null
    val mMonitors: ArrayList<Monitor> = ArrayList()
    var mCompleted = false
    val DEFALUT_WAIT_MAX_MILLIS = 5000
    var mWaitMax = DEFALUT_WAIT_MAX_MILLIS

    var mStartTime = 0L

    constructor(mHandler: Handler?, mWaitMax: Int) {
        this.mHandler = mHandler
        this.mWaitMax = mWaitMax
        mCompleted = true
    }

    constructor(mHandler: Handler?) {
        this.mHandler = mHandler
        mCompleted = true
    }


    fun addMonitor(monitor: Monitor) {
        mMonitors.add(monitor)
    }

    override fun run() {
        for (item in mMonitors) {
            item.monitor()//阻塞
        }

        mCompleted = true
    }

    fun scheduleCheckLocked() {
        val isPolling = MessageQueue::class.java.getMethod("isPolling")
            .invoke(mHandler?.looper?.queue) as Boolean

        if (mMonitors.size == 0 && isPolling) {
            mCompleted = true
            return
        }
        if (!mCompleted) return

        mCompleted = false
        mStartTime = SystemClock.uptimeMillis()
        mHandler?.postAtFrontOfQueue(this)
    }

    fun getCompletionStateLocked(): Int {
        if (mCompleted) {
            return WatchDog.COMPLETED
        } else {
            var diff = SystemClock.uptimeMillis() - mStartTime
            if (diff < mWaitMax / 2) {
                return WatchDog.WAITED_HALF
            } else if (diff < mWaitMax) {
                return WatchDog.WAITING
            }
        }
        return WatchDog.OVERDUE
    }

}

object WatchDog : Thread("WatchDog") {

    val mHandlerCheckers: ArrayList<HandlerChecker> = ArrayList()
    var mMonitorChecker: HandlerChecker

    init {
        val monitorHandlerThread = HandlerThread("MonitorHandlerThread")
        monitorHandlerThread.start()
        var monitorHanlder = Handler(monitorHandlerThread.looper)
        val monitorHandlerChecker = HandlerChecker(monitorHanlder)
        mMonitorChecker = monitorHandlerChecker
        mHandlerCheckers.add(mMonitorChecker)
    }

    fun addMonitor(monitor: Monitor) {
        mMonitorChecker.addMonitor(monitor)
    }

    override fun run() {
        super.run()
        while (true) {
            for (item in mHandlerCheckers) {
                item.scheduleCheckLocked()
            }

            SystemClock.sleep(500)

            val state = evaluateCheckerCompletionLocked()
            when (state) {
                COMPLETED -> {
                    continue
                }
                WAITING -> {
                    continue
                }
                WAITED_HALF -> {
                    Log.e("iehshx", "time_half")
                    continue
                }
            }
            Log.e("iehshx", "time overdue")
        }
    }

    const val COMPLETED = 0
    const val WAITING = 1
    const val WAITED_HALF = 2
    const val OVERDUE = 3

    private fun evaluateCheckerCompletionLocked(): Int {
        var state = COMPLETED
        for (item in mHandlerCheckers) {
            state = Math.max(state, item.getCompletionStateLocked())
        }
        return state
    }

}