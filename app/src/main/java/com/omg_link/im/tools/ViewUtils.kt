package com.omg_link.im.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

object ViewUtils {
    fun createLayoutFromXML(context: Context,parent: ViewGroup?, @LayoutRes resId:Int): View {
        return LayoutInflater.from(context).inflate(resId, parent, false)
    }

}