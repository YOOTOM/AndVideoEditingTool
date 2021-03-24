package com.anibear.andvideoeditingtool.kotlinffmpeg.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.anibear.andvideoeditingtool.R
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment
import com.github.hiteshsondhi88.libffmpeg.FFmpeg

/**
 * Created by umair on 18/01/2018.
 */
class ProgressDialog : DialogFragment(), MasterProcessorFragment.ProgressPublish {

    var text: String = ""

    lateinit var progress_text: AppCompatTextView
    lateinit var name_text: AppCompatTextView
    lateinit var stopButton: Button

    override fun onProgress(progress: String) {
        this.text = progress
        progress_text.text = text
    }

    override fun onDismiss() {
        dismiss()
    }

    companion object {
        val TAG = ProgressDialog::javaClass.name
        var name: String = ""

        fun show(fragmentManager: FragmentManager, name: String) {
            ProgressDialog().show(fragmentManager, TAG)
            this.name = name
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        MasterProcessorFragment.setProgressListener(this)

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_progress, null)

        progress_text = view.findViewById<AppCompatTextView>(R.id.txt_progress)
        name_text = view.findViewById<AppCompatTextView>(R.id.txt_name)
        stopButton = view.findViewById<Button>(R.id.stop)

        progress_text.text = text
        name_text.text = name

        stopButton.setOnClickListener {
            FFmpeg.getInstance(activity!!).killRunningProcesses()
            dismiss()
        }

        return AlertDialog.Builder(activity)
                .setCancelable(false)
                .setView(view)
                .setTitle("Running FFMpeg Commands")
                .create()
    }
}
