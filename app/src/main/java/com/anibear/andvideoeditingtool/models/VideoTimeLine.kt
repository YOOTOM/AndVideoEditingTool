package com.anibear.andvideoeditingtool.models

import android.net.Uri

class VideoTimeLine(val path: Uri, val maxDuration: Int, val videoInfo: VideoInfo) {

    companion object {
        @JvmStatic
        private val versionList = ArrayList<VideoTimeLine>()

        @JvmStatic
        fun setList(path: Uri, maxDuration: Int, videoInfo: VideoInfo) {
            versionList.add(VideoTimeLine(path, maxDuration, videoInfo))
            totalMaxDuration += maxDuration
        }

        @JvmStatic
        private var totalMaxDuration: Int = 0

        @JvmStatic
        fun getTotalMaxDuration(): Int {
            return totalMaxDuration
        }

        @JvmStatic
        fun clearList() {
            versionList.clear()
        }

        @JvmStatic
        fun removeAt(position: Int) {
            versionList.removeAt(position)

        }

        @JvmStatic
        fun resetTotalMaxDuration(value: Int) {
            totalMaxDuration = value
        }

        @JvmStatic
        fun getList(): ArrayList<VideoTimeLine> {
            return versionList
        }

        @JvmStatic
        fun changeListItem(position: Int, path: Uri, maxDuration: Int, videoInfo: VideoInfo) {
            versionList[position] = VideoTimeLine(path, maxDuration, videoInfo)

        }


    }
}