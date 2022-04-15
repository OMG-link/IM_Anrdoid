package com.omg_link.im.android.message

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.security.InvalidParameterException

class MessageAdapter(private val data: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return (data[position].type shl 1) or (if(data[position].isSelfSent)1 else 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return when (viewType shr 1) {
            Message.Type.TEXT -> ChatTextMessageHolder(parent.context,parent,(viewType and 1)==1)
            Message.Type.IMAGE -> ChatImageMessageHolder(parent.context,parent,(viewType and 1)==1)
            Message.Type.FILE -> ChatFileMessageHolder(parent.context,parent,(viewType and 1)==1)
            Message.Type.UPLOADING -> ChatFileUploadingMessageHolder(parent.context,parent)
            Message.Type.SYSTEM -> SystemMessageHolder(parent.context,parent)
            Message.Type.TIME -> TimeMessageHolder(parent.context,parent)
            else -> throw InvalidParameterException()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is ChatTextMessageHolder -> holder.bind(item as ChatTextMessage)
            is ChatImageMessageHolder -> holder.bind(item as ChatImageMessage)
            is ChatFileMessageHolder -> holder.bind(item as ChatFileMessage)
            is ChatFileUploadingMessageHolder -> holder.bind(item as ChatFileUploadingMessage)
            is SystemMessageHolder -> holder.bind(item as SystemMessage)
            is TimeMessageHolder -> holder.bind(item as TimeMessage)
        }
    }

}

