package com.omg_link.im.android_gui

import GUI.IFileTransferringPanel
import GUI.IRoomFrame
import IM.Client
import IM.Config
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.android_gui.tools.Message
import com.omg_link.im.android_gui.tools.MessagePanelAdapter
import protocol.dataPack.ImageType
import protocol.helper.fileTransfer.ClientFileReceiveTask
import protocol.helper.fileTransfer.IDownloadCallback
import java.util.*
import kotlin.concurrent.thread


class RoomActivity : AppCompatActivity(),IRoomFrame {

    private val handler: Client
    private val messageList : MutableList<Message>
    private val adapter : MessagePanelAdapter
    private lateinit var messageRecyclerView : RecyclerView
    private lateinit var textInputArea : EditText

    init{
        val activeClient = MainActivity.getActiveClient()
        if(activeClient!=null){
            handler = activeClient
        }else{
            throw RuntimeException("Failed to initialize connect activity: main client not found!")
        }

        messageList = mutableListOf()
        adapter = MessagePanelAdapter(messageList)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        title = Config.getServerIP()+":"+Config.getServerPort()

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        textInputArea = findViewById(R.id.roomChatInputArea)

        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        val chatSendButton = findViewById<Button>(R.id.roomChatSendButton)
        chatSendButton.setOnClickListener {
            val tempString = textInputArea.text.toString()
            textInputArea.setText("")
            thread { //禁止在主线程上进行网络操作
                handler.sendChat(tempString)
            }
        }

        val activeClient = MainActivity.getActiveClient()
            ?: throw RuntimeException("Failed to initialize connect activity: main client not found!")
        activeClient.roomFrame = this

    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveContext(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.networkHandler.interrupt()
    }

    override fun setVisible(b: Boolean) {
        //do nothing
    }

    override fun clearMessageArea() {
        messageRecyclerView.post {
            messageList.clear()
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMessageReceive(sender: String, stamp: Long, text: String) {
        val message = Message(sender,stamp,text)
        messageRecyclerView.post {
            val position = messageList.size
            messageList.add(message)
            adapter.notifyItemInserted(position)
            messageRecyclerView.scrollToPosition(position)
        }
    }

    override fun onChatImageReceive(
        sender: String,
        stamp: Long,
        imageUUID: UUID,
        imageType: ImageType
    ): IDownloadCallback {
        return object : IDownloadCallback {
            override fun onSucceed(task: ClientFileReceiveTask?) {
                //TODO("Not yet implemented")
            }

            override fun onFailed(task: ClientFileReceiveTask?) {
                //TODO("Not yet implemented")
            }

        }
    }

    override fun onFileUploadedReceive(
        sender: String,
        stamp: Long,
        uuid: UUID,
        fileName: String,
        fileSize: Long
    ) {
        val text = String.format(resources.getString(R.string.activity_room_file_not_support),fileName,sizeToString(fileSize))
        onMessageReceive(sender,stamp,text)
    }

    override fun onUserListUpdate(userList: Array<out String>) {
        //not implemented
    }

    override fun addFileTransferringPanel(fileName: String): IFileTransferringPanel {
        //not implemented
        return FileTransferringPanel()
    }

    private fun sizeToString(size:Long):String{
        return if (size < 1000L) {
            String.format("%dB", size)
        } else if (size < (1L shl 10) * 1000L) {
            String.format("%.2fKB", size.toDouble() / (1L shl 10))
        } else if (size < (1L shl 20) * 1000L) {
            String.format("%.2fMB", size.toDouble() / (1L shl 20))
        } else if (size < (1L shl 30) * 1000L) {
            String.format("%.2fGB", size.toDouble() / (1L shl 30))
        } else {
            String.format("%.2fTB", size.toDouble() / (1L shl 40))
        }
    }

}