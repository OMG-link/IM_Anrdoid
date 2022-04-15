package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.android.tools.ViewUtils.createLayoutFromXML
import com.omg_link.im.databinding.MessageChatTextBinding

class ChatTextMessage(username: String, time: Long,val serialId: Long, val text: String) :
    ChatMessage(username, time) {
    override val type = Type.TEXT
}

class ChatTextMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context, parent: ViewGroup) : this(createView(context, parent))

    private val binding = MessageChatTextBinding.bind(itemView.findViewById(R.id.rootMessageChatText))

    private val tvChatText: TextView = binding.tvChatText

    fun bind(chatTextMessage: ChatTextMessage) {
        super.bind(chatTextMessage as ChatMessage)
        tvChatText.text = chatTextMessage.text
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view: View = createLayoutFromXML(context, parent, R.layout.message_chat_text)
            return createView(context, parent, listOf(view))
        }
    }

}
