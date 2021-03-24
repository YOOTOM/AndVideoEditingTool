package com.anibear.andvideoeditingtool.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.TelegramActivity
import com.anibear.andvideoeditingtool.adapter.MergeAdapter
import com.anibear.andvideoeditingtool.adapter.VideoControllerAdapter
import com.anibear.andvideoeditingtool.adapter.VideoOptionsAdapter
import com.anibear.andvideoeditingtool.exoplayerfilter.MovieWrapperView
import com.anibear.andvideoeditingtool.interfaces.CommunicateListener
import com.anibear.andvideoeditingtool.interfaces.VideoOptionListener
import com.anibear.andvideoeditingtool.interfaces.VideoTimeListener
import com.anibear.andvideoeditingtool.kotlinffmpeg.callback.FFMpegCallback
import com.anibear.andvideoeditingtool.kotlinffmpeg.dialogs.VideoDialog
import com.anibear.andvideoeditingtool.kotlinffmpeg.tools.video.VideoMerger
import com.anibear.andvideoeditingtool.kotlinffmpeg.tools.video.VideoResizer
import com.anibear.andvideoeditingtool.models.VideoControllerInfo
import com.anibear.andvideoeditingtool.models.VideoInfo
import com.anibear.andvideoeditingtool.models.VideoTimeLine
import com.anibear.andvideoeditingtool.utils.CommonMethods
import com.anibear.andvideoeditingtool.utils.Constant
import com.anibear.andvideoeditingtool.utils.Prop
import com.anibear.andvideoeditingtool.utils.Utils
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnHgLVideoListener
import com.anibear.andvideoeditingtool.videoTrimmer.interfaces.OnTrimVideoListener
import com.anibear.andvideoeditingtool.videoTrimmer.utils.BackgroundExecutor
import com.anibear.andvideoeditingtool.videoTrimmer.utils.TrimVideoUtils
import com.anibear.andvideoeditingtool.videoTrimmer.view.VideoEditView
import com.anibear.andvideoeditingtool.viewholders.MergeHolder
import com.anibear.andvideoeditingtool.viewholders.VideoControllerHolder
import com.anibear.andvideoeditingtool.widget.SimpleProgressDialog
import com.facebook.drawee.backends.pipeline.Fresco
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.ceil

