package com.omg_link.im.emoji

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import im.file_manager.FileObject

class EmojiAdapter(private val data: List<FileObject>) :
    RecyclerView.Adapter<EmojiHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiHolder {
        return EmojiHolder(parent.context,parent)
    }

    override fun onBindViewHolder(holder: EmojiHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

}