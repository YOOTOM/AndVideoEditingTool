package com.anibear.andvideoeditingtool.kotlinffmpeg.tools.video

import android.content.Context
import com.anibear.andvideoeditingtool.kotlinffmpeg.callback.FFMpegCallback
import com.anibear.andvideoeditingtool.kotlinffmpeg.tools.OutputType
import com.anibear.andvideoeditingtool.kotlinffmpeg.utils.Utils
import com.anibear.andvideoeditingtool.models.VideoTimeLine
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File
import java.io.IOException

/**
 * Created by Umair_Adil on 31/07/2018.
 */

class VideoMerger private constructor(private val context: Context) {

    private var videos: ArrayList<VideoTimeLine>? = null
    private var callback: FFMpegCallback? = null
    private var outputPath = ""
    private var outputFileName = ""

    fun setVideoFiles(originalFiles: ArrayList<VideoTimeLine>): VideoMerger {
        this.videos = originalFiles
        return this
    }

    fun setCallback(callback: FFMpegCallback): VideoMerger {
        this.callback = callback
        return this
    }

    fun setOutputPath(output: String): VideoMerger {
        this.outputPath = output
        return this
    }

    fun setOutputFileName(output: String): VideoMerger {
        this.outputFileName = output
        return this
    }

    /*
     * For concat'ing videos there are a few properties that have to be equal. So before executing the concat, you better "normalize" or "format" all you inputs to share these properties:
     * Video resolution
     * Video framerate (actually frame rate dont need to match, but the timescale)
     * Video interlacing
     * Video colorspace (e.g. YUV 4:2:0)
     * Video codec
     * Audio samplerate
     * Audio channels and track / layout
     * Audio codec(s)
     */
    fun merge(width:String,height:String) {

        if (videos == null || videos!!.isEmpty()) {
            callback!!.onFailure(IOException("File not exists"))
            return
        }

        /*for (v in videos!!) {
            val file = File(v.path.toString())
            if (!file.canRead()) {
                callback!!.onFailure(IOException("Can't read the file. Missing permission?"))
                return
            }
        }*/

        val outputLocation = Utils.getConvertedFile(outputPath, outputFileName)

        val inputCommand = arrayListOf<String>()

        //Add all paths
        for (i in videos!!) {
            //val file = File(i.path.toString())
            inputCommand.add("-i")
            inputCommand.add(i.path.toString().substring(7, i.path.toString().length))
        }
        //Apply filter graph
        inputCommand.add("-filter_complex")

        //Compose concatenation commands
        val stringBuilder = StringBuilder()

        //'setpts' and 'asetpts' will prevent a jerky output due to presentation timestamp issues
        //Set SAR,DAR & Scale to merge/concatenate videos of different sizes
        //'setsar' filter sets the Sample (aka Pixel) Aspect Ratio for the filter output video
        for (i in 0 until videos!!.size) {
            if (i == 0) {
                stringBuilder.append("[$i:v]setpts=PTS-STARTPTS,setsar=1,setdar=4/3,fade=t=out:st=${videos!![i].maxDuration - 2}:d=1,scale=${width}x${height}[v$i]; [$i:a]asetpts=PTS-STARTPTS[a$i];")
            } else if (i == videos!!.size - 1) {
                stringBuilder.append("[$i:v]setpts=PTS-STARTPTS,setsar=1,setdar=4/3,fade=t=in:st=0:d=1,scale=${width}x${height}[v$i]; [$i:a]asetpts=PTS-STARTPTS[a$i];")
            } else {
                stringBuilder.append("[$i:v]setpts=PTS-STARTPTS,setsar=1,setdar=4/3,fade=t=in:st=0:d=1,,fade=t=out:st=${videos!![i].maxDuration - 1}:d=1,scale=${width}x${height}[v$i]; [$i:a]asetpts=PTS-STARTPTS[a$i];")
            }
        }

        for (i in 0 until videos!!.size) {
            stringBuilder.append("[v$i][a$i]")
        }

        //Concat command
        stringBuilder.append("concat=n=${videos!!.size}:v=1:a=1[v][a]")

        //Complete Command
        val cmd = arrayOf<String>(
            "-map",
            "[v]",
            "-map",
            "[a]",
            "-preset", //Presets can be ultrafast, superfast, veryfast, faster, fast, medium (default), slow and veryslow.
            "medium", //Using a slower preset gives you better compression, or quality per file size.
            "-crf", //Constant Rate Factor
            "18", //Value from 0 to 51, 23 is default, Large Value for highest quality
            outputLocation.path,
            "-y" //Overwrite output files without asking
        )

        val finalCommand = (inputCommand + stringBuilder.toString() + cmd).toTypedArray()

        try {
            FFmpeg.getInstance(context)
                .execute(finalCommand, object : ExecuteBinaryResponseHandler() {
                    override fun onStart() {}

                    override fun onProgress(message: String?) {
                        callback!!.onProgress(message!!)
                    }

                    override fun onSuccess(message: String?) {
                        Utils.refreshGallery(outputLocation.path, context)
                        callback!!.onSuccess(outputLocation, OutputType.TYPE_VIDEO)

                    }

                    override fun onFailure(message: String?) {
                        if (outputLocation.exists()) {
                            outputLocation.delete()
                        }
                        callback!!.onFailure(IOException(message))
                    }

                    override fun onFinish() {
                        callback!!.onFinish()
                    }
                })
        } catch (e: Exception) {
            callback!!.onFailure(e)
        } catch (e2: FFmpegCommandAlreadyRunningException) {
            callback!!.onNotAvailable(e2)
        }

    }

    companion object {

        val TAG = "VideoMerger"

        fun with(context: Context): VideoMerger {
            return VideoMerger(context)
        }
    }
}
