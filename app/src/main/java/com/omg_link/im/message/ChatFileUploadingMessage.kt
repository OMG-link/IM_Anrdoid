package com.omg_link.im.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.RoomActivity
import com.omg_link.im.tools.ViewUtils
import im.gui.IFileTransferringPanel
import mutils.FileUtils
import mutils.IStringGetter
import java.io.File

class ChatFileUploadingMessage(
    username: String,
    stamp: Long,
    private val activity: RoomActivity,
    private val fileNameGetter: IStringGetter,
    private val fileSize: Long
) : ChatMessage(username, stamp), IFileTransferringPanel,
    ISelfUpdatable<ChatFileUploadingMessageHolder> {

    override val type = Type.UPLOADING

    // Info

    private var info: String = ""
        set(value) {
            field = value
            activity.runOnUiThread {
                chatFileUploadingMessageHolder?.tvUploadInfo?.text = value
            }
        }

    // File transfer

    override fun setProgress(uploadedSize: Long) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_progress),
            fileNameGetter.string,
            FileUtils.sizeToString(uploadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_start),
            fileNameGetter.string
        )
    }

    override fun onTransferSucceed(file: File) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_succeed),
            fileNameGetter.string
        )
        activity.runOnUiThread {
            activity.getMessageManager().removeMessage(this)
        }
    }

    override fun onTransferFailed(reason: String) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_failed),
            fileNameGetter.string,
            reason
        )
    }

    //Holder

    private var chatFileUploadingMessageHolder: ChatFileUploadingMessageHolder? = null

    override fun removeHolder() {
        chatFileUploadingMessageHolder = null
    }

    override fun setHolder(holder: ChatFileUploadingMessageHolder) {
        chatFileUploadingMessageHolder = holder
        updateData()
    }

    private fun updateData() {
        chatFileUploadingMessageHolder?.let { updateData(it) }
    }

    private fun updateData(holder: MessageHolder) {
        info = info
    }

}

class ChatFileUploadingMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context,parent: ViewGroup) : this(createView(context,parent))

    val tvUploadInfo: TextView = itemView.findViewById(R.id.tvMessageChatFileUploadingInfo)

    private lateinit var chatFileUploadingMessage: ChatFileUploadingMessage

    fun bind(chatFileUploadingMessage: ChatFileUploadingMessage) {
        super.bind(chatFileUploadingMessage as ChatMessage)
        if (this::chatFileUploadingMessage.isInitialized) {
            this.chatFileUploadingMessage.removeHolder()
        }
        this.chatFileUploadingMessage = chatFileUploadingMessage
        this.chatFileUploadingMessage.setHolder(this)
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, R.layout.message_chat_file_uploading)
            return ChatMessageHolder.createView(context, parent, listOf(view))
        }
    }


}