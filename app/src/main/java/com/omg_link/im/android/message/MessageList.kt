package com.omg_link.im.android.message

class MessageList: ArrayList<Message>() {
    fun locateNextMessage(stamp: Long): Int {
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