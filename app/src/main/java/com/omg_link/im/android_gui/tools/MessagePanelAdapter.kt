package com.omg_link.im.android_gui.tools

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import mutils.FileUtils
import protocol.dataPack.FileTransferType

class MessagePanelAdapter(private val data:List<Message>) : RecyclerView.Adapter<MessagePanelHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePanelHolder {
        return MessagePanelHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MessagePanelHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class MessagePanelHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
    val usernameArea = itemView.findViewById<TextView>(R.id.messageTvUsername)
    val timeArea = itemView.findViewById<TextView>(R.id.messageTvTime)
    val componentArea = itemView.findViewById<LinearLayout>(R.id.messageComponents)

    private var currentMessage: Message? = null

    fun hide(){
        itemView.visibility = View.GONE
    }

    fun unhide(){
        itemView.visibility = View.VISIBLE
    }

    fun getColor(@ColorRes resId: Int): Int{
        return itemView.context.resources.getColor(resId,null)
    }

    fun getString(@StringRes resId: Int): String {
        return itemView.context.resources.getString(resId)
    }

    fun addView(view:View){
        componentArea.addView(view)
    }

    fun createLayoutFromXML(@LayoutRes resId: Int): View {
        return LayoutInflater.from(itemView.context).inflate(resId, null, false)
    }

    fun bind(message: Message){
        currentMessage?.removeHolder()
        currentMessage = message
        message.currentHolder = this
    }

    companion object{
        fun from(parent: ViewGroup):MessagePanelHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val root = layoutInflater.inflate(
                R.layout.message,
                parent,false
            )
            return MessagePanelHolder(root)
        }
    }

}