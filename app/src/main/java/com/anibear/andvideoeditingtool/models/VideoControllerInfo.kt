package com.anibear.andvideoeditingtool.models

import android.graphics.Bitmap
import android.net.Uri

class VideoControllerInfo(val bitmap: Bitmap, val maxDuration: Int) {

    companion object {
        @JvmStatic
        private val videoList = ArrayList<VideoControllerInfo>()

        @JvmStatic
        fun setList(bitmap: Bitmap, maxDuration: Int) {
            videoList.add(VideoControllerInfo(bitmap, maxDuration))
        }

        @JvmStatic
        fun clearList() {
            videoList.clear()
        }

        @JvmStatic
        fun getList(): ArrayList<VideoControllerInfo> {
            return videoList
        }

        @JvmStatic
        fun getList(position: Int): VideoControllerInfo {
            return videoList[position]
        }

        @JvmStatic
        fun removeAt(position: Int) {
            videoList.removeAt(position)
        }

        @JvmStatic
        fun changeListItem(position: Int, bitmap: Bitmap, maxDuration: Int) {
            videoList[position] = VideoControllerInfo(bitmap, maxDuration)

        }

    }
}