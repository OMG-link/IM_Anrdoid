package com.omg_link.im.android.message

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.android.RoomActivity
import java.security.InvalidParameterException
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

class MessageManager(
    val roomActivity: RoomActivity,
    private val messageRecyclerView: RecyclerView,
    private val toBottomButton: Button
) {

    private var isProcessingEvents = false
    private val eventQueue: Queue<Runnable> = LinkedList()

    private var maxMessageId: Long = Long.MIN_VALUE
    private var minMessageId: Long = Long.MAX_VALUE

    private var unreadMessageCount: Int = 0
        set(value) {
            if (field == value) return
            field = value
            addEvent {
                if (value == 0) {
                    toBottomButton.visibility = View.GONE
                } else {
                    toBottomButton.visibility = View.VISIBLE
                    toBottomButton.text = if (value > 99) {
                        "99+"
                    } else {
                        value.toString()
                    }
                }
            }
        }

    private val messageList = MessageList()
    private val timeDisplayManager = TimeDisplayManager(this, messageList)

    private val adapter: MessageAdapter = MessageAdapter(messageList)

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
        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                this@MessageManager.isRecyclerViewAtBottom = when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> !recyclerView.canScrollVertically(1)
                    RecyclerView.SCROLL_STATE_DRAGGING -> false
                    RecyclerView.SCROLL_STATE_SETTLING -> false
                    else -> throw InvalidParameterException()
                }
            }
        })

        toBottomButton.setOnClickListener {
            scrollToBottom()
        }

    }

    fun showMoreMessage() {
        if (minMessageId == Long.MAX_VALUE || minMessageId == 1L) {
            roomActivity.swipeRefreshLayout.isRefreshing = false
        } else {
            thread { //Network process inside
                roomActivity.room.messageManager.showMoreHistory()
            }
        }
    }

    fun insertMessage(message: Message) {
        addEvent {
            if (message.isUserMessage) {
                timeDisplayManager.onMessageInsert(message)
            }
            insertMessageRaw(message)

            // Update serial ID and refresh state
            val updateSerialId = { serialId: Long ->
                if (serialId < minMessageId) {
                    roomActivity.swipeRefreshLayout.isRefreshing = false
                    minMessageId = serialId
                }
                if (serialId > maxMessageId) {
                    if (!isRecyclerViewAtBottom) {
                        unreadMessageCount++
                    }
                    maxMessageId = serialId
                }
            }
            when (message) {
                is ChatTextMessage -> {
                    updateSerialId(message.serialId)
                }
                is ChatImageMessage -> {
                    updateSerialId(message.serialId)
                }
                is ChatFileMessage -> {
                    updateSerialId(message.serialId)
                }
            }
        }
    }

    /**
     * Should be called on UI thread.
     */
    fun insertMessageRaw(message: Message) {
        message.messageManager = this
        val position = messageList.addByStamp(message)
        adapter.notifyItemInserted(position)
        if (isRecyclerViewAtBottom) {
            keepBottom()
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

    fun keepBottom() {
        if (isRecyclerViewAtBottom) {
            scrollToBottom()
        }
    }

    private var isRecyclerViewAtBottom = true
        set(value) {
            field = value
            if (value) {
                unreadMessageCount = 0
            }
        }

    fun scrollToBottom() {
        addEvent {
            messageRecyclerView.scrollToPosition(messageList.size - 1)
            isRecyclerViewAtBottom = true
        }
    }

    private fun addEvent(event: Runnable) {
        eventQueue.offer(event)
        messageRecyclerView.post { runEvent() }
    }

    private fun runEvent() {
        if (isProcessingEvents) return
        isProcessingEvents = true
        while (!eventQueue.isEmpty()) {
            val event = eventQueue.poll()
                ?:throw NullPointerException()
            event.run()
        }
        isProcessingEvents = false
    }

}