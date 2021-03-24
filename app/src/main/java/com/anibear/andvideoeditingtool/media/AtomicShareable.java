package com.anibear.andvideoeditingtool.media;


import com.anibear.andvideoeditingtool.common.AtomicRefCounted;
import com.anibear.andvideoeditingtool.common.Recycler;

public abstract class AtomicShareable<T> extends AtomicRefCounted
{
    protected final Recycler<T> _Recycler;

    public AtomicShareable(Recycler<T> recycler)
    {
        this._Recycler = recycler;
    }

    protected abstract void onLastRef();
}