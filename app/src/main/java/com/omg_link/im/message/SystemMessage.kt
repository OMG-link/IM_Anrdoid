package com.omg_link.im.message

import android.view.View
import android.widget.TextView
import com.omg_link.im.R

class SystemMessage(val systemInfo:String): Message("System",System.currentTimeMillis()) {

    override val infoBarVisibility = View.GONE

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)

        val view = holder.createLayoutFromXML(R.layout.message_systeminfo)
        view.findViewById<TextView>(R.id.systemInfo).text = systemInfo
        holder.addView(view)

    }

}