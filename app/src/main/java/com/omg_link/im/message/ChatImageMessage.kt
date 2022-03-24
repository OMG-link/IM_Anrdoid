package com.omg_link.im.message

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.RoomActivity
import com.omg_link.im.tools.AndroidUtils
import im.protocol.fileTransfer.ClientFileReceiveTask
import im.protocol.fileTransfer.IDownloadCallback
import java.io.File

class ChatImageMessage(username: String, stamp: Long) : Message(username, stamp) {
    enum class State{
        Downloading,DownloadFailed,Downloaded
    }

    var state: State = State.Downloading

    override val type: MessageType = MessageType.CHAT

    lateinit var imagePath: String
    lateinit var failReason: String

    fun getDownloadCallback():IDownloadCallback{
        return object : IDownloadCallback{
            override fun onSucceed(task: ClientFileReceiveTask) {
                state = State.Downloaded
                imagePath = MainActivity.getActiveClient()!!.fileManager.openFile(task.receiverFileId).file.absolutePath
                val messageManager = this@ChatImageMessage.messageManager
                    ?:return
                messageManager.roomActivity.runOnUiThread {
                    onDataUpdated()
                }
            }

            override fun onFailed(task: ClientFileReceiveTask, reason: String) {
                state = State.DownloadFailed
                failReason = reason
                val messageManager = this@ChatImageMessage.messageManager
                    ?:return
                messageManager.roomActivity.runOnUiThread {
                    onDataUpdated()
                }
            }

        }
    }

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_chatimage)
        val textView = view.findViewById<TextView>(R.id.errorInfoArea)
        val imageView = view.findViewById<ImageView>(R.id.chatImageArea)
        when(state){
            State.Downloading -> {
                textView.setTextColor(holder.getAttrColor(R.attr.colorRoomChatText))
                textView.text = String.format(
                    holder.getString(R.string.frame_room_image_downloading)
                )
                textView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
            State.DownloadFailed -> {
                textView.setTextColor(holder.getColor(R.color.red))
                textView.text = String.format(
                    holder.getString(R.string.frame_room_image_download_failed),
                    failReason
                )
                textView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
            State.Downloaded -> {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap == null) {
                    textView.setTextColor(holder.getColor(R.color.red))
                    textView.text = holder.getString(R.string.frame_room_cannot_resolve_image)
                    textView.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                } else {
                    imageView.maxHeight = 400
                    imageView.setImageBitmap(bitmap)
                    imageView.adjustViewBounds =
                        (bitmap.width > imageView.maxWidth || bitmap.height > imageView.maxHeight)
                    imageView.scaleType = ImageView.ScaleType.FIT_START
                    imageView.setOnLongClickListener {
                        AndroidUtils.openFile(File(imagePath),holder.itemView.context,"image/*")
                        return@setOnLongClickListener true
                    }
                    textView.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                }
            }
        }
        holder.addView(view)
    }

}
