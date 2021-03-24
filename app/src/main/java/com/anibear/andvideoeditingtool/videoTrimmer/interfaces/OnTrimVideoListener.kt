package com.anibear.andvideoeditingtool.videoTrimmer.interfaces

import android.net.Uri

interface OnTrimVideoListener {
    fun onTrimStarted(startPosition: Int, endPosition: Int)
    fun onTrimStarted(startPosition: Int, endPosition: Int, path: Uri)
    fun getResult(uri: Uri?)
    fun cancelAction()
    fun onError(message: String?)
}