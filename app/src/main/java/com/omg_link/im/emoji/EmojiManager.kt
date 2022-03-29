package com.omg_link.im.emoji

import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.RoomActivity
import im.file_manager.FileObject

class EmojiManager(
    private val roomActivity: RoomActivity,
    private val emojiRecyclerView: RecyclerView,
) {

    val emojiFolder = "Emoji"

    private val emojiList = ArrayList<FileObject>()

    private val adapter: EmojiAdapter = EmojiAdapter(emojiList)

    init {
        emojiRecyclerView.layoutManager = GridLayoutManager(roomActivity,4)
        emojiRecyclerView.adapter = adapter
        reloadFromFolder()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reloadFromFolder(){
        val fileManager = roomActivity.client.fileManager
        val folder = fileManager.openFolder(emojiFolder)
        emojiList.clear()
        for(file in folder.listFiles()!!){
            emojiList.add(fileManager.openFile(file))
        }
        adapter.notifyDataSetChanged()
    }

}