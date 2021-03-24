package com.anibear.andvideoeditingtool.interfaces

interface CommunicateListener<T, T2> {
    fun Result(t: T, t2: T2)
}