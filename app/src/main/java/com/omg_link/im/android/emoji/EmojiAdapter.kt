package com.omg_link.im.android.emoji

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.core.ClientRoom
import com.omg_link.im.core.file_manager.FileObject

class EmojiAdapter(private val data: List<FileObject>,private val room: ClientRoom) :
    RecyclerView.Adapter<EmojiHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiHolder {
        return EmojiHolder(parent.context,parent,room)
    }

    override fun onBindViewHolder(holder: EmojiHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

}