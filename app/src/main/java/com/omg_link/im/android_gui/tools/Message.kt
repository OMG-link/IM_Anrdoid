package com.omg_link.im.android_gui.tools

import java.text.SimpleDateFormat
import java.util.*

abstract class Message(val username:String,val stamp:Long){
    var currentHolder: MessagePanelHolder? = null
    set(value) {
        field = value
        onDataUpdated()
    }

    open fun removeHolder(){
        currentHolder = null
    }

    protected fun onDataUpdated(){
        currentHolder?.let { onDataUpdated(it) }
    }

    protected open fun onDataUpdated(holder: MessagePanelHolder){
        holder.usernameArea.text = username
        holder.timeArea.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.CHINA).format(Date(stamp))
        holder.componentArea.removeAllViews()
    }

}
