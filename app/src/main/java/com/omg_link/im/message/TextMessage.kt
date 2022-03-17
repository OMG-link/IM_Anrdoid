package com.omg_link.im.message

import android.widget.TextView
import com.omg_link.im.R

class TextMessage(username:String,time:Long,val text:String):Message(username,time){
    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_text)
        val textView = view.findViewById<TextView>(R.id.chatMessageArea)
        textView.text = text
        holder.addView(view)
    }

}
