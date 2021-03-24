package com.anibear.andvideoeditingtool.videoTrimmer.interfaces

interface OnProgressVideoListener {
    fun updateProgress(time: Int, max: Int, scale: Float)
}