class MasterProcessorFragment() : Fragment(),
    BaseCreatorDialogFragment.CallBacks,
    VideoOptionListener, OnHgLVideoListener, OnTrimVideoListener,
    VideoEditView.OnSelectTimeChangeListener,
    DiscreteScrollView.ScrollStateChangeListener<MergeHolder>,
    DiscreteScrollView.OnItemChangedListener<MergeHolder> {

    private var tagName: String = MasterProcessorFragment::class.java.simpleName
    private lateinit var rootView: View
    private var videoUri: Uri? = null
    private var videoFile: File? = null
    private var permissionList: ArrayList<String> = ArrayList()
    private lateinit var preferences: SharedPreferences

    private var selectedVideoFile: File? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    var mDiscreteScrollView: DiscreteScrollView? = null
    var mScrollViewVideoController: DiscreteScrollView? = null
    private lateinit var recyclerViewVideoEditMenu: RecyclerView
    private lateinit var VideoOptionsAdapter: VideoOptionsAdapter
    private lateinit var mergeAdapter: MergeAdapter
    private lateinit var videoControllerAdapter: VideoControllerAdapter
    private var videoOptions: ArrayList<String> = ArrayList()
    private var orientationLand: Boolean = false
    private var videoSave: ImageView? = null
    private var mContext: Context? = null
    private var mStartTime: TextView? = null
    var nextPosition: Int = 0
    private var nextPositionFlag: Boolean = false
    private var previousPositionFlag: Boolean = false
    private var mEndTime: TextView? = null
    private var mTotalTime: TextView? = null
    private var mStartTimePostion: Int = 0
    private var mEndTimePostion: Int = 0
    private var mCurrentTime: Int = 0
    var mHandlePlaySwitch: AppCompatImageView? = null
    private var masterProcessorFragment: MasterProcessorFragment? = null
    var mVideoInfo: VideoInfo? = null
    var mVideoView: VideoView? = null
    private var simpleProgressDialog: SimpleProgressDialog? = null
    private var mMovieWrapperView: MovieWrapperView? = null
    var mCurrentPosition: Int = 0

    interface ProgressPublish {
        fun onProgress(progress: String)

        fun onDismiss()
    }

    companion object {
        lateinit var onProgress: ProgressPublish

        fun setProgressListener(onProgress: ProgressPublish) {
            this.onProgress = onProgress
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.video_processor_fragment, container, false)
        initView(rootView)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (mVideoView?.visibility == View.INVISIBLE) {
            mVideoView?.visibility = View.VISIBLE
            mMovieWrapperView?.visibility = View.INVISIBLE
        }
    }

    private fun initView(rootView: View?) {
        masterProcessorFragment = this
        mVideoView = rootView?.findViewById(R.id.video_loader)
        videoSave = rootView?.findViewById(R.id.img_videoSave)
        mStartTime = rootView?.findViewById(R.id.txt_startTime)
        mEndTime = rootView?.findViewById(R.id.txt_endTime)
        mTotalTime = rootView?.findViewById(R.id.txt_totalTime)
        mHandlePlaySwitch = rootView?.findViewById(R.id.Img_video_play_switch)
        mMovieWrapperView = rootView?.findViewById(R.id.layout_movie_wrapper)
        preferences = activity!!.getSharedPreferences("fetch_permission", Context.MODE_PRIVATE)
        mDiscreteScrollView = rootView?.findViewById(R.id.discreteScrollview)
        mScrollViewVideoController = rootView?.findViewById(R.id.scrollview_video_controller)
        recyclerViewVideoEditMenu = rootView?.findViewById(R.id.recycler_view_video_edit_menu)!!
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerViewVideoEditMenu.layoutManager = linearLayoutManager
        mContext = context

        //add video editing ons
        videoOptions.add(Constant.GALLERY)
        videoOptions.add(Constant.FLIRT)
        videoOptions.add(Constant.TRIM)
        videoOptions.add(Constant.DELETE)

        VideoOptionsAdapter =
            VideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        VideoOptionsAdapter.notifyDataSetChanged()
        recyclerViewVideoEditMenu.adapter = VideoOptionsAdapter
        checkStoragePermission(Constant.PERMISSION_STORAGE)

        videoSave?.setOnClickListener {
            mVideoView?.pause()
            if (selectedVideoFile == null) {
                Utils.showGlideToast(
                    activity!!,
                    "비디오를 선택/추가해주세요."
                )
                return@setOnClickListener
            }

            if (simpleProgressDialog == null) {
                simpleProgressDialog = SimpleProgressDialog.show(context, null, true, false);
            }

            //Kill previous running process
            stopRunningProcess()

            if (!isRunning()) {
                val handler = Handler(Looper.getMainLooper())
                VideoMerger.with(context!!)
                    .setVideoFiles(VideoTimeLine.getList())
                    .setOutputPath(Utils.outputPath + "video")
                    .setOutputFileName("merged_" + System.currentTimeMillis() + ".mp4")
                    .setCallback(object : FFMpegCallback {
                        @SuppressLint("LogNotTimber")
                        override fun onProgress(progress: String) {
                            Log.d("VIDEOMERHER", "prgress: $progress")
                        }

                        override fun onSuccess(convertedFile: File, type: String) {
                            handler.postDelayed(Runnable {
                                simpleProgressDialog?.dismiss()
                                Toast.makeText(mContext, "비디오 생성 및 저장 완료.", Toast.LENGTH_SHORT)
                                    .show()
                            }, 0)
                            VideoDialog.show(activity!!.supportFragmentManager, convertedFile)
                        }

                        override fun onFailure(error: Exception) {
                            handler.postDelayed(Runnable {
                                simpleProgressDialog?.dismiss()
                                Toast.makeText(mContext, "비디오 생성 및 저장 샐패.", Toast.LENGTH_SHORT)
                                    .show()
                            }, 0)
                        }

                        override fun onNotAvailable(error: Exception) {
                            handler.postDelayed(Runnable {
                                simpleProgressDialog?.dismiss()
                                Toast.makeText(mContext, "비디오 생성 및 저장 샐패.", Toast.LENGTH_SHORT)
                                    .show()
                            }, 0)
                        }

                        override fun onFinish() {
                            simpleProgressDialog?.dismiss()
                        }

                    })
                    .merge("720", "1280")
            } else {
                showInProgressToast()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //for playing video in landscape mode
        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v(tagName, "orientation: ORIENTATION_LANDSCAPE")
            orientationLand = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.v(tagName, "orientation: ORIENTATION_PORTRAIT")
            orientationLand = false
        }
        VideoOptionsAdapter =
            VideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        recyclerViewVideoEditMenu.adapter = VideoOptionsAdapter
        VideoOptionsAdapter.notifyDataSetChanged()
    }

    override fun onAudioFileProcessed(convertedAudioFile: File) {
    }

    override fun reInitPlayer() {
    }

    override fun onDidNothing() {
    }

    override fun onFileProcessed(file: File) {
    }

    override fun showLoading(isShow: Boolean) {
    }

    override fun showLoading(isShow: Boolean, duration: Int) {
    }

    override fun updateLoading(progress: Int) {
    }

    override fun getFile(): File? {
        return selectedVideoFile
    }


    private fun checkAllPermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if (blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if (isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constant.RECORD_VIDEO)
            }
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoFile = Utils.createVideoFile(context!!)
            Log.v(tagName, "videoPath1: " + videoFile!!.absolutePath)
            videoUri = FileProvider.getUriForFile(
                context!!,
                "com.obs.marveleditor.provider", videoFile!!
            )
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240) //4 minutes
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFile)
            startActivityForResult(cameraIntent, Constant.RECORD_VIDEO)
        }
    }

    private fun checkStoragePermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if (blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if (isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constant.ADD_ITEMS_IN_STORAGE)
            }
        }
    }

    private var isFirstTimePermission: Boolean
        get() = preferences.getBoolean("isFirstTimePermission", false)
        set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime)
            .apply()

    private val isMarshmallow: Boolean
        @SuppressLint("ObsoleteSdkInt")
        get() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) or (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)

    private fun checkHasPermission(
        context: Activity?,
        permissions: Array<String>?
    ): ArrayList<String> {
        permissionList = ArrayList()
        if (isMarshmallow && context != null && permissions != null) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(permission)
                }
            }
        }
        return permissionList
    }

    private fun isPermissionBlocked(context: Activity?, permissions: ArrayList<String>?): Boolean {
        if (isMarshmallow && context != null && permissions != null && isFirstTimePermission) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("LogNotTimber")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) return

        when (requestCode) {

            Constant.VIDEO_GALLERY -> {
                data?.let {
                    setFilePath(
                        resultCode,
                        it,
                        Constant.VIDEO_GALLERY,
                        object : CommunicateListener<String, File> {
                            override fun Result(path: String, file: File) {
                                mVideoInfo = VideoInfo(path, 0, file)
                                addDisplayList(
                                    Uri.fromFile(file), CommonMethods.getMediaDuration(
                                        context,
                                        Uri.fromFile(file)
                                    ),
                                    mVideoInfo!!, false, 0
                                )
                                if (mVideoView != null) {
                                    displayList(mVideoView!!, masterProcessorFragment!!)
                                }
                                simpleProgressDialog?.dismiss()
                            }
                        })
                }
            }

            Constant.MAIN_VIDEO_TRIM -> {

            }
        }
    }

    private fun setFilePath(
        resultCode: Int,
        data: Intent,
        mode: Int,
        communicateListener: CommunicateListener<String, File>
    ) {

        if (resultCode == RESULT_OK) {
            try {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver
                    .query(selectedImage!!, filePathColumn, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor
                        .getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if (mode == Constant.VIDEO_GALLERY) {
                        selectedVideoFile = File(filePath)
                        getVideoSize(
                            mContext!!,
                            Uri.parse(filePath),
                            object : CommunicateListener<String, String> {
                                override fun Result(width: String, height: String) {
                                    /*if (height.toInt() > 720) {
                                        stopRunningProcess()
                                        if (simpleProgressDialog == null) {
                                            simpleProgressDialog = SimpleProgressDialog.show(
                                                context,
                                                null,
                                                true,
                                                false
                                            );
                                        }
                                        if (!isRunning()) {
                                            val handler = Handler(Looper.getMainLooper())
                                            handler.postDelayed(Runnable {
                                                Toast.makeText(
                                                    mContext,
                                                    "비디오 리싸이징을 시작합니다.",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }, 0)
                                            VideoResizer.with(mContext!!)
                                                .setFile(selectedVideoFile!!)
                                                .setOutputPath(Utils.outputPath + "video")
                                                .setOutputFileName("resized_" + System.currentTimeMillis() + ".mp4")
                                                .setCallback(object : FFMpegCallback {
                                                    override fun onProgress(progress: String) {
                                                    }

                                                    override fun onSuccess(
                                                        convertedFile: File,
                                                        type: String
                                                    ) {
                                                        handler.postDelayed(Runnable {
                                                            Toast.makeText(
                                                                mContext,
                                                                "비디오 리싸이징 완료.",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }, 0)
                                                        communicateListener.Result(
                                                            convertedFile.toString(),
                                                            convertedFile
                                                        )
                                                    }

                                                    override fun onFailure(error: Exception) {
                                                        handler.postDelayed(Runnable {
                                                            simpleProgressDialog?.dismiss()
                                                            Toast.makeText(
                                                                mContext,
                                                                "비디오 리싸이징 실패.",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }, 0)
                                                    }

                                                    override fun onNotAvailable(error: Exception) {
                                                        handler.postDelayed(Runnable {
                                                            simpleProgressDialog?.dismiss()
                                                            Toast.makeText(
                                                                mContext,
                                                                "비디오 리싸이징 실패.",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }, 0)
                                                    }

                                                    override fun onFinish() {
                                                    }

                                                }).resize("720", "1280")
                                        } else {
                                            showInProgressToast()
                                        }
                                    } else {*/
                                        communicateListener.Result(filePath, selectedVideoFile!!)
                                    //}
                                }
                            })
                    }
                }
            } catch (e: Exception) {

            }
        }
    }


    private fun displayList(videoView: VideoView, fragment: MasterProcessorFragment) {
        mergeAdapter = MergeAdapter(VideoTimeLine.getList(), videoView, fragment)
        CoroutineScope(Main).launch {
            mDiscreteScrollView?.apply {
                setSlideOnFling(true)
                setItemTransitionTimeMillis(100)
                adapter = mergeAdapter
                addOnItemChangedListener(fragment)
                addScrollStateChangeListener(fragment)
                setSlideOnFling(false)
            }
        }
    }

    private var mVideoControllerHolder: VideoControllerHolder? = null
    fun initScrollViewForVideoController(fragment: MasterProcessorFragment) {
        videoControllerAdapter = VideoControllerAdapter(VideoControllerInfo.getList(), fragment)
        CoroutineScope(Main).launch {
            mScrollViewVideoController?.apply {
                setSlideOnFling(true)
                adapter = videoControllerAdapter
                setItemTransitionTimeMillis(10)
                setItemTransformer(
                    ScaleTransformer.Builder()
                        .setMinScale(0.8f)
                        .build()
                )
                addOnItemChangedListener(object :
                    DiscreteScrollView.OnItemChangedListener<VideoControllerHolder> {
                    override fun onCurrentItemChanged(
                        viewHolder: VideoControllerHolder?,
                        adapterPosition: Int
                    ) {
                        mVideoControllerHolder = viewHolder
                        viewHolder?.showEffect()
                        mCurrentTime = 0
                        if (VideoTimeLine.getList().size > adapterPosition && adapterPosition != 0) {
                            for (j in 0..VideoTimeLine.getList().size) {
                                if (j < adapterPosition) {
                                    mCurrentTime += VideoTimeLine.getList()[j].maxDuration
                                }
                            }
                        }
                        mCurrentPosition = adapterPosition
                        mVideoInfo = VideoTimeLine.getList()[adapterPosition].videoInfo
                    }
                })
                addScrollStateChangeListener(
                    object :
                        DiscreteScrollView.ScrollStateChangeListener<VideoControllerHolder> {
                        override fun onScroll(
                            scrollPosition: Float,
                            currentPosition: Int,
                            newPosition: Int,
                            currentHolder: VideoControllerHolder?,
                            newCurrent: VideoControllerHolder?
                        ) {
                        }

                        override fun onScrollEnd(
                            currentItemHolder: VideoControllerHolder,
                            adapterPosition: Int
                        ) {
                            mDiscreteScrollView?.smoothScrollToPosition(adapterPosition)
                            mVideoInfo = VideoTimeLine.getList()[adapterPosition].videoInfo
                        }

                        override fun onScrollStart(
                            currentItemHolder: VideoControllerHolder,
                            adapterPosition: Int
                        ) {
                            currentItemHolder.hidenEffect()
                        }
                    })
            }
        }


    }

    //path 교체
    fun setartPocessingVideoDataChange(
        position: Int,
        videoPath: String,
    ) {
        val videoFile = File(videoPath)
        mVideoInfo = VideoInfo(videoPath, 0, videoFile)
        addDisplayList(
            Uri.fromFile(videoFile),
            CommonMethods.getMediaDuration(context, Uri.fromFile(videoFile)),
            mVideoInfo!!, true, position
        )
    }

    private fun addDisplayList(
        path: Uri?,
        maxDuration: Int,
        videoInfo: VideoInfo,
        isChangeItem: Boolean,
        position: Int
    ) {
        if (isChangeItem) {
            VideoTimeLine.changeListItem(
                position, path!!,
                maxDuration,
                videoInfo
            )
            mCurrentTime = 0
            if (VideoTimeLine.getList().size > mCurrentPosition && mCurrentPosition != 0) {
                for (j in 0..VideoTimeLine.getList().size) {
                    if (j < mCurrentPosition) {
                        mCurrentTime += VideoTimeLine.getList()[j].maxDuration
                    }
                }
            }
            var totalTime = 0
            for (i in 0 until VideoTimeLine.getList().size) {
                totalTime += VideoTimeLine.getList()[i].maxDuration
            }
            VideoTimeLine.resetTotalMaxDuration(totalTime)

            if (mVideoView != null)
                displayList(mVideoView!!, this)
            getBitmap(1000, path!!, maxDuration, isChangeItem, position)
        } else {
            VideoTimeLine.setList(path!!, maxDuration, videoInfo)
            getBitmap(1000, path, maxDuration, isChangeItem, position)
        }

    }

    fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    override fun openGallery() {
        checkPermission(Constant.VIDEO_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun openCamera() {
        checkAllPermission(Constant.PERMISSION_CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            Constant.VIDEO_GALLERY -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity as Activity,
                            permission
                        )
                    ) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Utils.refreshGalleryAlone(context!!)
                            val i = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            )
                            i.type = "video/*"
                            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            startActivityForResult(i, Constant.VIDEO_GALLERY)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }
        }
    }

    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    private fun showBottomSheetDialogFragment(bottomSheetDialogFragment: BottomSheetDialogFragment) {
        val bundle = Bundle()
        bottomSheetDialogFragment.arguments = bundle
        bottomSheetDialogFragment.show(fragmentManager!!, bottomSheetDialogFragment.tag)
    }

    override fun videoOption(option: String) {
        //based on selected video editing on - helper, file is passed
        when (option) {
            Constant.GALLERY -> {
                openGallery()
            }
            //필터
            Constant.FLIRT -> {
                if (selectedVideoFile == null) {
                    Utils.showGlideToast(
                        activity!!,
                        "비디오를 선택/추가해주세요."
                    )
                    return
                }
                mVideoInfo?.file?.let { file ->
                    mVideoView?.visibility = View.INVISIBLE
                    mMovieWrapperView?.visibility = View.VISIBLE
                    Fresco.initialize(mContext)
                    val filterFragment =
                        FilterFragment(mCurrentPosition, mMovieWrapperView!!, mVideoView!!)
                    filterFragment.setHelper(this@MasterProcessorFragment)
                    filterFragment.setFilePathFromSource(file)
                    filterFragment.setVideoDuration(mVideoView!!.duration)
                    showBottomSheetDialogFragment(filterFragment)
                    filterFragment.setVideoChangedListener(object :
                        CommunicateListener<Int, String> {
                        override fun Result(position: Int, path: String) {
                            setartPocessingVideoDataChange(position, path)
                        }
                    })
                }

            }

            //자르기
            Constant.TRIM -> {
                if (selectedVideoFile == null) {
                    Utils.showGlideToast(
                        activity!!,
                        "비디오를 선택/추가해주세요."
                    )
                    return
                }
                if (mVideoInfo != null) {
                    val intent = Intent(context, TelegramActivity::class.java)
                    intent.putExtra(Prop.MAIN_OBJ, Uri.parse(mVideoInfo!!.path))
                    intent.putExtra(Prop.VIDEO_POS, mCurrentPosition)
                    context!!.startActivity(intent)
                    TelegramActivity().setVideoChangedListener(object :
                        CommunicateListener<Int, String> {
                        override fun Result(position: Int, path: String) {
                            setartPocessingVideoDataChange(position, path)
                        }
                    })
                }
            }

            //음악
            Constant.MUSIC -> {

            }

            Constant.PLAYBACK -> {

            }

            Constant.TEXT -> {

            }

            Constant.OBJECT -> {

            }

            Constant.MERGE -> {

            }

            Constant.DELETE -> {
                if (selectedVideoFile == null) {
                    Utils.showGlideToast(
                        activity!!,
                        "비디오를 선택/추가해주세요."
                    )
                    return
                }

                if (mVideoView != null) {
                    if (mCurrentPosition == 0) {
                        Utils.showGlideToast(
                            activity!!,
                            "비디오를 선택/추가해주세요."
                        )
                        return
                    }
                    VideoTimeLine.removeAt(mCurrentPosition)
                    VideoControllerInfo.removeAt(mCurrentPosition)
                    displayList(mVideoView!!, this)
                    initScrollViewForVideoController(this)
                    mCurrentTime = 0
                    if (VideoTimeLine.getList().size > mCurrentPosition && mCurrentPosition != 0) {
                        for (j in 0..VideoTimeLine.getList().size) {
                            if (j < mCurrentPosition) {
                                mCurrentTime += VideoTimeLine.getList()[j].maxDuration
                            }
                        }
                    }
                    var totalTime = 0
                    for (i in 0 until VideoTimeLine.getList().size) {
                        totalTime += VideoTimeLine.getList()[i].maxDuration
                    }
                    VideoTimeLine.resetTotalMaxDuration(totalTime)

                }
            }
        }
    }

    override fun onVideoPrepared() {
    }

    override fun onTrimStarted(startPosition: Int, endPosition: Int) {
    }

    private fun stopRunningProcess() {
        FFmpeg.getInstance(activity).killRunningProcesses()
    }

    private fun isRunning(): Boolean {
        return FFmpeg.getInstance(activity).isFFmpegCommandRunning
    }

    private fun showInProgressToast() {
        Toast.makeText(
            activity,
            "Operation already in progress! Try again in a while.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onTrimStarted(startPosition: Int, endPosition: Int, path: Uri) {
    }

    override fun getResult(uri: Uri?) {

    }

    override fun cancelAction() {

    }

    override fun onError(message: String?) {

    }

    override fun onScrollStart(currentItemHolder: MergeHolder, adapterPosition: Int) {
        if (simpleProgressDialog == null) {
            simpleProgressDialog = SimpleProgressDialog.show(context, null, true, true);
        }
    }

    override fun onScrollEnd(currentItemHolder: MergeHolder, adapterPosition: Int) {
    }

    override fun onScroll(
        scrollPosition: Float,
        currentPosition: Int,
        newPosition: Int,
        currentHolder: MergeHolder?,
        newCurrent: MergeHolder?
    ) {
    }

    private var mViewHolder: MergeHolder? = null
    override fun onCurrentItemChanged(viewHolder: MergeHolder?, adapterPosition: Int) {
        mViewHolder = viewHolder
        viewHolder?.changedPos(object : VideoTimeListener {
            override fun onStartTime(str: String, postion: Int) {
                val seconds: String = mContext!!.getString(R.string.short_seconds)
                val totalTime: Int = VideoTimeLine.getTotalMaxDuration()
                val currentTimeString: String =
                    String.format(
                        "%s %s",
                        TrimVideoUtils.stringForTime(postion + mCurrentTime),
                        seconds
                    )
                val totalTimeString: String =
                    String.format("%s %s", TrimVideoUtils.stringForTime(totalTime), seconds)
                mTotalTime?.text = "$currentTimeString / $totalTimeString "
                mStartTime?.text = str
                mStartTimePostion = postion
                if (mStartTimePostion == mEndTimePostion || mEndTime?.text == mStartTime?.text) {
                    if (nextPosition == adapterPosition) {
                        if (nextPositionFlag) {
                            nextPositionFlag = false
                            if (VideoTimeLine.getList().size - 1 > adapterPosition) {
                                mDiscreteScrollView?.smoothScrollToPosition(adapterPosition + 1)
                                mScrollViewVideoController?.smoothScrollToPosition(
                                    adapterPosition + 1
                                )
                            }
                        }
                    }
                } else if (postion > 1500) {
                    nextPosition = adapterPosition
                    if (!previousPositionFlag) {
                        previousPositionFlag = true
                        nextPositionFlag = true
                        simpleProgressDialog?.dismiss()
                        simpleProgressDialog = null
                    }
                } else {
                    if (str == "00:00 sec") {
                        if (mStartTimePostion == 0) {
                            if (nextPosition == adapterPosition) {
                                if (previousPositionFlag) {
                                    previousPositionFlag = false
                                    if (VideoTimeLine.getList().size > adapterPosition) {
                                        if (adapterPosition == 0) {
                                            return
                                        }
                                        mDiscreteScrollView?.smoothScrollToPosition(
                                            adapterPosition - 1
                                        )
                                        mScrollViewVideoController?.smoothScrollToPosition(
                                            adapterPosition - 1
                                        )
                                    }
                                } else if (nextPositionFlag) {
                                    nextPositionFlag = false
                                    if (VideoTimeLine.getList().size - 1 > adapterPosition) {
                                        if (mCurrentPosition > adapterPosition) {
                                            mDiscreteScrollView?.smoothScrollToPosition(
                                                adapterPosition + 1
                                            )
                                            mScrollViewVideoController?.smoothScrollToPosition(
                                                adapterPosition + 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onEndTime(str: String, postion: Int) {
                mEndTime?.text = str
                if (mEndTimePostion != postion) {
                    mEndTimePostion = postion
                }
                if (nextPosition != adapterPosition) {
                    nextPositionFlag = true
                }
                simpleProgressDialog?.dismiss()
                simpleProgressDialog = null
            }
        })
    }

    private fun getVideoSize(
        context: Context,
        videoUri: Uri,
        communicateListener: CommunicateListener<String, String>
    ) {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(context, videoUri)
        val height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        communicateListener.Result(width, height)
    }

    private fun getBitmap(
        viewWidth: Int,
        mVideoUri: Uri,
        maxDuration: Int,
        isChangeItem: Boolean,
        position: Int
    ) {
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            override fun execute() {
                try {
                    val mHeightView =
                        mContext!!.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(mContext, mVideoUri)
                    val videoLengthInMs = mediaMetadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    ).toInt() * 1000.toLong()
                    val thumbWidth: Int = mHeightView * 2
                    val thumbHeight: Int = mHeightView * 2
                    val numThumbs =
                        ceil(viewWidth.toFloat() / thumbWidth.toDouble()).toInt()
                    val interval = videoLengthInMs / numThumbs
                    for (i in 0 until numThumbs) {
                        var bitmap = mediaMetadataRetriever.getFrameAtTime(
                            i * interval,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        try {
                            bitmap =
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    thumbWidth - 10,
                                    thumbHeight,
                                    false
                                )
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        if (i == 0) {
                            if (isChangeItem) {
                                VideoControllerInfo.changeListItem(position, bitmap, maxDuration)
                            } else {
                                VideoControllerInfo.setList(bitmap, maxDuration)
                            }
                            initScrollViewForVideoController(masterProcessorFragment!!)
                        }
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e)
                }
            }
        }
        )
    }

    override fun selectTimeChange(startTime: Long, endTime: Long) {
    }

    override fun playChange(isPlayVideo: Boolean) {
    }

    override fun videoProgressUpdate(currentTime: Long, isVideoPlaying: Boolean) {
    }

}