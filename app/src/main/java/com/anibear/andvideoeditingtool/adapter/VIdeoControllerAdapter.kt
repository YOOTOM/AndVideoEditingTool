package com.anibear.andvideoeditingtool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.models.VideoControllerInfo
import com.anibear.andvideoeditingtool.viewholders.VideoControllerHolder

class VideoControllerAdapter(
    private val videoList: ArrayList<VideoControllerInfo>,
    private val fragment: MasterProcessorFragment
) :
    RecyclerView.Adapter<VideoControllerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoControllerHolder {
        return VideoControllerHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.video_controller_item, parent, false), fragment
        )
    }

    override fun onBindViewHolder(holder: VideoControllerHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

}