package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.android.tools.ViewUtils

class SystemMessage(val systemInfo: String) : Message(System.currentTimeMillis()) {
    override val isUserMessage = false
    override val type = Type.SYSTEM
}

class SystemMessageHolder(itemView: View) : MessageHolder(itemView) {

    constructor(context: Context, parent: ViewGroup) : this(createView(context, parent))

    private val tvInfo: TextView = itemView.findViewById(R.id.tvMessageSystemInfo)

    fun bind(systemMessage: SystemMessage) {
        super.bind(systemMessage as Message)
        tvInfo.text = systemMessage.systemInfo
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, R.layout.message_system_info)
            return createView(context, parent, listOf(view))
        }
    }


}