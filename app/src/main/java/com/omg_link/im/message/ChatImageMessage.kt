package com.omg_link.im.message

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.RoomActivity
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.BitmapUtils
import com.omg_link.im.tools.ViewUtils
import im.protocol.fileTransfer.ClientFileReceiveTask
import im.protocol.fileTransfer.IDownloadCallback
import java.io.File

class ChatImageMessage(val roomActivity: RoomActivity,username: String, stamp: Long) : ChatMessage(username, stamp),
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
                tvErrorInfo.setTextColor(holder.getAttrColor(R.attr.colorRoomMainText))
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
                    ivImage.layoutParams.height = if (bitmap.height < 100) {
                        bitmap.height * 3
                    } else {
                        300
                    }
                    ivImage.setImageBitmap(bitmap)
                    ivImage.adjustViewBounds = true
                    ivImage.scaleType = ImageView.ScaleType.FIT_START
                    ivImage.setOnClickListener {
                        openImage(ivImage.context)
                    }
                    ivImage.setOnLongClickListener {
                        showPopupWindow(ivImage)
                        return@setOnLongClickListener true
                    }
                    tvErrorInfo.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openImage(context: Context){
        AndroidUtils.openFile(File(imagePath), context, "image/*")
    }

    private fun showPopupWindow(anchor: View) {
        //Create content
        val content =
            ViewUtils.createLayoutFromXML(
                anchor.context,
                null,
                R.layout.popup_chat_image
            )
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        //Create popup window
        val context = anchor.context
        val popupWindow = PopupWindow(context)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.bgr_popup,
                context.theme
            )
        )
        popupWindow.contentView = content

        //Configure buttons
        content.findViewById<TextView>(R.id.tvPopupChatImageBtnOpen).setOnClickListener {
            openImage(context)
            popupWindow.dismiss()
        }
        content.findViewById<TextView>(R.id.tvPopupChatImageBtnSave).setOnClickListener {
            roomActivity.emojiManager.addEmoji(roomActivity.client.fileManager.openFile(File(imagePath)))
            roomActivity.client.showInfo(roomActivity.resources.getString(R.string.frame_room_emoji_added))
            popupWindow.dismiss()
        }

        //Show it
        val margin = 20
        val pos = IntArray(2)
        anchor.getLocationOnScreen(pos)
        var showPosY = pos[1] - content.measuredHeight - margin
        if (showPosY < 0) {
            showPosY = pos[1] + anchor.measuredHeight + margin
        }
        popupWindow.showAtLocation(anchor, Gravity.START or Gravity.TOP, pos[0], showPosY)

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