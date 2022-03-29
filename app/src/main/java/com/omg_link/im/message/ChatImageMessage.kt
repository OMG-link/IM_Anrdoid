package com.omg_link.im.message

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.BitmapUtils
import com.omg_link.im.tools.ViewUtils
import im.protocol.fileTransfer.ClientFileReceiveTask
import im.protocol.fileTransfer.IDownloadCallback
import java.io.File

class ChatImageMessage(username: String, stamp: Long) : ChatMessage(username, stamp),
    ISelfUpdatable<ChatImageMessageHolder> {

    override val type = Type.IMAGE

    // State

    enum class State {
        Downloading, DownloadFailed, Downloaded
    }

    var state: State = State.Downloading

    lateinit var imagePath: String
    lateinit var failReason: String

    // Downloading

    fun getDownloadCallback(): IDownloadCallback {
        return object : IDownloadCallback {
            override fun onSucceed(task: ClientFileReceiveTask) {
                state = State.Downloaded
                imagePath =
                    MainActivity.getActiveClient()!!.fileManager.openFile(task.receiverFileId).file.absolutePath
                val messageManager = this@ChatImageMessage.messageManager
                    ?: return
                messageManager.roomActivity.runOnUiThread {
                    updateData()
                }
            }

            override fun onFailed(task: ClientFileReceiveTask, reason: String) {
                state = State.DownloadFailed
                failReason = reason
                val messageManager = this@ChatImageMessage.messageManager
                    ?: return
                messageManager.roomActivity.runOnUiThread {
                    updateData()
                }
            }

        }
    }

    // Holder

    var chatImageMessageHolder: ChatImageMessageHolder? = null

    override fun removeHolder() {
        chatImageMessageHolder = null
    }

    override fun setHolder(holder: ChatImageMessageHolder) {
        chatImageMessageHolder = holder
        updateData()
    }

    private fun updateData() {
        chatImageMessageHolder?.let { updateData(it) }
    }

    private fun updateData(holder: ChatImageMessageHolder) {
        val tvErrorInfo = holder.tvErrorInfo
        val ivImage = holder.ivImage
        when (state) {
            State.Downloading -> {
                tvErrorInfo.setTextColor(holder.getAttrColor(R.attr.colorRoomChatText))
                tvErrorInfo.text = String.format(
                    holder.getString(R.string.frame_room_image_downloading)
                )
                tvErrorInfo.visibility = View.VISIBLE
                ivImage.visibility = View.GONE
            }
            State.DownloadFailed -> {
                tvErrorInfo.setTextColor(holder.getColor(R.color.red))
                tvErrorInfo.text = String.format(
                    holder.getString(R.string.frame_room_image_download_failed),
                    failReason
                )
                tvErrorInfo.visibility = View.VISIBLE
                ivImage.visibility = View.GONE
            }
            State.Downloaded -> {
                val bitmap = BitmapUtils.getBitmap(imagePath)
                if (bitmap == null) {
                    tvErrorInfo.setTextColor(holder.getColor(R.color.red))
                    tvErrorInfo.text = holder.getString(R.string.frame_room_cannot_resolve_image)
                    tvErrorInfo.visibility = View.VISIBLE
                    ivImage.visibility = View.GONE
                } else {
                    ivImage.maxHeight = if(bitmap.height<150){
                        bitmap.height * 2
                    }else{
                        300
                    }
                    ivImage.setImageBitmap(bitmap)
                    ivImage.adjustViewBounds = true
                    ivImage.scaleType = ImageView.ScaleType.FIT_START
                    ivImage.setOnLongClickListener {
                        AndroidUtils.openFile(File(imagePath), holder.itemView.context, "image/*")
                        return@setOnLongClickListener true
                    }
                    tvErrorInfo.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                }
            }
        }
    }

}

class ChatImageMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context, parent: ViewGroup) : this(createView(context, parent))

    val tvErrorInfo: TextView = itemView.findViewById(R.id.tvErrorInfo)
    val ivImage: ImageView = itemView.findViewById(R.id.ivImage)

    private lateinit var chatImageMessage: ChatImageMessage

    fun bind(chatImageMessage: ChatImageMessage) {
        super.bind(chatImageMessage as ChatMessage)
        if (this::chatImageMessage.isInitialized) {
            this.chatImageMessage.removeHolder()
        }
        this.chatImageMessage = chatImageMessage
        this.chatImageMessage.setHolder(this)
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, R.layout.message_chat_image)
            return ChatMessageHolder.createView(context, parent, listOf(view))
        }
    }

}