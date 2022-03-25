package com.omg_link.im

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.message.*
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.UriUtils
import im.Client
import im.config.Config
import im.gui.IFileTransferringPanel
import im.gui.IRoomFrame
import im.protocol.data_pack.file_transfer.FileTransferType
import im.protocol.data_pack.system.ConnectResultPack
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

    private lateinit var messageManager: MessageManager
    private lateinit var textInputArea: EditText
    private lateinit var roomChatSendButton: Button

    private lateinit var getImageActivity: ActivityResultLauncher<String>
    private lateinit var getFileActivity: ActivityResultLauncher<String>
    private lateinit var getPermissionActivity: ActivityResultLauncher<String>
    private lateinit var requestPermissionCallback: IRequestPermissionCallback

    private var isConnectionBuilt: Boolean = false
    private var isTextInputAreaCleared: Boolean = false

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
                val file = File(path)
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

        messageManager = MessageManager(
            this,
            findViewById(R.id.messageRecyclerView),
            findViewById(R.id.buttonRoomToBottom)
        )
        textInputArea = findViewById(R.id.roomChatInputArea)

        roomChatSendButton = findViewById(R.id.buttonRoomChatSend)
        roomChatSendButton.setOnClickListener {
            val tempString = textInputArea.text.toString()
            textInputArea.setText("")
            thread { //禁止在主线程上进行网络操作
                client.sendChat(tempString)
            }
        }
        textInputArea.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateChatSendButtonState()
            }
        })

        findViewById<Button>(R.id.buttonRoomImageSend).setOnClickListener {
            selectImageToSend()
        }

        findViewById<Button>(R.id.buttonRoomFileSend).setOnClickListener {
            selectFileToSend()
        }

        registerActivities()

        client.roomFrame = this

    }

    /**
     * Should be called on UI Thread
     */
    private fun updateChatSendButtonState(){
        roomChatSendButton.isEnabled = (textInputArea.text.isNotEmpty()&&isConnectionBuilt)
    }

    private fun selectFileToSend() {
        if (!AndroidUtils.hasPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object:
                IRequestPermissionCallback {
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
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object:
                IRequestPermissionCallback {
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

    override fun exitRoom(reason: String) {
        client.showInfo(reason)
        finish()
    }

    override fun onConnectionBuilt() {
        isConnectionBuilt = true
        messageManager.clearMessageArea()
        runOnUiThread {
            updateChatSendButtonState()
            findViewById<Button>(R.id.buttonRoomImageSend).isEnabled = true
            findViewById<Button>(R.id.buttonRoomFileSend).isEnabled = true
            textInputArea.isEnabled = true;
            if(!isTextInputAreaCleared){
                textInputArea.setText("")
                isTextInputAreaCleared = true
            }
        }
    }

    override fun onConnectionBroke() {
        isConnectionBuilt = false
        showSystemMessage(resources.getString(R.string.frame_room_disconnected))
        runOnUiThread {
            updateChatSendButtonState()
            findViewById<Button>(R.id.buttonRoomImageSend).isEnabled = false
            findViewById<Button>(R.id.buttonRoomFileSend).isEnabled = false
            textInputArea.isEnabled = false;
        }
    }

    override fun onConnectionRefused(reason: ConnectResultPack.RejectReason) {
        exitRoom(resources.getString(
            when(reason){
                ConnectResultPack.RejectReason.InvalidToken -> R.string.activity_login_connectrejected_invalidtoken
            }
        ))
    }

    override fun showSystemMessage(message: String) {
        messageManager.insertMessage(SystemMessage(message))
    }

    override fun showTextMessage(sender: String, stamp: Long, text: String) {
        messageManager.insertMessage(ChatTextMessage(sender, stamp, text))

    }

    override fun showChatImageMessage(
        sender: String,
        stamp: Long,
        serverFileId: UUID
    ): IDownloadCallback {
        val message = ChatImageMessage(sender,stamp)
        messageManager.insertMessage(message)
        return message.getDownloadCallback()
    }

    override fun showFileUploadedMessage(
        sender: String,
        stamp: Long,
        fileId: UUID,
        fileName: String,
        fileSize: Long
    ) {
        messageManager.insertMessage(ChatFileMessage(
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
        val message = ChatFileUploadingMessage(Config.getUsername(),System.currentTimeMillis(),this,fileNameGetter,fileSize)
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
        showSystemMessage(stringBuilder.toString())
    }

    override fun onUserJoined(user: User) {
        showSystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_userjoin),
            user.name
        ))
    }

    override fun onUserLeft(user: User) {
        showSystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_userleft),
            user.name
        ))
    }

    override fun onUsernameChanged(user: User, previousName: String) {
        showSystemMessage(String.format(
            resources.getString(R.string.frame_room_systeminfo_changename),
            previousName,
            user.name
        ))
    }

    fun getMessageManager(): MessageManager {
        return messageManager
    }

    fun getHandler(): Client {
        return client
    }

}