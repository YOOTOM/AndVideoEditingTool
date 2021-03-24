package com.anibear.andvideoeditingtool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.models.VideoTimeLine
import com.anibear.andvideoeditingtool.viewholders.MergeHolder
import kotlinx.android.synthetic.main.video_processor_fragment.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Govind on 3/7/2018.
 */
class MergeAdapter(
    private val versionList: ArrayList<VideoTimeLine>,
    private val videoView: VideoView,
    private val fragment: MasterProcessorFragment
    ) : RecyclerView.Adapter<MergeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeHolder {
        return MergeHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.merge_item_list, parent, false),
            videoView, fragment
        )
    }

    override fun onBindViewHolder(holder: MergeHolder, position: Int) {
        holder.bindData(versionList[position],position)

    }

    override fun getItemCount(): Int {
        return versionList.size
    }
}