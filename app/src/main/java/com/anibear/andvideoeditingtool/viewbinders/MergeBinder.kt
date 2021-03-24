package com.anibear.andvideoeditingtool.viewbinders

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatButton
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.anibear.andvideoeditingtool.interfaces.VideoTimeListener
import com.anibear.andvideoeditingtool.models.VideoTimeLine
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnHgLVideoListener
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnProgressVideoListener
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnRangeSeekBarListener
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnTrimVideoListener
import com.anibear.andvideoeditingtool.videoTrimmer.utils.BackgroundExecutor
import com.anibear.andvideoeditingtool.videoTrimmer.utils.TrimVideoUtils
import com.anibear.andvideoeditingtool.videoTrimmer.utils.UiThreadExecutor
import com.anibear.andvideoeditingtool.videoTrimmer.view.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class MergeBinder(
    itemView: View,
    private val mVideoView: VideoView,
    private val fragment: MasterProcessorFragment
) {
    private var timeLineView: TimeLineView? = null
    private var videoEditView: VideoEditView? = null
    private val mContext: Context = itemView.context
    private var mListeners: ArrayList<OnProgressVideoListener>? = null
    private var mVideoProgressIndicator: ProgressBarView? = null
    private var btnCancel: AppCompatButton? = null
    private var btnSave: AppCompatButton? = null
    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnHgLVideoListener: OnHgLVideoListener? = null
    private var mRangeSeekBarView: RangeSeekBarView? = null
    private var mHolderTopView: SeekBar? = null
    private val SHOW_PROGRESS = 2
    private var mDuration = 0
    private var mTimeVideo = 0
    private var mStartPosition = 0
    private var mEndPosition = 0
    private var mResetSeekBar = true
    private val mMessageHandler = MessageHandler(this)
    private var mSrc: Uri? = null
    private var mOriginSizeFile: Long = 0
    private var mTimeInfoContainer: View? = null
    private var mFinalPath: String? = null
    private var mMaxDuration = 0
    private var mLayoutBtn: LinearLayout? = null

    init {
        timeLineView = itemView.findViewById(R.id.timeLineView)
        mVideoProgressIndicator = itemView.findViewById(R.id.progressBarView)
        btnCancel = itemView.findViewById(R.id.btnCancel)
        btnSave = itemView.findViewById(R.id.btnSave)
        mRangeSeekBarView = itemView.findViewById(R.id.timeLineBar)
        mHolderTopView = itemView.findViewById(R.id.handlerTop)
        mTimeInfoContainer = itemView.findViewById(R.id.timeText)
        mLayoutBtn = itemView.findViewById(R.id.layout_btn)
    }

    private var mVideoTimeLine: VideoTimeLine? = null
    private var mPosition: Int = 0
    fun bindData(videoTimeLine: VideoTimeLine, position: Int) {
        setMaxDuration(videoTimeLine.maxDuration)
        setOnTrimVideoListener(fragment)
        setOnHgLVideoListener(fragment)
        setVideoURI(videoTimeLine.path)
        setVideoInformationVisibility(true)
        mRangeSeekBarView?.visibility = View.INVISIBLE
        mHolderTopView?.visibility = View.INVISIBLE
        mLayoutBtn?.visibility = View.INVISIBLE
        mTimeInfoContainer?.visibility = View.INVISIBLE
        mVideoTimeLine = videoTimeLine
        mPosition = position
        setUpListeners(videoTimeLine, position)

        if (fragment.mHandlePlaySwitch?.visibility == View.INVISIBLE) {
            fragment.mHandlePlaySwitch?.visibility = View.VISIBLE
            fragment.mHandlePlaySwitch?.isSelected = true
        }
        fragment.mHandlePlaySwitch?.setOnClickListener {
            if (!fragment.mHandlePlaySwitch!!.isSelected) {
                fragment.mHandlePlaySwitch!!.isSelected = true
                mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
                mVideoView.pause()
            } else {
                fragment.mHandlePlaySwitch!!.isSelected = false
                mHolderTopView?.visibility = View.VISIBLE
                mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
                mVideoView.start()
            }
        }
        if (mRangeSeekBarView?.visibility == View.INVISIBLE) {
            mRangeSeekBarView?.visibility = View.INVISIBLE
            mLayoutBtn?.visibility = View.INVISIBLE
            mHolderTopView?.visibility = View.VISIBLE
            mTimeInfoContainer?.visibility = View.VISIBLE
        }
    }

    private var videoTimeListener: VideoTimeListener? = null
    fun changedPos(videoTimeListener: VideoTimeListener?) {
        if (videoTimeListener != null) {
            this.videoTimeListener = videoTimeListener
        }
        if (mHolderTopView?.visibility == View.VISIBLE) {
            mRangeSeekBarView?.visibility = View.INVISIBLE
            mLayoutBtn?.visibility = View.INVISIBLE
            mHolderTopView?.visibility = View.INVISIBLE
            mTimeInfoContainer?.visibility = View.INVISIBLE
        }
        bindData(mVideoTimeLine!!, mPosition)
    }

    private fun setUpMargins() {
        val marge = Objects.requireNonNull(mRangeSeekBarView!!.thumbs)[0].mWidthBitmap
        val widthSeek = mHolderTopView!!.thumb.minimumWidth / 2
        var lp = mHolderTopView!!.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0)
        mHolderTopView!!.layoutParams = lp
        lp = timeLineView!!.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(marge + 40, 0, marge + 40, 0)
        timeLineView!!.layoutParams = lp
        lp = mVideoProgressIndicator!!.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        mVideoProgressIndicator!!.layoutParams = lp
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListeners(videoTimeLine: VideoTimeLine, position: Int) {
        mListeners = ArrayList<OnProgressVideoListener>()
        mListeners!!.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Int, max: Int, scale: Float) {
                updateVideoProgress(time)
            }
        })
        mVideoProgressIndicator?.let { mListeners!!.add(it) }

        btnCancel!!.setOnClickListener {
            onCancelClicked()
            if (mRangeSeekBarView?.visibility == View.VISIBLE) {
                mRangeSeekBarView?.visibility = View.INVISIBLE
                mLayoutBtn?.visibility = View.INVISIBLE
                mHolderTopView?.visibility = View.INVISIBLE
                mTimeInfoContainer?.visibility = View.INVISIBLE
            } else {
                if (mRangeSeekBarView?.visibility == View.INVISIBLE) {
                    mRangeSeekBarView?.visibility = View.INVISIBLE
                    mLayoutBtn?.visibility = View.INVISIBLE
                    mHolderTopView?.visibility = View.VISIBLE
                    mTimeInfoContainer?.visibility = View.VISIBLE
                }
            }
        }
        btnSave!!.setOnClickListener {
            onSaveClicked(videoTimeLine)
        }

        mVideoView.apply {
            setOnErrorListener { _: MediaPlayer?, what: Int, _: Int ->
                mOnTrimVideoListener?.onError("Something went wrong reason : $what")
                false
            }
            setOnTouchListener { _, event ->
                true
            }

            setOnPreparedListener {
                onVideoPrepared(it)
            }
            setOnCompletionListener {
                onVideoCompleted()
            }
        }
        mRangeSeekBarView!!.setOnTouchListener { v, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mRangeSeekBarView?.visibility == View.VISIBLE) {
                        mRangeSeekBarView?.visibility = View.INVISIBLE
                        mLayoutBtn?.visibility = View.INVISIBLE
                        mHolderTopView?.visibility = View.INVISIBLE
                        mTimeInfoContainer?.visibility = View.INVISIBLE
                    }
                    v!!.parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_UP ->
                    v!!.parent.requestDisallowInterceptTouchEvent(true)
            }
            true
        }
        mRangeSeekBarView!!.apply {
            addOnRangeSeekBarListener(mVideoProgressIndicator)
            addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
                override fun onCreate(
                    rangeSeekBarView: RangeSeekBarView?,
                    index: Int,
                    value: Float
                ) {

                }

                override fun onSeek(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
                    onSeekThumbs(index, value)
                }

                override fun onSeekStart(
                    rangeSeekBarView: RangeSeekBarView?,
                    index: Int,
                    value: Float
                ) {
                }

                override fun onSeekStop(
                    rangeSeekBarView: RangeSeekBarView?,
                    index: Int,
                    value: Float
                ) {
                    onStopSeekThumbs()
                }
            })
        }

        mHolderTopView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val action = event!!.action
                when (action) {
                    MotionEvent.ACTION_DOWN ->
                        v!!.parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP ->
                        v!!.parent.requestDisallowInterceptTouchEvent(true)
                }
                v!!.onTouchEvent(event)
                return true
            }
        })
        mHolderTopView!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
                if (!isVideoViewStart) {
                    onPlayerIndicatorSeekStart()
                    onPlayerIndicatorSeekStop(seekBar)
                } else {
                    isVideoViewStart = false
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })
    }

    private fun onVideoCompleted() {
        mVideoView.seekTo(mStartPosition)
        if (mStartPosition == 0) {
            fragment.mHandlePlaySwitch!!.isSelected = true
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mVideoView.pause()
        }
    }

    private fun onClickVideoPlayPause() {
        if (mVideoView.isPlaying) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mVideoView.pause()
            fragment.mHandlePlaySwitch?.isSelected = true
        } else {
            mHolderTopView?.visibility = View.VISIBLE
            if (mResetSeekBar) {
                mResetSeekBar = false
                mVideoView.seekTo(mStartPosition)
            }
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mVideoView.start()
            fragment.mHandlePlaySwitch?.isSelected = false
        }
    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        mDuration = mVideoView.duration
        setSeekBarPosition()
        setTimeFrames()
        if (mOnHgLVideoListener != null) {
            mOnHgLVideoListener!!.onVideoPrepared()
        }
        mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
        mp.start()
        //mVideoView.start()
        fragment.mHandlePlaySwitch?.isSelected = false
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = mDuration / 2 - mMaxDuration / 2
            mEndPosition = mDuration / 2 + mMaxDuration / 2
            mRangeSeekBarView!!.setThumbValue(0, mStartPosition * 100 / mDuration.toFloat())
            mRangeSeekBarView!!.setThumbValue(1, mEndPosition * 100 / mDuration.toFloat())
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
        }
        setProgressBarPosition(mStartPosition)
        mVideoView.seekTo(mStartPosition)
        mTimeVideo = mDuration
        mRangeSeekBarView!!.initMaxWidth()
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView.pause()
        if (!fragment.mHandlePlaySwitch!!.isSelected)
            fragment.mHandlePlaySwitch!!.isSelected = true
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView.pause()
        if (!fragment.mHandlePlaySwitch!!.isSelected)
            fragment.mHandlePlaySwitch!!.isSelected = true
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView.pause()
        if (!fragment.mHandlePlaySwitch!!.isSelected)
            fragment.mHandlePlaySwitch!!.isSelected = true
        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        mVideoView.seekTo(duration)
        setTimeVideo(duration)
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        var duration = (mDuration * progress / 1000L).toInt()
        if (fromUser) {
            if (duration < mStartPosition) {
                setProgressBarPosition(mStartPosition)
                duration = mStartPosition
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition)
                duration = mEndPosition
            }
            setTimeVideo(duration)
        }
    }

    private var isVideoViewStart: Boolean = false
    private fun updateVideoProgress(time: Int) {
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mVideoView.pause()
            if (!fragment.mHandlePlaySwitch!!.isSelected)
                fragment.mHandlePlaySwitch!!.isSelected = true
            mResetSeekBar = true
            return
        }
        if (mHolderTopView != null) {
            isVideoViewStart = true
            setProgressBarPosition(time)
        }
        setTimeVideo(time)
    }


    private fun setProgressBarPosition(position: Int) {
        if (mDuration > 0) {
            val pos = 1000L * position / mDuration
            mHolderTopView!!.progress = pos.toInt()
        }
    }

    private fun setTimeVideo(position: Int) {
        val seconds: String = mContext.getString(R.string.short_seconds)
        if (videoTimeListener != null) {
            videoTimeListener?.onStartTime(
                String.format(
                    "%s %s",
                    TrimVideoUtils.stringForTime(position),
                    seconds
                ), position
            )
        }
    }

    @Synchronized
    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L).toInt()
                mVideoView.seekTo(mStartPosition)
                notifyProgressUpdate(false)
            }
            Thumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L).toInt()
            }
        }
        setProgressBarPosition(mStartPosition)
        setTimeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun setTimeFrames() {
        val seconds: String = mContext.getString(R.string.short_seconds)
        if (videoTimeListener != null) {
            videoTimeListener?.onStartTime(
                String.format(
                    "%s %s",
                    TrimVideoUtils.stringForTime(mStartPosition),
                    seconds
                ), mStartPosition
            )
            videoTimeListener?.onEndTime(
                String.format(
                    "%s %s",
                    TrimVideoUtils.stringForTime(mEndPosition),
                    seconds
                ), mEndPosition
            )
        }
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0) return
        val position = mVideoView.currentPosition
        if (all) {
            for (item in mListeners!!) {
                item.updateProgress(position, mDuration, (position * 100 / mDuration).toFloat())
            }
        } else {
            mListeners!![1].updateProgress(
                position,
                mDuration,
                (position * 100 / mDuration).toFloat()
            )
        }
    }


    private fun setVideoInformationVisibility(visible: Boolean) {
        mTimeInfoContainer!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun setOnTrimVideoListener(onTrimVideoListener: OnTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener
    }

    private fun setOnHgLVideoListener(onHgLVideoListener: OnHgLVideoListener) {
        mOnHgLVideoListener = onHgLVideoListener
    }

    fun setDestinationPath(finalPath: String) {
        mFinalPath = finalPath
        Log.d(mContext.javaClass.simpleName, "Setting custom path $mFinalPath")
    }

    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    private fun setMaxDuration(maxDuration: Int) {
        mMaxDuration = maxDuration
    }

    private fun setVideoURI(videoURI: Uri) {
        mSrc = videoURI
        if (mOriginSizeFile == 0L) {
            val file = File(mSrc!!.path)
            mOriginSizeFile = file.length()
        }
        mVideoView.setVideoURI(mSrc)
        mVideoView.requestFocus()
        timeLineView!!.setVideo(mSrc!!)
    }

    private class MessageHandler(view: MergeBinder) : Handler() {
        private val mView: WeakReference<MergeBinder> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view?.mVideoView == null) {
                return
            }
            view.notifyProgressUpdate(true)
            if (view.mVideoView.isPlaying) {
                sendEmptyMessageDelayed(0, 10)
            }
        }
    }

    private fun onCancelClicked() {
        mVideoView.stopPlayback()
    }

    private fun onSaveClicked(videoTimeLine: VideoTimeLine) {
        val finalDuration = mEndPosition - mStartPosition

        //check if timeinmillis duration is less than 4 minutes
        if (finalDuration < 240000) {
            mVideoView.pause()
            if (!fragment.mHandlePlaySwitch!!.isSelected)
                fragment.mHandlePlaySwitch!!.isSelected = true
            if (mOnTrimVideoListener != null)
                Log.v(
                    mContext.javaClass.simpleName,
                    "mStartPosition: $mStartPosition mEndPosition: $mEndPosition"
                )
            mOnTrimVideoListener!!.onTrimStarted(mStartPosition, mEndPosition, videoTimeLine.path)
        } else {
            Toast.makeText(mContext, "Please trim video under 4 minutes", Toast.LENGTH_SHORT).show()
        }
    }
}
