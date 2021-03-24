package com.anibear.andvideoeditingtool;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.anibear.andvideoeditingtool.interfaces.CommunicateListener;
import com.anibear.andvideoeditingtool.utils.OtherUtils;
import com.anibear.andvideoeditingtool.utils.Prop;
import com.anibear.andvideoeditingtool.utils.Utils;
import com.anibear.andvideoeditingtool.widget.SimpleProgressDialog;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.google.android.material.snackbar.Snackbar;

import org.telegram.messenger.CustomVideoTimelinePlayView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TelegramActivity extends AppCompatActivity {
    private static final int VIDEO_MIN_DURATION_MS = 3000;
    private static final int VIDEO_MAX_DURATION_MS = 60000;
    private static final long VIDEO_MAX_SIZE = 10 * 1024 * 1024;
    @BindView(R.id.videoViewWrapper)
    FrameLayout videoViewWrapper;
    @BindView(R.id.videoView)
    VideoView videoView;
    @BindView(R.id.playBtn)
    ImageView playBtn;
    @BindView(R.id.trimDurAndSizeTxt)
    TextView trimDurAndSizeTxt;
    @BindView(R.id.trimDurRangeTxt)
    TextView trimDurRangeTxt;
    @BindView(R.id.timelineView)
    CustomVideoTimelinePlayView timelineView;

    private Uri videoUri;
    private File videoFile;
    private float videoDuration;
    private long trimStartTime;
    private long trimEndTime;
    private long originalSize;
    private Runnable updateProgressRunnable;
    private int mPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer_telegram);
        setTitle("TRIM");
        setBackBtnVisible();
        ButterKnife.bind(this);
        initialize();
    }

    public void startTrimVideo(View view) {
        if (view.getId() == R.id.btn_trim) {
            startCodec(this, videoUri.toString(), getVideoFilePath(), trimStartTime, trimEndTime, new CommunicateListener<Boolean, String>() {
                @Override
                public void Result(Boolean success, String detail) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(getApplicationContext(), "TrimStatus : " + detail, Toast.LENGTH_LONG).show();
                                if (videoView.isPlaying()) {
                                    trimDurCounterTimer.cancel();
                                    videoView.pause();
                                    playBtn.setVisibility(View.VISIBLE);
                                }
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "TrimStatus : " + detail, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });

        }
    }

    private File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    @SuppressLint("SimpleDateFormat")
    private String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "trim_apply.mp4";
    }

    private void exportMp4ToGallery(Context context, String filePath) {
        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        context.getContentResolver().insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values
        );
        context.sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + filePath))
        );
    }

    private static CommunicateListener<Integer, String> videoChangeListener = null;

    public void setVideoChangedListener(CommunicateListener<Integer, String> communicateListener) {
        videoChangeListener = communicateListener;
    }

    private void startCodec(Context context, String inputVideoPath, String outputVideoPath, Long trimStartTime, Long trimEndTime, CommunicateListener<Boolean, String> communicateListener) {
        SimpleProgressDialog simpleProgressDialog = SimpleProgressDialog.show(context, null, true, true);
        new Mp4Composer(inputVideoPath, outputVideoPath)
                .size(720, 720)
                .trim(trimStartTime, trimEndTime)
                .listener(new Mp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                    }

                    @Override
                    public void onCompleted() {
                        exportMp4ToGallery(getApplicationContext(), outputVideoPath);
                        videoChangeListener.Result(mPosition, outputVideoPath);
                        communicateListener.Result(true, "Completed");
                        simpleProgressDialog.dismiss();
                    }

                    @Override
                    public void onCanceled() {
                        communicateListener.Result(false, "Canceled");
                        simpleProgressDialog.dismiss();
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        communicateListener.Result(false, "Failed");
                        simpleProgressDialog.dismiss();
                    }
                })
                .start();
    }

    protected void setBackBtnVisible() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Timer trimDurCounterTimer;

    @OnClick(R.id.videoViewWrapper)
    public void videoViewWrapper() {
        if (videoView.isPlaying()) {
            trimDurCounterTimer.cancel();
            videoView.pause();
            playBtn.setVisibility(View.VISIBLE);
        } else {
            trimDurCounterTimer = new Timer();
            trimDurCounterTimer.scheduleAtFixedRate(new TimerTask() {
                long currentTime;

                @Override
                public void run() {
                    currentTime = videoView.getCurrentPosition();
                    String trimRangeDurStr = OtherUtils.getMinuteSeconds(currentTime) + "-" + OtherUtils.getMinuteSeconds(trimEndTime);
                    runOnUiThread(() -> {
                        trimDurRangeTxt.setText(trimRangeDurStr);
                    });
                    if (currentTime >= trimEndTime) {
                        trimDurCounterTimer.cancel();
                        runOnUiThread(() -> {
                            videoView.pause();
                            videoView.seekTo((int) trimStartTime);
                            String trimRangeDurStr2 = OtherUtils.getMinuteSeconds(trimStartTime) + "-" + OtherUtils.getMinuteSeconds(trimEndTime);
                            trimDurRangeTxt.setText(trimRangeDurStr2);
                            playBtn.setVisibility(View.VISIBLE);
                        });
                    }
                }
            }, 0, 100);
            videoView.start();
            timelineView.post(updateProgressRunnable);
            playBtn.setVisibility(View.GONE);
        }
    }

    private void initialize() {
        videoUri = getIntent().getParcelableExtra(Prop.MAIN_OBJ);
        mPosition = getIntent().getIntExtra(Prop.VIDEO_POS, 0);
        assert videoUri != null;
        String path = videoUri.toString();
        videoFile = new File(path);

        updateProgressRunnable = () -> {
            if (videoView == null || !videoView.isPlaying()) {
                timelineView.removeCallbacks(updateProgressRunnable);
            }
            updatePlayProgress();
            timelineView.postDelayed(updateProgressRunnable, 17);
        };
        initVideo();
    }

    private void updatePlayProgress() {
        float progress = 0;
        if (videoView != null) {
            progress = videoView.getCurrentPosition() / (float) videoView.getDuration();
            if (timelineView.getVisibility() == View.VISIBLE) {
                progress -= timelineView.getLeftProgress();
                if (progress < 0) {
                    progress = 0;
                }
                progress /= (timelineView.getRightProgress() - timelineView.getLeftProgress());
                if (progress > 1) {
                    progress = 1;
                }
            }
        }
        timelineView.setProgress(progress);
    }

    private void updateVideoInfo() {
        trimStartTime = (long) Math.ceil(timelineView.getLeftProgress() * videoDuration);
        trimEndTime = (long) Math.ceil(timelineView.getRightProgress() * videoDuration);
        long estimatedDuration = trimEndTime - trimStartTime;
        long estimatedSize = (int) (originalSize * ((float) estimatedDuration / videoDuration));
        String videoTimeSize = String.format(Locale.US, "%s, ~%s", OtherUtils.getMinuteSeconds(estimatedDuration), OtherUtils.formatFileSize(estimatedSize));
        trimDurAndSizeTxt.setText(videoTimeSize);
        String trimRangeDurStr = OtherUtils.getMinuteSeconds(trimStartTime) + "-" + OtherUtils.getMinuteSeconds(trimEndTime);
        trimDurRangeTxt.setText(trimRangeDurStr);
    }

    private void initVideo() {
        originalSize = videoFile.length();
        videoView.setOnPreparedListener(mediaPlayer -> {
            videoDuration = mediaPlayer.getDuration();
            initVideoTimelineView();
            playBtn.setVisibility(View.VISIBLE);
            updateVideoInfo();
            videoViewWrapper();
        });
        videoView.setVideoURI(videoUri);
    }

    private void initVideoTimelineView() {
        if (videoDuration >= (VIDEO_MIN_DURATION_MS + 1000)) {
            float minProgressDiff = VIDEO_MIN_DURATION_MS / videoDuration;
            timelineView.setMinProgressDiff(minProgressDiff);
        }
        if (videoDuration >= (VIDEO_MAX_DURATION_MS + 1000)) {
            float maxProgressDiff = VIDEO_MAX_DURATION_MS / videoDuration;
            timelineView.setMaxProgressDiff(maxProgressDiff);
        }
        timelineView.setMaxVideoSize(VIDEO_MAX_SIZE, originalSize);
        timelineView.setDelegate(new CustomVideoTimelinePlayView.VideoTimelineViewDelegate() {

            @Override
            public void onLeftProgressChanged(float progress) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playBtn.setVisibility(View.VISIBLE);
                }
                timelineView.setProgress(0);
                updateVideoInfo();
            }

            @Override
            public void onRightProgressChanged(float progress) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playBtn.setVisibility(View.VISIBLE);
                }
                timelineView.setProgress(0);
                updateVideoInfo();
            }

            @Override
            public void onPlayProgressChanged(float progress) {
                videoView.seekTo((int) (videoDuration * progress));
            }

            @Override
            public void didStartDragging() {
            }

            @Override
            public void didStopDragging() {
                videoView.seekTo((int) (videoDuration * timelineView.getLeftProgress()));
            }
        });
        timelineView.setVideoPath(videoUri);
    }
}
