package com.omg_link.im.android

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.omg_link.im.R
import com.omg_link.im.android.emoji.EmojiManager
import com.omg_link.im.android.message.*
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.im.android.tools.UriUtils
import com.omg_link.im.core.ClientRoom
import com.omg_link.im.core.config.Config
import com.omg_link.im.core.gui.IFileTransferringPanel
import com.omg_link.im.core.gui.IRoomFrame
import com.omg_link.im.core.user_manager.User
import com.omg_link.utils.IStringGetter
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class RoomActivity : AppCompatActivity(), IRoomFrame {

    private interface IRequestPermissionCallback {
        fun onSuccess()
        fun onFailed()
    }

    val room: ClientRoom

    private lateinit var inputManager: InputManager
    private lateinit var messageManager: MessageManager
    lateinit var emojiManager: EmojiManager

    lateinit var textInputArea: EditText
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var roomChatSendButton: Button
    private lateinit var buttonBar: LinearLayout
    private lateinit var tvRoomTitle: TextView
    private lateinit var tvRoomPlayerNum: TextView

    private lateinit var getImageActivity: ActivityResultLauncher<String>
    private lateinit var getFileActivity: ActivityResultLauncher<String>
    private lateinit var getPermissionActivity: ActivityResultLauncher<String>
    private lateinit var requestPermissionCallback: IRequestPermissionCallback

    private var isConnectionBuilt: Boolean = false
    private var isTextInputAreaCleared: Boolean = false

    private var userNum: Int = 0
    @SuppressLint("SetTextI18n")
    set(value) {
        field = value
        runOnUiThread {
            tvRoomPlayerNum.text = "($value)"
        }
    }

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            room = activeClient.room
        } else {
            throw RuntimeException("Failed to initialize connect roomActivity: main client not found!")
        }
    }

    private fun requestPermission(permission: String, callback: IRequestPermissionCallback) {
        requestPermissionCallback = callback
        getPermissionActivity.launch(permission)
    }

    private fun registerActivities() {
        getImageActivity =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                val file = File(
                    UriUtils.getFileAbsolutePath(this@RoomActivity, uri)
                        ?: return@registerForActivityResult
                )
                if (!file.canRead()) {
                    getHandler().showMessage("Unable to read file ${file.absolutePath}. Send canceled!")
                    return@registerForActivityResult
                }
                thread { getHandler().sendChatImage(file) }
            }

        getFileActivity =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                val path = UriUtils.getFileAbsolutePath(this@RoomActivity, uri)
                    ?: return@registerForActivityResult
                val file = File(path)
                if (!file.canRead()) {
                    getHandler().showMessage("Unable to read file ${file.absolutePath}. Send canceled!")
                    return@registerForActivityResult
                }
                Log.d("AndroidGUI", file.absolutePath)
                thread {
                    getHandler().uploadFile(
                        file,
                        addFileTransferringPanel(
                            { file.name },
                            file.length()
                        )
                    )
                }
            }

        getPermissionActivity =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it == true) {
                    requestPermissionCallback.onSuccess()
                } else {
                    requestPermissionCallback.onFailed()
                }
            }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        tvRoomTitle = findViewById(R.id.tvRoomName)
        tvRoomPlayerNum = findViewById(R.id.tvRoomUserNum)

        messageManager = MessageManager(
            this,
            findViewById(R.id.rvMessageArea),
            findViewById(R.id.buttonRoomToBottom)
        )
        inputManager = InputManager(
            this
        )
        emojiManager = EmojiManager(
            this,
            findViewById(R.id.rvEmojiArea)
        )

        // toolbar
        title = Config.getServerIP() + ":" + Config.getServerPort()
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        // textInputArea
        textInputArea = findViewById(R.id.roomChatInputArea)
        textInputArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateChatSendButtonState()
            }
        })
        textInputArea.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputManager.state = InputManager.State.Text
            }
        }
        textInputArea.setOnClickListener {
            inputManager.state = InputManager.State.Text
        }

        // chatSendButton
        roomChatSendButton = findViewById(R.id.buttonRoomChatSend)
        roomChatSendButton.setOnClickListener {
            val tempString = textInputArea.text.toString()
            textInputArea.setText("")
            thread { //禁止在主线程上进行网络操作
                room.sendChat(tempString)
            }
        }

        // buttonBar
        buttonBar = findViewById(R.id.linearLayoutButtonBar)

        // imageSendButton
        findViewById<Button>(R.id.buttonRoomImageSend).setOnClickListener {
            inputManager.state = InputManager.State.Image
        }

        // fileSendButton
        findViewById<Button>(R.id.buttonRoomFileSend).setOnClickListener {
            inputManager.state = InputManager.State.File
        }

        // emojiSendButton
        findViewById<Button>(R.id.buttonRoomEmojiSend).setOnClickListener {
            inputManager.state = InputManager.State.Emoji
        }

        // messageArea
        findViewById<RecyclerView>(R.id.rvMessageArea).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                inputManager.state = InputManager.State.None
            }
            return@setOnTouchListener false
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutMessageArea)
        swipeRefreshLayout.setOnRefreshListener {
            messageManager.showMoreMessage()
        }

        registerActivities()

        room.roomFrame = this

    }

    /**
     * Should be called on UI Thread
     */
    private fun updateChatSendButtonState() {
        roomChatSendButton.isEnabled = (textInputArea.text.isNotEmpty() && isConnectionBuilt)
    }

    fun selectFileToSend() {
        if (!AndroidUtils.hasPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object :
                IRequestPermissionCallback {
                override fun onSuccess() {
                    selectFileToSend()
                }

                override fun onFailed() {
                    room.showMessage(resources.getString(R.string.frame_room_external_storage_denied))
                }

            })
            return
        }
        getFileActivity.launch("*/*")
    }

    fun selectImageToSend() {
        if (!AndroidUtils.hasPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, object :
                IRequestPermissionCallback {
                override fun onSuccess() {
                    selectImageToSend()
                }

                override fun onFailed() {
                    room.showMessage(resources.getString(R.string.frame_room_external_storage_denied))
                }

            })
            return
        }
        getImageActivity.launch("image/*")
    }

    override fun setTitle(title: CharSequence) {
        tvRoomTitle.text = title
    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveActivity(this)

        if (room.networkHandler?.isStopped == true) {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        room.networkHandler.stop()
        MainActivity.removeActivity()
    }

    override fun exitRoom(reason: IRoomFrame.ExitReason) {
        room.showMessage(
            resources.getString(
                when (reason) {
                    IRoomFrame.ExitReason.ClientException -> R.string.frame_room_exit_reason_client_exception
                    IRoomFrame.ExitReason.ConnectingToNewRoom -> R.string.frame_room_exit_reason_connecting_to_new_room
                    IRoomFrame.ExitReason.InvalidToken -> R.string.frame_room_exit_reason_invalid_token
                    IRoomFrame.ExitReason.InvalidUrl -> R.string.frame_room_exit_reason_invalid_url
                    IRoomFrame.ExitReason.PackageDecodeError -> R.string.frame_room_exit_reason_package_decode_error
                    IRoomFrame.ExitReason.Unknown -> R.string.frame_room_exit_reason_unknown
                }
            )
        )
        finish()
    }

    override fun onConnectionBuilt() {
        isConnectionBuilt = true
        messageManager.clearMessageArea()
        runOnUiThread {
            updateChatSendButtonState()
            buttonBar.forEach {
                it.isEnabled = true
            }
            textInputArea.isEnabled = true
            if (!isTextInputAreaCleared) {
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
            buttonBar.forEach {
                it.isEnabled = false
            }
            textInputArea.isEnabled = false
        }
    }

    override fun showSystemMessage(message: String) {
        messageManager.insertMessage(SystemMessage(message))
    }

    override fun showTextMessage(serialId: Long, sender: String, stamp: Long, text: String) {
        messageManager.insertMessage(ChatTextMessage(sender, stamp, serialId, text))
    }

    override fun showChatImageMessage(
        serialId: Long,
        sender: String,
        stamp: Long,
        serverFileId: UUID
    ): IFileTransferringPanel {
        val message = ChatImageMessage(this, sender, stamp, serialId)
        messageManager.insertMessage(message)
        return message
    }

    override fun showFileUploadedMessage(
        serialId: Long,
        sender: String,
        stamp: Long,
        fileId: UUID,
        fileName: String,
        fileSize: Long
    ): IFileTransferringPanel {
        val message = ChatFileMessage(
            sender, stamp, serialId,
            this, fileName, fileSize, fileId
        )
        messageManager.insertMessage(message)
        return message
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
        val message = ChatFileUploadingMessage(
            Config.getUsername(),
            System.currentTimeMillis(),
            this,
            fileNameGetter,
            fileSize
        )
        messageManager.insertMessage(message)
        return message
    }

    override fun updateUserList(userList: MutableCollection<User>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(resources.getString(R.string.frame_room_systeminfo_userlist))
        var isFirst = true
        for (user in userList) {
            if (isFirst) {
                isFirst = false
            } else {
                stringBuilder.append(", ")
            }
            stringBuilder.append(user.name)
        }
        showSystemMessage(stringBuilder.toString())
        userNum = userList.size
    }

    override fun onUserJoined(user: User) {
        showSystemMessage(
            String.format(
                resources.getString(R.string.frame_room_systeminfo_userjoin),
                user.name
            )
        )
        userNum++
    }

    override fun onUserLeft(user: User) {
        showSystemMessage(
            String.format(
                resources.getString(R.string.frame_room_systeminfo_userleft),
                user.name
            )
        )
        userNum--
    }

    override fun onUsernameChanged(user: User, previousName: String) {
        showSystemMessage(
            String.format(
                resources.getString(R.string.frame_room_systeminfo_changename),
                previousName,
                user.name
            )
        )
    }

    fun getMessageManager(): MessageManager {
        return messageManager
    }

    fun getHandler(): ClientRoom {
        return room
    }

}