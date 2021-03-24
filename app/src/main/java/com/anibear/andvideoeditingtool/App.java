package com.anibear.andvideoeditingtool;

import android.app.Application;
import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kk.taurus.playerbase.config.PlayerLibrary;

public class App extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;

        //라이브러리 초기화, Trim 동영상 플레이어
        PlayerLibrary.init(this);

        //Load FFMpeg library
        try {
            FFmpeg.getInstance(this).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return sContext;
    }
}
