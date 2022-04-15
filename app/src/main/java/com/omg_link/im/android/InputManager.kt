package com.omg_link.im.android

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.android.tools.InputMethodUtils

class InputManager(val roomActivity: RoomActivity) {

    val emojiArea:RecyclerView = roomActivity.findViewById(R.id.rvEmojiArea)
    val emojiButton: ImageView = roomActivity.findViewById(R.id.buttonRoomEmojiSend)

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
                emojiButton.isSelected = false
            }
            else -> {}
        }
        field = value
        when(field){
            State.Emoji -> {
                if(roomActivity.emojiManager.getEmojiNum()==0){
                    roomActivity.room.showMessage(
                        roomActivity.resources.getString(R.string.frame_room_no_emoji_found)
                    )
                }else{
                    emojiArea.visibility = View.VISIBLE
                    emojiButton.isSelected = true
                }
            }
            State.File -> roomActivity.selectFileToSend()
            State.Image -> roomActivity.selectImageToSend()
            else -> {}
        }
    }

}