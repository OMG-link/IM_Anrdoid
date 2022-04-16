package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.omg_link.im.R
import com.omg_link.im.android.RoomActivity
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.im.android.tools.ViewUtils
import com.omg_link.im.core.file_manager.FileObject
import com.omg_link.im.core.gui.IFileTransferringPanel
import com.omg_link.im.core.protocol.file_transfer.FileTransferType
import com.omg_link.utils.FileUtils
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class ChatFileMessage(
    roomActivity: RoomActivity,
    username: String,
    stamp: Long,
    avatarFileId: UUID,
    override val isSelfSent: Boolean,
    val serialId: Long,
    private var fileName: String,
    private val fileSize: Long,
    private val fileId: UUID
) : ChatMessage(roomActivity, username, avatarFileId, stamp), IFileTransferringPanel/*, ISelfUpdatable<ChatFileMessageHolder>*/ {

    override val type = Type.FILE

    // Panel State

    enum class State {
        READY, DOWNLOADING, DOWNLOADED
    }

    private var panelState = State.READY
        set(value) {
            field = value
            roomActivity.runOnUiThread {
                updateData()
            }
        }

    // Info

    private var downloadInfo: String = ""
        set(value) {
            field = value
            roomActivity.runOnUiThread {
                chatFileMessageHolder?.tvDownloadInfo?.text = value
            }
        }

    // File transfer

    lateinit var file: File

    override fun setProgress(downloadedSize: Long) {
        downloadInfo = String.format(
            roomActivity.resources.getString(R.string.frame_room_file_download_progress),
            fileName,
            FileUtils.sizeToString(downloadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        downloadInfo = roomActivity.resources.getString(R.string.frame_room_file_download_start)
        panelState = State.DOWNLOADING
    }

    override fun onTransferSucceed(senderFileId: UUID, receiverFileId: UUID) {
        this.file = roomActivity.room.fileManager.openFile(receiverFileId).file
        fileName = file.name
        downloadInfo = String.format(
            roomActivity.resources.getString(R.string.frame_room_file_download_succeed),
            fileName
        )
        panelState = State.DOWNLOADED
    }

    override fun onTransferFailed(reason: String) {
        downloadInfo = String.format(
            roomActivity.resources.getString(R.string.frame_room_file_download_failed),
            reason
        )
        panelState = State.READY
    }

    //Holder

    private var chatFileMessageHolder: ChatFileMessageHolder? = null

    fun removeHolder(holder: ChatFileMessageHolder) {
        chatFileMessageHolder = null
    }

    fun setHolder(holder: ChatFileMessageHolder) {
        chatFileMessageHolder = holder
        updateData()
    }

    private fun updateData() {
        chatFileMessageHolder?.let { updateData(it) }
    }

    private fun updateData(holder: ChatFileMessageHolder) {
        val bubble = holder.layoutBubble
        val tvFileName = holder.tvFileName
        val tvFileSize = holder.tvFileSize
        tvFileName.text = fileName
        tvFileSize.text = FileUtils.sizeToString(fileSize)
        downloadInfo = downloadInfo
        when (panelState) {
            State.READY -> {
                bubble.setOnClickListener {
                    bubble.setOnClickListener(null)
                    thread {
                        roomActivity.room.downloadFile(fileName, fileId, FileTransferType.ChatFile, this)
                    }
                    onTransferStart()
                }
            }
            State.DOWNLOADING -> {

            }
            State.DOWNLOADED -> {
                bubble.setOnClickListener {
                    AndroidUtils.openFile(file, roomActivity)
                }
                downloadInfo = holder.getString(R.string.frame_room_file_download_succeed)
            }
        }
    }

}

class ChatFileMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context,parent: ViewGroup, isSelfSent: Boolean) : this(createView(context,parent,isSelfSent))

    val layoutBubble: ConstraintLayout = itemView.findViewById(R.id.layoutMessageChatFileBubble)
    val tvFileName: TextView = itemView.findViewById(R.id.tvMessageChatFileFileName)
    val tvFileSize: TextView = itemView.findViewById(R.id.tvMessageChatFileFileSize)
    val tvDownloadInfo: TextView = itemView.findViewById(R.id.tvMessageChatFileDownloadInfo)

    private lateinit var chatFileMessage: ChatFileMessage

    fun bind(chatFileMessage: ChatFileMessage) {
        super.bind(chatFileMessage as ChatMessage)
        if (this::chatFileMessage.isInitialized) {
            this.chatFileMessage.removeHolder(this)
        }
        this.chatFileMessage = chatFileMessage
        this.chatFileMessage.setHolder(this)
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup, isSelfSent: Boolean): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, if(isSelfSent){
                R.layout.message_chat_right_file
            }else{
                R.layout.message_chat_left_file
            })
            return ChatMessageHolder.createView(context, parent, isSelfSent, listOf(view))
        }
    }

}