package com.anibear.andvideoeditingtool.exoplayerfilter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Aspect 16 : 9 of View
 * Created by sudamasayuki on 2017/05/17.
 */
public class MovieWrapperView extends LinearLayout {

    public MovieWrapperView(@NonNull Context context) {
        super(context);
    }

    public MovieWrapperView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieWrapperView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth / 16 * 9);*/
    }
}
