package com.omg_link.im.message

import android.view.View
import java.text.SimpleDateFormat
import java.util.*

abstract class Message(val username:String,val stamp:Long){
    var currentHolder: MessagePanelHolder? = null
    set(value) {
        field = value
        onDataUpdated()
    }

    /**
     * Indicates whether the info bar should be displayed.
     */
    open val infoBarVisibility = View.VISIBLE

    var messageVisibility = View.VISIBLE
    set(value){
        field = value
        onDataUpdated()
    }

    /**
     * Test test{@link View.VISIBLE}
     * @see View.VISIBLE
     */
    open fun removeHolder(){
        currentHolder = null
    }

    protected fun onDataUpdated(){
        currentHolder?.let { onDataUpdated(it) }
    }

    protected open fun onDataUpdated(holder: MessagePanelHolder){
        holder.setVisibility(messageVisibility)
        holder.infoBar.visibility = infoBarVisibility
        holder.usernameArea.text = username
        holder.timeArea.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.CHINA).format(Date(stamp))
        holder.componentArea.removeAllViews()
    }

}
