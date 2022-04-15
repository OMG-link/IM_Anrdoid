package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.android.tools.ViewUtils.createLayoutFromXML

class ChatTextMessage(
    username: String, time: Long, override val isSelfSent: Boolean, val serialId: Long,
    val text: String
) :
    ChatMessage(username, time) {
    override val type = Type.TEXT
}

class ChatTextMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context, parent: ViewGroup, isSelfSent: Boolean) : this(
        createView(
            context,
            parent,
            isSelfSent
        )
    )

    private val tvChatText: TextView = itemView.findViewById(R.id.tvChatText)

    fun bind(chatTextMessage: ChatTextMessage) {
        super.bind(chatTextMessage as ChatMessage)
        tvChatText.text = chatTextMessage.text
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup, isSelfSent: Boolean): View {
            val view: View = createLayoutFromXML(
                context, parent, if (isSelfSent) {
                    R.layout.message_chat_right_text
                } else {
                    R.layout.message_chat_left_text
                }
            )
            return ChatMessageHolder.createView(context, parent, isSelfSent, listOf(view))
        }
    }

}
