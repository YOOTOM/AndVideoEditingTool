package com.anibear.andvideoeditingtool.interfaces

interface VideoTimeListener {
    fun onStartTime(str: String, postion: Int)
    fun onEndTime(str: String, postion: Int)
}