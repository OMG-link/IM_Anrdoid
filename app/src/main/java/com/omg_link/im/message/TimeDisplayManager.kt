package com.omg_link.im.message

class TimeDisplayManager(
    private val messageManager: MessageManager,
    private val messageList: ArrayList<Message>
    ) {

    private var lastShowTime = Long.MIN_VALUE
    private var lastShowCount = 0

    private fun shouldShowTimeMessage(stamp: Long):Boolean{
        if(stamp-lastShowTime>10*60*1000){ //10min
            return true
        }
        if(lastShowCount>=20){
            return true
        }
        return false
    }

    fun onMessageInsert(message: Message){
        if(messageList.isNotEmpty()&&message.stamp<messageList[messageList.size-1].stamp){
            //Messages displayed before the last message
            messageManager.insertMessageRaw(TimeMessage(message.stamp))
        }else{
            //Messages added to the end
            if(shouldShowTimeMessage(message.stamp)){
                lastShowTime = message.stamp
                lastShowCount = 1
                messageManager.insertMessageRaw(TimeMessage(message.stamp))
            }else{
                lastShowCount++
            }
        }
    }

}