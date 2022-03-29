package com.omg_link.im

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.tools.InputMethodUtils

class InputManager(val roomActivity: RoomActivity) {

    val emojiArea:RecyclerView = roomActivity.findViewById(R.id.rvEmojiArea)

    enum class State{
        None,Text,Emoji,Image,File
    }

    var state: State = State.None
    set(value) {
        if(field==value) return
        when(field){
            State.Text -> InputMethodUtils.hideInputMethod(roomActivity,roomActivity.textInputArea)
            State.Emoji -> emojiArea.visibility = View.GONE
            else -> {}
        }
        field = value
        when(field){
            State.Emoji -> emojiArea.visibility = View.VISIBLE
            State.File -> roomActivity.selectFileToSend()
            State.Image -> roomActivity.selectImageToSend()
            else -> {}
        }
    }

}