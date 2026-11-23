package com.pharos.evlink

import android.util.Log

/** Thin logging wrapper so we can later route logs to a file / in-app console. */
object PharosLog {
    private const val TAG = "Pharos"
    fun d(scope: String, msg: String) = Log.d(TAG, "[$scope] $msg")
    fun i(scope: String, msg: String) = Log.i(TAG, "[$scope] $msg")
    fun w(scope: String, msg: String) = Log.w(TAG, "[$scope] $msg")
    fun e(scope: String, msg: String, t: Throwable? = null) = Log.e(TAG, "[$scope] $msg", t)
}
