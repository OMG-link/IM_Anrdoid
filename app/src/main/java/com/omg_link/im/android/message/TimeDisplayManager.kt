package com.omg_link.im.android.message

class TimeDisplayManager(
    private val messageManager: MessageManager,
    private val messageList: MessageList
) {

    private var lastShowTime = 0L
    private var lastShowCount = 0

    private fun shouldShowTimeMessage(stamp: Long): Boolean {
        if (stamp - lastShowTime > 10 * 60 * 1000) { // 10min
            return true
        }
        if (lastShowCount >= 20) {
            return true
        }
        return false
    }

    fun onMessageInsert(message: Message) {
        if (messageList.isNotEmpty() && message.stamp < messageList[messageList.size - 1].stamp) {
            //Messages displayed before the last message
            val pos = messageList.locateNextMessage(message.stamp)
            val shouldDisplayTime = if(pos==0){
                true
            }else{
                val lastTime = messageList[pos-1].stamp
                message.stamp-lastTime>5*60*1000
            }
            if(shouldDisplayTime){
                messageManager.insertMessageRaw(TimeMessage(message.stamp))
            }
        } else {
            //Messages added to the end
            if (shouldShowTimeMessage(message.stamp)) {
                lastShowTime = message.stamp
                lastShowCount = 1
                messageManager.insertMessageRaw(TimeMessage(message.stamp))
            } else {
                lastShowCount++
            }
        }
    }

}