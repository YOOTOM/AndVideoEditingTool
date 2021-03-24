package com.anibear.andvideoeditingtool.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import com.anibear.andvideoeditingtool.R;
import com.github.lzyzsd.circleprogress.DonutProgress;

/**
 * Created by milktea on 2016-10-06.
 */

public class SimpleProgressDialog extends Dialog {
    private View progressBar;
    private int progress = 0;
    private Context mContext;
    private CharSequence mTitle;

    public SimpleProgressDialog(Context context) {
        super(context, R.style.SimpleProgressDialog);
        this.mContext = context;
    }

    public static SimpleProgressDialog show(Context context, CharSequence title) {
        return show(context, title, true);
    }

    public static SimpleProgressDialog show(Context context, CharSequence title, boolean indeterminate) {
        return show(context, title, indeterminate, false, null);
    }

    public static SimpleProgressDialog show(Context context, CharSequence title, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        try {
            SimpleProgressDialog dialog = new SimpleProgressDialog(context);
            dialog.setTitle(title);
            dialog.setCancelable(cancelable);
            dialog.setOnCancelListener(cancelListener);

            if (indeterminate) {
                dialog.progressBar = new ProgressBar(context);
            } else {
                DonutProgress donutProgress = new DonutProgress(context);
                donutProgress.setFinishedStrokeColor(ContextCompat.getColor(context, R.color.colorAccent));
                donutProgress.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                dialog.progressBar = donutProgress;
            }
            dialog.addContentView(dialog.progressBar,
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT));

            dialog.show();

            return dialog;
        } catch (Exception e) {
            return null;
        }
    }

    public static SimpleProgressDialog show(Context context, CharSequence title, boolean indeterminate, boolean cancelable) {
        return show(context, title, indeterminate, cancelable, null);
    }

    public void setProgress(Activity activity, final int progress) {
        if (progressBar instanceof ContentLoadingProgressBar) {
            if (progress != this.progress) {
                this.progress = progress;
                new UpdateAsyncTask().execute(progress);
            }
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... progress) {
            return progress[0];
        }

        @Override
        protected void onPostExecute(Integer result) {
            ((ContentLoadingProgressBar) progressBar).setProgress(progress);
        }
    }
}
