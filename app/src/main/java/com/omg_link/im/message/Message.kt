package com.omg_link.im.message

import android.os.Looper
import android.view.View
import com.omg_link.im.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

abstract class Message(val username:String,val stamp:Long){

    enum class MessageType{
        CHAT,SYSTEM
    }

    var currentHolder: MessagePanelHolder? = null
    set(value) {
        field = value
        onDataUpdated()
    }

    var messageManager: MessageManager? = null

    open val infoBarVisibility = View.VISIBLE
    abstract val type: MessageType

    var messageVisibility = View.VISIBLE
    set(value){
        field = value
        onDataUpdated()
    }

    open fun removeHolder(){
        currentHolder = null
    }

    protected fun onDataUpdated(){
        currentHolder?.let {
            onDataUpdated(it)
        }
    }

    protected open fun onDataUpdated(holder: MessagePanelHolder){
        holder.setVisibility(messageVisibility)
        holder.infoBar.visibility = infoBarVisibility
        holder.usernameArea.text = username
        holder.componentArea.removeAllViews()
    }

}
