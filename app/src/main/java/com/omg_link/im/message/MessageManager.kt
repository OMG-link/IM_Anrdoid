package com.omg_link.im.message

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.RoomActivity
import java.util.ArrayList

class MessageManager(roomActivity: RoomActivity, private val messageRecyclerView: RecyclerView) {

    private val messageList = object : ArrayList<Message>() {
        private fun locateNextMessage(stamp:Long):Int{
            var l = 0
            var r = size - 1
            while (l <= r) {
                val mid = (l + r) / 2
                if (get(mid).stamp > stamp) {
                    r = mid - 1
                } else {
                    l = mid + 1
                }
            }
            return l;
        }

        fun addByStamp(message: Message): Int {
            val p = locateNextMessage(message.stamp)
            add(p, message)
            return p
        }

    }

    private val adapter: MessagePanelAdapter = MessagePanelAdapter(messageList)

    init {
        messageRecyclerView.layoutManager = LinearLayoutManager(roomActivity)
        messageRecyclerView.adapter = adapter
    }

    fun insertMessage(message: Message) {
        messageRecyclerView.post {
            val position = messageList.addByStamp(message)
            adapter.notifyItemInserted(position)
            onMessageInserted(position)
        }
    }

    fun removeMessage(message: Message){
        messageRecyclerView.post {
            val p = messageList.indexOf(message)
            if(p==-1) return@post
            messageList.removeAt(p)
            adapter.notifyItemRemoved(p)
        }
    }

    fun clearMessageArea() {
        messageRecyclerView.post {
            messageList.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun onMessageInserted(position: Int){
        if(messageRecyclerView.canScrollVertically(1)) return
        scrollToBottom()
    }

    fun scrollToBottom(){
        messageRecyclerView.scrollToPosition(messageList.size-1)
    }

}