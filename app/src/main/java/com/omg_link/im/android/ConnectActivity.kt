package com.omg_link.im.android

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.res.ResourcesCompat
import com.omg_link.im.R
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.im.android.tools.BitmapUtils
import com.omg_link.im.android.tools.UriUtils
import com.omg_link.im.core.Client
import com.omg_link.im.core.config.Config
import com.omg_link.im.core.config.ConfigSetFailedException
import com.omg_link.im.core.gui.IConnectFrame
import com.omg_link.im.databinding.ActivityLoginBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread


fun getAvatarPath() = Config.getRuntimeDir()+"/avatar"

class ConnectActivity : AppCompatActivity(), IConnectFrame {

    private val client: Client

    private lateinit var binding: ActivityLoginBinding

    private lateinit var setAvatarActivity: ActivityResultLauncher<String>

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            client = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect roomActivity: main client not found!")
        }
    }

    private fun registerActivities() {
        setAvatarActivity =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                val file = File(
                    UriUtils.getFileAbsolutePath(this@ConnectActivity, uri)
                        ?: return@registerForActivityResult
                )
                if (!file.canRead()) {
                    client.showMessage("Unable to read file ${file.absolutePath}. Set failed!")
                    return@registerForActivityResult
                }
                FileInputStream(file).use { fileInputStream ->
                    FileOutputStream(File(getAvatarPath())).use { fileOutputStream ->
                        FileUtils.copy(
                            fileInputStream,
                            fileOutputStream
                        )
                    }
                }
                BitmapUtils.getBitmap(getAvatarPath(),true) // force refresh avatar
                binding.loginAvatar.setImageDrawable(getAvatar())
                client.showMessage(resources.getString(R.string.frame_login_avatar_set_succeed))
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = ""

        val urlInputArea: EditText = binding.urlInputArea
        val nameInputArea: EditText = binding.nameInputArea
        val tokenInputArea: EditText = binding.tokenInputArea
        val connectButton: Button = binding.connectButton
        val optionsButton: Button = binding.btnOption

        urlInputArea.setText(Config.getUrl())
        nameInputArea.setText(Config.getUsername())
        tokenInputArea.setText(Config.getToken())

        connectButton.setOnClickListener {
            thread {
                try {
                    client.connectToRoom(
                        urlInputArea.text.toString(),
                        nameInputArea.text.toString(),
                        tokenInputArea.text.toString()
                    )
                } catch (e: ConfigSetFailedException) {
                    client.showMessage(
                        resources.getString(
                            when (e.reason!!) {
                                ConfigSetFailedException.Reason.InvalidPort -> {
                                    R.string.frame_login_invalid_port
                                }
                                ConfigSetFailedException.Reason.InvalidUrl -> {
                                    R.string.frame_login_invalid_url
                                }
                                ConfigSetFailedException.Reason.UsernameTooLong -> {
                                    R.string.frame_login_invalid_username
                                }
                            }
                        )
                    )
                }
            }
        }

        optionsButton.setOnClickListener {
            showPopupMenu(it)
        }

        /*val debugButton: Button = binding.debugButton
        debugButton.setOnClickListener {
            handler.showCheckBox("Test message.",object:IConfirmDialogCallback{
                override fun onPositiveInput() {

                }

                override fun onNegativeInput() {

                }

            })
        }*/

        binding.tvCoreVersion.text = String.format(
            resources.getString(R.string.frame_login_core_version),
            Config.version
        )

        binding.tvApkVersion.text = String.format(
            resources.getString(R.string.frame_login_apk_version),
            AndroidUtils.getApkVersion(this)
        )

        binding.loginAvatar.setImageDrawable(getAvatar())
        binding.loginAvatar.setOnClickListener {
            setAvatarActivity.launch("image/*")
        }

        registerActivities()

        client.setConnectFrame(this)

    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.removeActivity()
    }

    private fun getAvatar(): Drawable {
        val avatarPath = getAvatarPath()
        val file = File(avatarPath)
        if(file.isFile){
            val bitmap = BitmapUtils.getBitmap(avatarPath)
            if(bitmap!=null) {
                return BitmapDrawable(resources, bitmap)
            }
        }
        return ResourcesCompat.getDrawable(resources,R.drawable.avatar,theme)!!
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.connect_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_set_avatar -> {
                    setAvatarActivity.launch("image/*")
                }
                R.id.menu_manage_downloaded_file -> {
                    val intent = Intent(this, FileManagerActivity::class.java)
                    startActivity(intent)
                }
                R.id.menu_download_other_version -> {
                    client.gui.openInBrowser("https://www.omg-link.com/IM/")
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
    }

}