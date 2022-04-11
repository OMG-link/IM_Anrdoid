package com.omg_link.im.android.emoji

import android.annotation.SuppressLint
import android.os.FileUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.android.RoomActivity
import com.omg_link.im.core.file_manager.FileObject
import java.io.FileInputStream
import java.io.FileOutputStream

class EmojiManager(
    private val roomActivity: RoomActivity,
    private val emojiRecyclerView: RecyclerView,
) {

    private val emojiFolder = "Emoji"

    private val emojiList = ArrayList<FileObject>()

    private val adapter: EmojiAdapter = EmojiAdapter(emojiList,roomActivity.room)

    init {
        emojiRecyclerView.layoutManager = GridLayoutManager(roomActivity,4)
        emojiRecyclerView.adapter = adapter
        reloadFromFolder()
    }

    fun getEmojiNum():Int{
        return emojiList.size
    }

    fun addEmoji(fileObject: FileObject){
        val inputStream = FileInputStream(fileObject.file)
        val outputStream = FileOutputStream(roomActivity.room.fileManager.createUnnamedFileInFolder(emojiFolder).file)
        FileUtils.copy(inputStream,outputStream)
        inputStream.close()
        outputStream.close()
        reloadFromFolder()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reloadFromFolder(){
        val fileManager = roomActivity.room.fileManager
        val folder = fileManager.openFolder(emojiFolder)
        emojiList.clear()
        for(file in folder.listFiles()!!){
            emojiList.add(fileManager.openFile(file))
        }
        adapter.notifyDataSetChanged()
    }

}