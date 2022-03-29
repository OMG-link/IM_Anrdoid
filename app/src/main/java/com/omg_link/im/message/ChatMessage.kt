package com.omg_link.im.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.tools.ViewUtils.createLayoutFromXML

abstract class ChatMessage(val username: String, stamp: Long) : Message(stamp) {
    final override val isUserMessage = true
}

abstract class ChatMessageHolder protected constructor(itemView: View) : MessageHolder(itemView) {
    private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

    protected fun bind(chatMessage: ChatMessage) {
        super.bind(chatMessage as Message)
        tvUsername.text = chatMessage.username
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup, children: List<View>? = null): View {
            val view = createLayoutFromXML(context, parent, R.layout.message_chat)
            if (children != null) {
                val layoutChildren: LinearLayout = view.findViewById(R.id.layoutMessageChatChildren)
                for (child in children) {
                    layoutChildren.addView(child)
                }
            }
            return MessageHolder.createView(context, parent, listOf(view))
        }
    }

}