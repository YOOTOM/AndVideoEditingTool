/*
 *
 *  Created by sol on Aug 2019.
 *  Copyright © 2019 sol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.anibear.andvideoeditingtool.videoTrimmer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.anibear.andvideoeditingtool.R;
import com.anibear.andvideoeditingtool.videoTrimmer.utils.BackgroundExecutor;
import com.anibear.andvideoeditingtool.videoTrimmer.utils.UiThreadExecutor;

public class TimeLineView extends View {

    private Uri mVideoUri;
    private int mHeightView;
    private LongSparseArray<Bitmap> mBitmapList = null;
    private LongSparseArray<Bitmap> mThumbnailList = null;

    public TimeLineView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

        final int minH = getPaddingBottom() + getPaddingTop() + mHeightView;
        int h = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(final int w, int h, final int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        if (w != oldW) {
            getBitmap(w);
        }
    }

    //!!!!썸네일 생성!!!!
    private void getBitmap(final int viewWidth) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
                                       @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                       @Override
                                       public void execute() {
                                           try {
                                               mThumbnailList = new LongSparseArray<>();

                                               MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                               mediaMetadataRetriever.setDataSource(getContext(), mVideoUri);

                                               // Retrieve media data
                                               long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                                               // Set thumbnail properties (Thumbs are squares)
                                               final int thumbWidth = mHeightView * 2;
                                               final int thumbHeight = mHeightView * 2;

                                               int numThumbs = (int) Math.ceil(((float) viewWidth) / thumbWidth);

                                               final long interval = videoLengthInMs / numThumbs;

                                               for (int i = 0; i < numThumbs; ++i) {
                                                   Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                                                   // TODO: bitmap might be null here, hence throwing NullPointerException. You were right
                                                   try {
                                                       bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth - 10, thumbHeight, false);
                                                   } catch (Exception e) {
                                                       e.printStackTrace();
                                                   }
                                                   mThumbnailList.put(i, bitmap);
                                               }

                                               mediaMetadataRetriever.release();
                                               returnBitmaps(mThumbnailList);
                                           } catch (final Throwable e) {
                                               Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                           }
                                       }
                                   }
        );
    }

    private void returnBitmaps(final LongSparseArray<Bitmap> thumbnailList) {
        UiThreadExecutor.runTask("", () -> {
            mBitmapList = thumbnailList;
            invalidate();
        }
                , 0L);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapList != null) {
            canvas.save();
            int x = 0;

            for (int i = 0; i < mBitmapList.size(); i++) {
                Bitmap bitmap = mBitmapList.get(i);

                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x, 0, null);
                    x = x + bitmap.getWidth();
                }
            }
        }
    }

    public void setVideo(@NonNull Uri data) {
        this.mVideoUri = data;
    }
}
