/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.anibear.andvideoeditingtool.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.adapter.FilterAdapter
import com.anibear.andvideoeditingtool.exoplayerfilter.FilterType
import com.anibear.andvideoeditingtool.exoplayerfilter.FilterType2
import com.anibear.andvideoeditingtool.exoplayerfilter.MovieWrapperView
import com.anibear.andvideoeditingtool.exoplayerfilter.PlayerTimer
import com.anibear.andvideoeditingtool.interfaces.CommunicateListener
import com.anibear.andvideoeditingtool.interfaces.FilterListener
import com.anibear.andvideoeditingtool.widget.SimpleProgressDialog
import com.daasuu.epf.EPlayerView
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.composer.Mp4Composer
import com.daasuu.mp4compose.filter.GlFilter
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kk.taurus.playerbase.config.AppContextAttach.getApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class FilterFragment(
    private val position: Int,
    private val movieWrapperView: MovieWrapperView,
    private val videoView: VideoView
) : BottomSheetDialogFragment(), FilterListener{

    private var tagName: String = FilterFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvFilter: RecyclerView
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var videoDuration: Int = 0
    private var helper: BaseCreatorDialogFragment.CallBacks? = null

    private lateinit var optiFilterAdapter: FilterAdapter
    private var selectedFilter: GlFilter? = null
    private var bmThumbnail: Bitmap? = null
    private var mContext: Context? = null
    private var ePlayerView: EPlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var playerTimer: PlayerTimer? = null
    private var filterTypes2: ArrayList<FilterType2>? = null
    private var filterTypes: ArrayList<FilterType>? = null
    private var simpleProgressDialog: SimpleProgressDialog? = null
    private var mp4Composer: Mp4Composer? = null
    private var videoChangeListener: CommunicateListener<Int, String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_filter_dialog, container, false)
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!, R.style.MyTransparentBottomSheetDialogTheme);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFilter = rootView.findViewById(R.id.rvFilter)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)
        mContext = context

        ivClose.setOnClickListener {
            releasePlayer()
            if (playerTimer != null) {
                playerTimer!!.stop()
                playerTimer!!.removeMessages(0)
            }
            movieWrapperView.visibility = View.INVISIBLE
            videoView.visibility = View.VISIBLE
            dismiss()
        }

        ivDone.setOnClickListener {

            if (selectedFilter != null) {
                startCodec(
                    videoFile!!.absolutePath,
                    /* Utils.createVideoFile(context!!).toString()*/
                    getCustomVideoFilePath().toString(),
                    selectedFilter!!
                )
            }
        }


        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvFilter.layoutManager = linearLayoutManager

        filterTypes2 = FilterType2.createFilterList()
        filterTypes = FilterType.createFilterList()

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(
            videoFile!!.absolutePath,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )
        if (bmThumbnail == null)
            return

        optiFilterAdapter =
            FilterAdapter(filterTypes2!!, bmThumbnail!!, activity!!.applicationContext, this)
        rvFilter.adapter = optiFilterAdapter
        optiFilterAdapter.notifyDataSetChanged()

        setUpSimpleExoPlayer(mContext!!)
        setUoGlPlayerView()
        setUpTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mp4Composer!=null){
            mp4Composer!!.cancel()
        }
        releasePlayer()
        if (playerTimer != null) {
            playerTimer!!.stop()
            playerTimer!!.removeMessages(0)
        }
        movieWrapperView.visibility = View.INVISIBLE
        videoView.visibility = View.VISIBLE
    }

    private fun setUpSimpleExoPlayer(context: Context) {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, context.packageName))

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoFile.toString()))

        player = ExoPlayerFactory.newSimpleInstance(mContext)
        player?.prepare(videoSource)
        player?.playWhenReady = true
    }

    private fun setUoGlPlayerView() {
        ePlayerView = EPlayerView(mContext)
        ePlayerView?.apply {
            setSimpleExoPlayer(player)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        movieWrapperView.addView(ePlayerView)
        onResume()
    }

    private fun setUpTimer() {
        playerTimer = PlayerTimer()
        playerTimer?.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                val position: Long = player!!.currentPosition
                val duration: Long = player!!.duration
                if (duration <= 0) return
            }
        })
        playerTimer?.start()
    }

    private fun releasePlayer() {
        ePlayerView?.apply {
            onPause()
            movieWrapperView.removeAllViews()
        }
        ePlayerView = null
        player?.stop()
        player?.release()
        player = null
    }

    override fun selectedFilter(filter: String) {
    }

    private fun changeFilter(filter: FilterType, context: Context): GlFilter {
        return FilterType.createGlFilter(filter, context)
    }


    override fun selectedFilter(filter: String, position: Int, context: Context) {
        selectedFilter = changeFilter(filterTypes!![position], context)
        ePlayerView?.setGlFilter(
            FilterType2.createGlFilter(
                filterTypes2?.get(position),
                getApplicationContext()
            )
        )
    }

    fun setHelper(helper: BaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }

    fun setVideoDuration(duration: Int) {
        videoDuration = duration
    }

    fun setVideoChangedListener(communicateListener: CommunicateListener<Int, String>) {
        this.videoChangeListener = communicateListener
    }

    private fun startCodec(
        inputVideoPath: String,
        outputVideoPath: String,
        filter: GlFilter
    ) {
        simpleProgressDialog = SimpleProgressDialog.show(context, null, true, true)
        mp4Composer = null
        mp4Composer =
            Mp4Composer(inputVideoPath, outputVideoPath) // .rotation(Rotation.ROTATION_270)
                .size(720, 720)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(filter)
                //.mute(muteCheckBox.isChecked()) //.timeScale(2f)
                //.changePitch(false)
                //.trim(2000, 5000)
                //.flipHorizontal(flipHorizontalCheckBox.isChecked())
                //.flipVertical(flipVerticalCheckBox.isChecked())
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        /*Log.d(com.daasuu.sample.BasicUsageActivity.TAG, "onProgress = $progress")
                        runOnUiThread { progressBar.setProgress((progress * 100).toInt()) }*/
                    }

                    override fun onCompleted() {
                        exportMp4ToGallery(getApplicationContext(), outputVideoPath)
                        videoChangeListener?.Result(position, outputVideoPath)
                        simpleProgressDialog?.dismiss()
                        dismiss()
                    }

                    override fun onCanceled() {
                        simpleProgressDialog?.dismiss()
                    }

                    override fun onFailed(exception: java.lang.Exception) {
                        simpleProgressDialog?.dismiss()
                    }
                })
                .start()
    }

    fun exportMp4ToGallery(context: Context, filePath: String) {
        val values = ContentValues(2)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, filePath)
        context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        )
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://$filePath")
            )
        )
    }

    private fun getAndroidMoviesFolder(): File? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCustomVideoFilePath(): String? {
        return "${getAndroidMoviesFolder()!!.absolutePath}/${
            SimpleDateFormat("yyyyMM_dd-HHmmss").format(
                Date()
            )
        }filter_apply.mp4"
    }
}
