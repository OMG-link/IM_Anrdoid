package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.omg_link.im.android.MainActivity
import com.omg_link.im.R
import com.omg_link.im.android.RoomActivity
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.im.android.tools.ViewUtils
import com.omg_link.im.core.file_manager.FileObject
import com.omg_link.im.core.gui.IFileTransferringPanel
import com.omg_link.im.core.protocol.data_pack.file_transfer.FileTransferType
import com.omg_link.utils.FileUtils
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class ChatFileMessage(
    username: String,
    stamp: Long,
    val serialId: Long,
    private val activity: RoomActivity,
    private var fileName: String,
    private val fileSize: Long,
    private val fileId: UUID
) : ChatMessage(username, stamp), IFileTransferringPanel, ISelfUpdatable<ChatFileMessageHolder> {

    override val type = Type.FILE

    // Panel State

    enum class State {
        READY, DOWNLOADING, DOWNLOADED
    }

    private var panelState = State.READY
        set(value) {
            field = value
            activity.runOnUiThread {
                updateData()
            }
        }

    // Info

    private var downloadInfo: String = ""
        set(value) {
            field = value
            activity.runOnUiThread {
                chatFileMessageHolder?.tvDownloadInfo?.text = value
            }
        }

    // File transfer

    lateinit var file: File

    override fun setProgress(downloadedSize: Long) {
        downloadInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_download_progress),
            fileName,
            FileUtils.sizeToString(downloadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        downloadInfo = activity.resources.getString(R.string.frame_room_file_download_start)
        panelState = State.DOWNLOADING
    }

    override fun onTransferSucceed(fileObject: FileObject) {
        this.file = fileObject.file
        fileName = file.name
        downloadInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_download_succeed),
            fileName
        )
        panelState = State.DOWNLOADED
    }

    override fun onTransferFailed(reason: String) {
        downloadInfo = String.format(
            activity.resources.getString(R.string.frame_room_file_download_failed),
            reason
        )
        panelState = State.READY
    }

    //Holder

    private var chatFileMessageHolder: ChatFileMessageHolder? = null

    override fun removeHolder() {
        chatFileMessageHolder = null
    }

    override fun setHolder(holder: ChatFileMessageHolder) {
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
                        activity.room.downloadFile(fileName, fileId, FileTransferType.ChatFile, this)
                    }
                    onTransferStart()
                }
            }
            State.DOWNLOADING -> {

            }
            State.DOWNLOADED -> {
                bubble.setOnClickListener {
                    AndroidUtils.openFile(file, activity)
                }
                downloadInfo = holder.getString(R.string.frame_room_file_download_succeed)
            }
        }
    }

}

class ChatFileMessageHolder(itemView: View) : ChatMessageHolder(itemView) {

    constructor(context: Context,parent: ViewGroup) : this(createView(context,parent))

    val layoutBubble: ConstraintLayout = itemView.findViewById(R.id.layoutMessageChatFileBubble)
    val tvFileName: TextView = itemView.findViewById(R.id.tvMessageChatFileFileName)
    val tvFileSize: TextView = itemView.findViewById(R.id.tvMessageChatFileFileSize)
    val tvDownloadInfo: TextView = itemView.findViewById(R.id.tvMessageChatFileDownloadInfo)

    private lateinit var chatFileMessage: ChatFileMessage

    fun bind(chatFileMessage: ChatFileMessage) {
        super.bind(chatFileMessage as ChatMessage)
        if (this::chatFileMessage.isInitialized) {
            this.chatFileMessage.removeHolder()
        }
        this.chatFileMessage = chatFileMessage
        this.chatFileMessage.setHolder(this)
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, R.layout.message_chat_file)
            return createView(context, parent, listOf(view))
        }
    }

}