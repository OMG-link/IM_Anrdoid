package com.omg_link.im.android_gui.tools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import java.text.SimpleDateFormat
import java.util.*

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
    private val usernameArea = itemView.findViewById<TextView>(R.id.messageTvUsername)
    private val timeArea = itemView.findViewById<TextView>(R.id.messageTvTime)
    private val textArea = itemView.findViewById<TextView>(R.id.messageTvText)

    fun bind(message: Message){
        usernameArea.text = message.username
        timeArea.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).format(Date(message.time))
        textArea.text = message.text
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