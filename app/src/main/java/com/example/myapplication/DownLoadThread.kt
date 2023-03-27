package com.example.myapplication

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import android.util.Log

class DownLoadThread : HandlerThread("DownLoadThread"), Monitor {

    val TAG = "DownLoadThread"
    var mUIHandler: Handler? = null
    var percent = 0

    companion object {
        val STATE_START = 0
        val STATE_DOWNLOADING = 1
        val STATE_FINISH = 2
    }


    override fun onLooperPrepared() {
        while (percent != 100) {
            Log.e(TAG, "${Thread.currentThread()} 下载了:${percent} %")
            percent++
            SystemClock.sleep(1000)
            mUIHandler?.sendMessage(Message.obtain().apply {
                what = STATE_DOWNLOADING
                obj = percent
            })
            if (percent == 10) {
                lock()
            }
        }
        mUIHandler?.sendEmptyMessage(STATE_FINISH)
        super.onLooperPrepared()
    }

    private fun lock() {
        synchronized(this) {
            SystemClock.sleep(Long.MAX_VALUE)
        }
    }

    fun startDownload() {
        mUIHandler?.sendEmptyMessage(STATE_START)
    }

    override fun monitor() {
        synchronized(this) {//阻塞

        }
    }

}