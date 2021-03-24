package com.anibear.andvideoeditingtool.interfaces

import android.content.Context

interface FilterListener {
    fun selectedFilter(filter: String)
    fun selectedFilter(filter: String, position: Int, context: Context)
}