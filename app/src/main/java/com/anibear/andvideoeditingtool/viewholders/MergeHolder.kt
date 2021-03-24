package com.anibear.andvideoeditingtool.viewholders

import android.view.View
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.interfaces.VideoTimeListener
import com.anibear.andvideoeditingtool.models.VideoTimeLine
import com.anibear.andvideoeditingtool.viewbinders.MergeBinder

class MergeHolder(
    itemView: View,
    videoView: VideoView,
    fragment: MasterProcessorFragment
) : RecyclerView.ViewHolder(itemView) {

    private var mergeBinder: MergeBinder? = null

    init {
        mergeBinder = MergeBinder(itemView, videoView, fragment)
        itemView.setOnClickListener {
        }
    }

    fun bindData(videoTimeLine: VideoTimeLine, position: Int) {
        if (mergeBinder != null) {
            mergeBinder!!.bindData(videoTimeLine, position)
        }
    }

    fun changedPos(videoTimeListener: VideoTimeListener?){
        if (mergeBinder != null) {
            if(videoTimeListener !=null){
                mergeBinder!!.changedPos(videoTimeListener)
            }else {
                mergeBinder!!.changedPos(null)
            }
        }
    }
}