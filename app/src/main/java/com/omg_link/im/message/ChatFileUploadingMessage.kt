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

    private var mainInfo: String = ""
        set(value) {
            field = value
            activity.runOnUiThread {
                val tv = chatFileUploadingMessageHolder?.mainInfo
                    ?: return@runOnUiThread
                if(value.isEmpty()){
                    tv.visibility = View.GONE
                }else{
                    tv.visibility = View.VISIBLE
                    tv.text = value
                }
            }
        }
    private var subInfo: String = ""
        set(value) {
            field = value
            activity.runOnUiThread {
                val tv = chatFileUploadingMessageHolder?.subInfo
                    ?: return@runOnUiThread
                if(value.isEmpty()){
                    tv.visibility = View.GONE
                }else{
                    tv.visibility = View.VISIBLE
                    tv.text = value
                }
            }
        }


    // File transfer

    override fun setProgress(uploadedSize: Long) {
        mainInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_progress),
            fileNameGetter.string
        )
        subInfo = String.format(
            "%s/%s",
            FileUtils.sizeToString(uploadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        mainInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_start),
            fileNameGetter.string
        )
        subInfo = ""
    }

    override fun onTransferSucceed(file: File) {
        mainInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_succeed),
            fileNameGetter.string
        )
        subInfo = ""
        activity.getMessageManager().removeMessage(this)
    }

    override fun onTransferFailed(reason: String) {
        mainInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_failed),
            fileNameGetter.string
        )
        subInfo = reason
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
        mainInfo = mainInfo
        subInfo = subInfo
    }

}

class ChatFileUploadingMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context,parent: ViewGroup) : this(createView(context,parent))

    val mainInfo: TextView = itemView.findViewById(R.id.tvMessageChatFileUploadingMainInfo)
    val subInfo: TextView = itemView.findViewById(R.id.tvMessageChatFileUploadingSubInfo)

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