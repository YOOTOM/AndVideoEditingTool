package com.anibear.andvideoeditingtool.viewbinders

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.models.VideoControllerInfo
import com.anibear.andvideoeditingtool.utils.Constant
import com.anibear.andvideoeditingtool.videoTrimmer.utils.TrimVideoUtils

class VideoControllerBinder(itemView: View, private val fragment: MasterProcessorFragment) {
    private var mImg: AppCompatImageView
    private var mContainer: FrameLayout? = null
    private var mTxt: AppCompatTextView
    private var mImgBtn: AppCompatImageView
    private val mContext: Context = itemView.context

    init {
        mContainer = itemView.findViewById(R.id.frame_container)
        mImg = itemView.findViewById(R.id.img_video)
        mTxt = itemView.findViewById(R.id.txt_video)
        mImgBtn = itemView.findViewById(R.id.img_btn)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun bindData(position: Int) {
        mImg.setImageBitmap(VideoControllerInfo.getList()[position].bitmap)
        mTxt.text =
            TrimVideoUtils.stringForTime(VideoControllerInfo.getList()[position].maxDuration)
        if (VideoControllerInfo.getList().size - 1 == position) {
            mImgBtn.setImageDrawable(
                mContext.resources.getDrawable(
                    R.drawable.video_gallery_24,
                    null
                )
            )
            mImgBtn.setOnClickListener {
                if (fragment.mCurrentPosition < VideoControllerInfo.getList().size - 1) {
                    fragment.mScrollViewVideoController?.smoothScrollToPosition(VideoControllerInfo.getList().size - 1)
                } else {
                    if (fragment.mCurrentPosition == VideoControllerInfo.getList().size - 1) {
                        fragment.checkPermission(
                            Constant.VIDEO_GALLERY,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }

    fun showEffect() {
        mContainer?.setBackgroundColor(Color.parseColor("#EEEE00"))
    }

    fun hidenEffect() {
        mContainer?.setBackgroundColor(Color.GRAY)
    }
}