package com.omg_link.im.android

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.android.tools.InputMethodUtils

class InputManager(val roomActivity: RoomActivity) {

    val emojiArea:RecyclerView = roomActivity.findViewById(R.id.rvEmojiArea)

    enum class State{
        None,Text,Emoji,Image,File
    }

    var state: State = State.None
    set(value) {
        when(field){
            State.Text -> {
                if(field==value) return
                InputMethodUtils.hideInputMethod(roomActivity,roomActivity.textInputArea)
            }
            State.Emoji -> {
                if(field==value) return
                emojiArea.visibility = View.GONE
            }
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