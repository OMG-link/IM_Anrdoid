package com.omg_link.im.message

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R

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
    val root = itemView.findViewById<LinearLayout>(R.id.messageRoot)
    val infoBar = itemView.findViewById<LinearLayout>(R.id.messageInfo)
    val usernameArea = itemView.findViewById<TextView>(R.id.messageTvUsername)
    val componentArea = itemView.findViewById<LinearLayout>(R.id.messageComponents)

    private var currentMessage: Message? = null

    fun setVisibility(state:Int){
        root.visibility = state
    }

    fun getColor(@ColorRes resId: Int): Int{
        return itemView.context.resources.getColor(resId,null)
    }

    fun getAttrColor(resId: Int): Int{
        val value = TypedValue()
        itemView.context.theme.resolveAttribute(resId,value,true)
        assert(value.isColorType)
        return value.data
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