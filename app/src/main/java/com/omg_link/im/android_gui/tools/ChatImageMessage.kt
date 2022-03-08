package com.omg_link.im.android_gui.tools

import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.omg_link.im.R

class ChatImageMessage(username: String, stamp: Long, val imagePath: String) : Message(username, stamp) {

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_chatimage)
        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap == null) {
            val textView = view.findViewById<TextView>(R.id.errorInfoArea)
            textView.setTextColor(holder.getColor(R.color.red))
            textView.visibility = View.VISIBLE
        } else {
            val imageView = view.findViewById<ImageView>(R.id.chatImageArea)
            imageView.maxHeight = 400
            imageView.setImageBitmap(bitmap)
            imageView.adjustViewBounds =
                (bitmap.width > imageView.maxWidth || bitmap.height > imageView.maxHeight)
            imageView.visibility = View.VISIBLE
        }
        holder.addView(view)
    }

}
