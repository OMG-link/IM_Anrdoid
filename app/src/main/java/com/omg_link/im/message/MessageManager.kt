package com.omg_link.im.message

import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.RoomActivity
import java.util.*
import java.util.logging.Level

class MessageManager(roomActivity: RoomActivity, private val messageRecyclerView: RecyclerView) {

    private val eventQueue: Queue<Runnable> = LinkedList()

    private val messageList = object : ArrayList<Message>() {
        private fun locateNextMessage(stamp: Long): Int {
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
            return l
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
        messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (oldBottom > bottom) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        scrollToBottom()
                    }
                }, 100) //Can't scroll immediately. I don't known why. T.T
            }
        }
    }

    fun insertMessage(message: Message) {
        addEvent {
            val position = messageList.addByStamp(message)
            adapter.notifyItemInserted(position)
            onMessageInserted()
        }
    }

    fun removeMessage(message: Message) {
        addEvent {
            val p = messageList.indexOf(message)
            if (p == -1) return@addEvent
            messageList.removeAt(p)
            adapter.notifyItemRemoved(p)
        }
    }

    fun clearMessageArea() {
        addEvent {
            val count = messageList.size
            messageList.clear()
            adapter.notifyItemRangeRemoved(0, count)
        }
    }

    private fun onMessageInserted() {
        if (messageRecyclerView.canScrollVertically(1)) return
        scrollToBottom()
    }

    fun scrollToBottom() {
        addEvent{
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun addEvent(event: Runnable) {
        eventQueue.offer(event)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runEvent()
        } else {
            messageRecyclerView.post { runEvent() }
        }
    }

    private fun runEvent() {
        while (!eventQueue.isEmpty()) {
            val event = eventQueue.poll()!!
            event.run()
        }
    }

}