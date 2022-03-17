package com.omg_link.im.message

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.tools.AndroidUtils
import java.io.File

class ChatImageMessage(username: String, stamp: Long, val imagePath: String, val activity: Activity) : Message(username, stamp) {

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_chatimage)
        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap == null) {
            val textView = view.findViewById<TextView>(R.id.errorInfoArea)
            textView.setTextColor(holder.getColor(R.color.red))
            textView.text = holder.getString(R.string.frame_room_cannot_resolve_image)
            textView.visibility = View.VISIBLE
        } else {
            val imageView = view.findViewById<ImageView>(R.id.chatImageArea)
            imageView.maxHeight = 400
            imageView.setImageBitmap(bitmap)
            imageView.adjustViewBounds =
                (bitmap.width > imageView.maxWidth || bitmap.height > imageView.maxHeight)
            imageView.scaleType = ImageView.ScaleType.FIT_START
            imageView.setOnLongClickListener {
                AndroidUtils.openFile(File(imagePath),activity,"image/*")
                return@setOnLongClickListener true
            }
            imageView.visibility = View.VISIBLE
        }
        holder.addView(view)
    }

}
