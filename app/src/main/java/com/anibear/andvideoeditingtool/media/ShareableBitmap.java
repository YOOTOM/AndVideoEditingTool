package com.anibear.andvideoeditingtool.media;


import android.graphics.Bitmap;

import com.anibear.andvideoeditingtool.common.Recycler;

public class ShareableBitmap extends AtomicShareable<ShareableBitmap>
{

    private final Bitmap data;

    public ShareableBitmap(Recycler<ShareableBitmap> recycler, int w, int h)
    {
        super(recycler);
        data = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    public ShareableBitmap(Bitmap bitmap)
    {
        super(null);

        data = bitmap;
    }

    @Override
    protected void onLastRef()
    {
        if (_Recycler != null)
        {
            _Recycler.recycle(this);
        }
        else
        {
            data.recycle();
        }
    }

    public Bitmap getData()
    {
        return data;
    }

}