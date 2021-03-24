package com.anibear.andvideoeditingtool.videoTrimmer.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.anibear.andvideoeditingtool.R;

public class VideoEditView extends RelativeLayout implements VideoEditProgressView.PlayStateListener {

    private String TAG = VideoEditView.class.getSimpleName();
    public VideoEditProgressView videoEditProgressView;
    private int viewWidth;
    private int viewHeight;
    private int screenWidth;

    public VideoEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    //컨트롤 초기화
    private void initView(Context context, AttributeSet attrs) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        screenWidth = dm.widthPixels;

        videoEditProgressView = new VideoEditProgressView(context, attrs);  //ViewEditProgressView 추가
        RelativeLayout.LayoutParams videoEditParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams params = new LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT);
        videoEditProgressView.setLayoutParams(params);
        videoEditProgressView.setPlayStateListener(this);
        videoEditParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        addView(videoEditProgressView, videoEditParams);

        ImageView ivCenter = new ImageView(context);
        ivCenter.setImageResource(R.drawable.bigicon_center);
        RelativeLayout.LayoutParams ivRarams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ivRarams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(ivCenter, ivRarams);
    }


    /**
     * 레이아웃 파일이로드되면이 메서드를 다시 호출합니다.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 측정 방법에서 각 컨트롤의 높이와 너비를 가져옵니다.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = videoEditProgressView.getMeasuredWidth();
        viewHeight = getMeasuredHeight();
       /* viewWidth = discreteScrollView.getMeasuredWidth();
        viewHeight = getMeasuredHeight();*/
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //메뉴 위치 지정
        //discreteScrollView.layout(screenWidth / 2, 0, screenWidth / 2 + viewWidth, viewHeight);

        videoEditProgressView.layout(screenWidth / 2, 0, screenWidth / 2 + viewWidth, viewHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        videoEditProgressView.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void playStateChange(boolean playState) {
    }


    public interface OnSelectTimeChangeListener {
        void selectTimeChange(long startTime, long endTime);

        void playChange(boolean isPlayVideo);

        void videoProgressUpdate(long currentTime, boolean isVideoPlaying);
    }

    public OnSelectTimeChangeListener onSelectTimeChangeListener;

    //시작 시간 및 종료 시간 콜백
    @Override
    public void selectTimeChange(long startTime, long endTime) {
        if (onSelectTimeChangeListener != null) {
            onSelectTimeChangeListener.selectTimeChange(startTime, endTime);
        }
    }

    @Override
    public void videoProgressUpdate(long currentTime, boolean isVideoPlaying) {
        Log.e(TAG, "진행률 업데이트");
        if (onSelectTimeChangeListener != null) {
            onSelectTimeChangeListener.videoProgressUpdate(currentTime, isVideoPlaying);
        }
    }
}
