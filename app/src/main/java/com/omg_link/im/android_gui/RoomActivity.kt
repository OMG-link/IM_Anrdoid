package com.omg_link.im.android_gui

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.android_gui.tools.*
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.UriUtils
import im.Client
import im.config.Config
import im.gui.IFileTransferringPanel
import im.gui.IRoomFrame
import im.protocol.data_pack.file_transfer.FileTransferType
import im.protocol.fileTransfer.ClientFileReceiveTask
import im.protocol.fileTransfer.IDownloadCallback
import im.user_manager.User
import mutils.IStringGetter
import mutils.ImageType
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class RoomActivity : AppCompatActivity(), IRoomFrame {

    private interface IRequestPermissionCallback{
        fun onSuccess()
        fun onFailed()
    }

    private val client: Client

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageManager: MessageManager
    private lateinit var textInputArea: EditText

    private lateinit var getImageActivity: ActivityResultLauncher<String>
    private lateinit var getFileActivity: ActivityResultLauncher<String>
    private lateinit var getPermissionActivity: ActivityResultLauncher<String>
    private lateinit var requestPermissionCallback: IRequestPermissionCallback

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            client = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect activity: main client not found!")
        }
    }

    private fun requestPermission(permission: String,callback: IRequestPermissionCallback){
        requestPermissionCallback = callback
        getPermissionActivity.launch(permission)
    }

    private fun registerActivities(){
        getImageActivity =
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

        getFileActivity =
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

        getPermissionActivity =
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                if(it==true){
                    requestPermissionCallback.onSuccess()
                }else{
                    requestPermissionCallback.onFailed()
                }
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        title = Config.getServerIP() + ":" + Config.getServerPort()

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageManager = MessageManager(this,messageRecyclerView)
        textInputArea = findViewById(R.id.roomChatInputArea)

        findViewById<Button>(R.id.roomChatSendButton).setOnClickListener {
            val tempString = textInputArea.text.toString()
            textInputArea.setText("")
            thread { //禁止在主线程上进行网络操作
                client.sendChat(tempString)
            }
        }

        findViewById<Button>(R.id.roomImageSendButton).setOnClickListener {
            selectImageToSend()
        }

        findViewById<Button>(R.id.roomFileSendbutton).setOnClickListener {
            selectFileToSend()
        }

        registerActivities()

        client.roomFrame = this

    }

    private fun selectFileToSend() {
        if (!AndroidUtils.hasPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object: IRequestPermissionCallback{
                override fun onSuccess() {
                    selectFileToSend()
                }

                override fun onFailed() {
                    client.showInfo(resources.getString(R.string.frame_room_external_storage_denied))
                }

            })
            return
        }
        getFileActivity.launch("*/*")
    }

    private fun selectImageToSend() {
        if (!AndroidUtils.hasPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object: IRequestPermissionCallback{
                override fun onSuccess() {
                    selectImageToSend()
                }

                override fun onFailed() {
                    client.showInfo(resources.getString(R.string.frame_room_external_storage_denied))
                }

            })
            return
        }
        getImageActivity.launch("image/*")
    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveActivity(this)

        if(client.networkHandler?.isInterrupted == true){
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        client.networkHandler.interrupt()
        MainActivity.removeActivity()
    }

    override fun onConnectionBuilt() {
        runOnUiThread {
            messageManager.clearMessageArea()
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

    override fun onMessageReceive(sender: String, stamp: Long, text: String) {
        messageManager.insertMessage(TextMessage(sender, stamp, text))
    }

    override fun onChatImageReceive(
        sender: String,
        stamp: Long,
        serverFileId: UUID
    ): IDownloadCallback {
        return object : IDownloadCallback {
            override fun onSucceed(task: ClientFileReceiveTask) {
                messageManager.insertMessage(ChatImageMessage(
                    sender,
                    stamp,
                    client.fileManager.openFile(task.receiverFileId).file.absolutePath,
                    this@RoomActivity
                ))
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
        messageManager.insertMessage(FileUploadedMessage(
            sender,stamp,
            this,fileName,fileSize, fileId
        ))
    }

    override fun onRoomNameUpdate(roomName: String) {
        runOnUiThread {
            title = roomName.ifEmpty {
                Config.getUrl()
            }
        }
    }

    override fun addFileTransferringPanel(
        fileNameGetter: IStringGetter,
        fileSize: Long
    ): IFileTransferringPanel {
        val message = FileUploadingMessage(Config.getUsername(),System.currentTimeMillis(),this,fileNameGetter,fileSize)
        messageManager.insertMessage(message)
        return message
    }

    override fun updateUserList(userList: MutableCollection<User>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(resources.getString(R.string.frame_room_systeminfo_userlist))
        var isFirst = true
        for (user in userList){
            if(isFirst){
                isFirst = false
            }else{
                stringBuilder.append(", ")
            }
            stringBuilder.append(user.name)
        }
        messageManager.insertMessage(SystemMessage(stringBuilder.toString()))
    }

    override fun onUserJoined(user: User) {
        messageManager.insertMessage(SystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_userjoin),
            user.name
        )))
    }

    override fun onUserLeft(user: User) {
        messageManager.insertMessage(SystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_userleft),
            user.name
        )))
    }

    override fun onUsernameChanged(user: User, previousName: String) {
        messageManager.insertMessage(SystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_changename),
            previousName,
            user.name
        )))
    }

    fun getMessageManager(): MessageManager{
        return messageManager
    }

    fun getHandler(): Client {
        return client
    }

}