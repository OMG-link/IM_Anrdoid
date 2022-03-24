package com.omg_link.im.message

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.RoomActivity
import java.security.InvalidParameterException
import java.util.*
import java.util.logging.Level

class MessageManager(
    val roomActivity: RoomActivity,
    private val messageRecyclerView: RecyclerView,
    private val toBottomButton: Button) {

    private var isProcessingEvents = false
    private val eventQueue: Queue<Runnable> = LinkedList()

    private var unreadMessageCount: Int = 0
    set(value) {
        field = value
        addEvent{
            if(value==0){
                toBottomButton.visibility = View.GONE
            }else{
                toBottomButton.visibility = View.VISIBLE
                toBottomButton.text = if(value>99){
                    "99+"
                }else{
                    value.toString()
                }
            }
        }
    }

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
    private val timeDisplayManager = TimeDisplayManager(this,messageList)

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
        messageRecyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                this@MessageManager.isRecyclerViewAtBottom = when(newState){
                    RecyclerView.SCROLL_STATE_IDLE -> !recyclerView.canScrollVertically(1)
                    RecyclerView.SCROLL_STATE_DRAGGING -> false
                    RecyclerView.SCROLL_STATE_SETTLING -> false
                    else -> throw InvalidParameterException()
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(isRecyclerViewAtBottom()){
                    unreadMessageCount = 0
                }
            }
        })

        toBottomButton.setOnClickListener {
            scrollToBottom()
        }

    }

    fun insertMessage(message: Message) {
        addEvent {
            if(message.type==Message.MessageType.CHAT){
                timeDisplayManager.onMessageInsert(message)
            }
            insertMessageRaw(message)
        }
    }

    /**
     * Should be called on UI thread.
     */
    fun insertMessageRaw(message: Message){
        message.messageManager = this
        val position = messageList.addByStamp(message)
        adapter.notifyItemInserted(position)
        onMessageInserted(message)
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

    private fun onMessageInserted(message: Message) {
        if (isRecyclerViewAtBottom()){
            keepBottom()
        }else{
            if(message.type==Message.MessageType.CHAT){
                unreadMessageCount++
            }
        }
    }

    fun keepBottom(){
        if(isRecyclerViewAtBottom()){
            scrollToBottom()
        }
    }

    private var isRecyclerViewAtBottom = true
    private fun isRecyclerViewAtBottom() = isRecyclerViewAtBottom

    fun scrollToBottom() {
        addEvent{
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun addEvent(event: Runnable) {
        eventQueue.offer(event)
        messageRecyclerView.post { runEvent() }
    }

    private fun runEvent() {
        if(isProcessingEvents) return
        isProcessingEvents = true
        while (!eventQueue.isEmpty()) {
            val event = eventQueue.poll()!!
            event.run()
        }
        isProcessingEvents = false
    }

}