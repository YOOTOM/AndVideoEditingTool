package com.anibear.andvideoeditingtool.models

import java.io.File

data class VideoInfo(
    private val _path: String,
    private val _duration: Long,
    private val _file: File
) {
    val path
        get() = _path

    val duration: Long
        get() = _duration

    val file: File
        get() = _file
}