package com.anibear.andvideoeditingtool.common;


public abstract class AtomicShareable<T> extends AtomicRefCounted {
    protected final Recycler<T> _Recycler;

    public AtomicShareable(Recycler<T> recycler) {
        this._Recycler = recycler;
    }

    protected abstract void onLastRef();
}
