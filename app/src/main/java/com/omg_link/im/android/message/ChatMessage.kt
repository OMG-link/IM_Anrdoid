package com.omg_link.im.android.message

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.omg_link.im.R
import com.omg_link.im.android.RoomActivity
import com.omg_link.im.android.tools.BitmapUtils
import com.omg_link.im.android.tools.ViewUtils.createLayoutFromXML
import com.omg_link.im.core.file_manager.FileObject
import com.omg_link.im.core.gui.IFileTransferringPanel
import com.omg_link.im.core.protocol.file_transfer.FileTransferType
import java.util.*

abstract class ChatMessage(
    val roomActivity: RoomActivity, val username: String, avatarFileId: UUID, stamp: Long
) : Message(stamp), ISelfUpdatable<ChatMessageHolder> {
    final override val isUserMessage = true

    private var avatar: Drawable? =
        ResourcesCompat.getDrawable(roomActivity.resources, R.drawable.avatar, roomActivity.theme)

    init {
        if (!Objects.equals(avatarFileId, UUID(0, 0))) { // Not default avatar
            roomActivity.room.downloadFile(
                avatarFileId.toString(),
                avatarFileId,
                FileTransferType.Avatar,
                object : IFileTransferringPanel {
                    override fun onTransferSucceed(senderFileId: UUID, receiverFileId: UUID) {
                        val bitmap = BitmapUtils.getBitmap(
                            roomActivity.room.fileManager.openFile(receiverFileId).file.absolutePath
                        )
                        avatar = BitmapDrawable(roomActivity.resources, bitmap)
                        roomActivity.runOnUiThread {
                            updateData()
                        }
                    }
                }
            )
        }
    }

    private var chatMessageHolder: ChatMessageHolder? = null

    override fun removeHolder(holder: ChatMessageHolder) {
        chatMessageHolder = null
    }

    override fun setHolder(holder: ChatMessageHolder) {
        chatMessageHolder = holder
        updateData()
    }

    private fun updateData() {
        chatMessageHolder?.let {
            updateData(it)
        }
    }

    private fun updateData(holder: ChatMessageHolder) {
        holder.ivAvatar.setImageDrawable(avatar)
    }


}

abstract class ChatMessageHolder protected constructor(itemView: View) : MessageHolder(itemView) {

    val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
    private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

    private lateinit var chatMessage: ChatMessage

    protected fun bind(chatMessage: ChatMessage) {
        super.bind(chatMessage as Message)
        tvUsername.text = chatMessage.username

        if (this::chatMessage.isInitialized) {
            chatMessage.removeHolder(this)
        }
        this.chatMessage = chatMessage
        this.chatMessage.setHolder(this)

    }

    companion object {
        fun createView(
            context: Context,
            parent: ViewGroup,
            isSelfSent: Boolean,
            children: List<View>? = null
        ): View {
            val view = createLayoutFromXML(
                context, parent, if (isSelfSent) {
                    R.layout.message_chat_right
                } else {
                    R.layout.message_chat_left
                }
            )
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