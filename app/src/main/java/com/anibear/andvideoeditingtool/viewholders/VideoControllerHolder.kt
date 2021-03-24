package com.anibear.andvideoeditingtool.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.viewbinders.VideoControllerBinder

class VideoControllerHolder(itemView: View, fragment: MasterProcessorFragment) :
    RecyclerView.ViewHolder(itemView) {

    private var mVideoControllerBinder: VideoControllerBinder? = null
    private var mFragment: MasterProcessorFragment? = null

    init {
        mVideoControllerBinder = VideoControllerBinder(itemView,fragment)
        this.mFragment = fragment
        itemView.setOnClickListener {
            fragment.mScrollViewVideoController?.smoothScrollToPosition(adapterPosition)
        }
    }

    fun bindData(position: Int) {
        mVideoControllerBinder?.bindData(position)
       /* if (VideoControllerInfo.getList().size - 1 == position) {
            mFragment?.mScrollViewVideoController?.smoothScrollToPosition(position)
        }*/
    }

    fun showEffect() {
        mVideoControllerBinder?.showEffect()
    }

    fun hidenEffect() {
        mVideoControllerBinder?.hidenEffect()
    }


}