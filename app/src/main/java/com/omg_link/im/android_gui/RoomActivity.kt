package com.omg_link.im.android_gui

import GUI.IFileTransferringPanel
import GUI.IRoomFrame
import IM.Client
import IM.Config
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.android_gui.tools.*
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.UriUtils
import mutils.IStringGetter
import protocol.dataPack.FileTransferType
import protocol.dataPack.ImageType
import protocol.helper.fileTransfer.ClientFileReceiveTask
import protocol.helper.fileTransfer.IDownloadCallback
import java.io.File
import java.util.*
import kotlin.concurrent.thread


class RoomActivity : AppCompatActivity(), IRoomFrame {

    private val handler: Client
    private val adapter: MessagePanelAdapter
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var textInputArea: EditText

    private val getImageActivity =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val file = File(
                UriUtils.getFileAbsolutePath(this@RoomActivity, uri)
                    ?: return@registerForActivityResult
            )
            if (!file.canRead()) {
                getHandler().showInfo("Unable to read file ${file.absolutePath}. Send canceled!")
                return@registerForActivityResult
            }
            thread { getHandler().sendChatImage(file, ImageType.PNG) }
        }

    private val getFileActivity =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if(uri==null) return@registerForActivityResult
            val path = UriUtils.getFileAbsolutePath(this@RoomActivity,uri)
                ?: return@registerForActivityResult
            val file = object:File(path){ //name is like 1234name
                override fun getName(): String {
                    return super.getName().substring(4)
                }
            }
            if (!file.canRead()) {
                getHandler().showInfo("Unable to read file ${file.absolutePath}. Send canceled!")
                return@registerForActivityResult
            }
            Log.d("AndroidGUI",file.absolutePath)
            thread {
                getHandler().uploadFile(
                    file,
                    FileTransferType.ChatFile,
                    addFileTransferringPanel(
                        { file.name },
                        file.length()
                    )
                )
            }
        }

    private val messageList = object : ArrayList<Message>() {
        fun addByStamp(message: Message): Int {
            var l = 0
            var r = size - 1
            while (l <= r) {
                val mid = (l + r) / 2
                if (get(mid).stamp > message.stamp) {
                    r = mid - 1;
                } else {
                    l = mid + 1;
                }
            }
            add(l, message)
            return l
        }
    }

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            handler = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect activity: main client not found!")
        }

        adapter = MessagePanelAdapter(messageList)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        title = Config.getServerIP() + ":" + Config.getServerPort()

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        textInputArea = findViewById(R.id.roomChatInputArea)

        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        findViewById<Button>(R.id.roomChatSendButton).setOnClickListener {
            val tempString = textInputArea.text.toString()
            textInputArea.setText("")
            thread { //禁止在主线程上进行网络操作
                handler.sendChat(tempString)
            }
        }

        findViewById<Button>(R.id.roomImageSendButton).setOnClickListener {
            if (!AndroidUtils.requestPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                return@setOnClickListener
            }
            getImageActivity.launch("image/*")
        }

        findViewById<Button>(R.id.roomFileSendbutton).setOnClickListener {
            if(!AndroidUtils.requestPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )){
                return@setOnClickListener
            }
            getFileActivity.launch("*/*")
        }

        handler.roomFrame = this

    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveActivity(this)

        if(handler.networkHandler?.isInterrupted == true){
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.networkHandler.interrupt()
        MainActivity.removeActivity()
    }

    override fun onConnectionBuilt() {
        runOnUiThread {
            findViewById<Button>(R.id.roomChatSendButton).isEnabled = true
            findViewById<Button>(R.id.roomImageSendButton).isEnabled = true
            findViewById<Button>(R.id.roomFileSendbutton).isEnabled = true
            textInputArea.isEnabled = true
            textInputArea.setText("")
        }
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
        val message = TextMessage(sender, stamp, text)
        messageRecyclerView.post {
            val position = messageList.addByStamp(message)
            adapter.notifyItemInserted(position)
            messageRecyclerView.scrollToPosition(position)
        }
    }

    override fun onChatImageReceive(
        sender: String,
        stamp: Long,
        serverFileId: UUID
    ): IDownloadCallback {
        return object : IDownloadCallback {
            override fun onSucceed(task: ClientFileReceiveTask) {
                val message = ChatImageMessage(
                    sender,
                    stamp,
                    handler.fileManager.getFile(task.receiverFileId).file.absolutePath,
                    this@RoomActivity
                )
                messageRecyclerView.post {
                    val position = messageList.addByStamp(message)
                    adapter.notifyItemInserted(position)
                    messageRecyclerView.scrollToPosition(position)
                }
            }

            override fun onFailed(task: ClientFileReceiveTask?, reason: String) {
                //todo: retry
                onMessageReceive(
                    sender,
                    stamp,
                    resources.getString(R.string.activity_room_image_download_failed)
                )
            }

        }
    }

    override fun onFileUploadedReceive(
        sender: String,
        stamp: Long,
        fileId: UUID,
        fileName: String,
        fileSize: Long
    ) {
        val message = FileUploadedMessage(
            sender,stamp,
            this,fileName,fileSize, fileId
        )
        messageRecyclerView.post {
            val position = messageList.addByStamp(message)
            adapter.notifyItemInserted(position)
            messageRecyclerView.scrollToPosition(position)
        }
    }

    override fun onUserListUpdate(userList: Array<out String>) {
        //not implemented
    }

    override fun onRoomNameUpdate(roomName: String) {
        if(roomName.isEmpty()) return
        runOnUiThread {
            title = "$roomName (${Config.getServerIP()}:${Config.getServerPort()})"
        }
    }

    override fun addFileTransferringPanel(
        fileNameGetter: IStringGetter,
        fileSize: Long
    ): IFileTransferringPanel {
        val message = FileUploadingMessage(Config.getUsername(),System.currentTimeMillis(),this,fileNameGetter,fileSize)
        messageRecyclerView.post {
            val position = messageList.addByStamp(message)
            adapter.notifyItemInserted(position)
            messageRecyclerView.scrollToPosition(position)
        }
        return message
    }

    fun getHandler(): Client {
        return handler
    }

}