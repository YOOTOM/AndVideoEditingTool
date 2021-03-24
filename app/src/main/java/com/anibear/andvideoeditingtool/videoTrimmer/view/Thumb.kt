package com.anibear.andvideoeditingtool.videoTrimmer.view

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.anibear.andvideoeditingtool.R
import java.util.*

class Thumb {
    var mIndex = 0

    var mVal = 0f

    var mPos = 0f

    var mBitmap: Bitmap? = null

    var mWidthBitmap = 0

    private var mHeightBitmap = 0

    var mLastTouchX = 0f

    private fun setBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
        mWidthBitmap = bitmap.width
        mHeightBitmap = bitmap.height
    }

    companion object {
        const val LEFT = 0
        const val RIGHT = 1

        @JvmStatic
        fun initThumbs(resources: Resources?): List<Thumb> {
            val thumbs: MutableList<Thumb> = Vector()
            for (i in 0..1) {
                val th = Thumb()
                th.mIndex = i
                if (i == 0) {
                    val resImageLeft = R.drawable.apptheme_text_select_handle_left
                    th.setBitmap(BitmapFactory.decodeResource(resources, resImageLeft))
                } else {
                    val resImageRight = R.drawable.apptheme_text_select_handle_right
                    th.setBitmap(BitmapFactory.decodeResource(resources, resImageRight))
                }
                thumbs.add(th)
            }
            return thumbs
        }

        @JvmStatic
        fun getWidthBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].mWidthBitmap
        }

        @JvmStatic
        fun getHeightBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].mHeightBitmap
        }
    }
}