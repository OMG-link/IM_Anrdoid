package com.omg_link.im.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.R
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.im.core.Client
import com.omg_link.im.core.config.Config
import com.omg_link.im.core.config.ConfigSetFailedException
import com.omg_link.im.core.gui.IConnectFrame
import com.omg_link.im.databinding.ActivityLoginBinding
import kotlin.concurrent.thread

class ConnectActivity : AppCompatActivity(), IConnectFrame {

    private val client: Client

    private lateinit var binding: ActivityLoginBinding

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            client = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect roomActivity: main client not found!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val urlInputArea: EditText = binding.urlInputArea
        val nameInputArea: EditText = binding.nameInputArea
        val tokenInputArea: EditText = binding.tokenInputArea
        val fileManagerButton: Button = binding.fileManagerButton
        val connectButton: Button = binding.connectButton
        val downloadOtherVersionButton: Button = binding.downloadApkButton

        urlInputArea.setText(Config.getUrl())
        nameInputArea.setText(Config.getUsername())
        tokenInputArea.setText(Config.getToken())

        fileManagerButton.setOnClickListener {
            val intent = Intent(this, FileManagerActivity::class.java)
            startActivity(intent)
        }

        connectButton.setOnClickListener {
            thread {
                try{
                    client.connectToRoom(
                        urlInputArea.text.toString(),
                        nameInputArea.text.toString(),
                        tokenInputArea.text.toString()
                    )
                }catch (e:ConfigSetFailedException){
                    client.showMessage(resources.getString(when(e.reason!!){
                        ConfigSetFailedException.Reason.InvalidPort -> {R.string.frame_login_invalid_port}
                        ConfigSetFailedException.Reason.InvalidUrl -> {R.string.frame_login_invalid_url}
                        ConfigSetFailedException.Reason.UsernameTooLong -> {R.string.frame_login_invalid_username}
                    }))
                }
            }
        }

        downloadOtherVersionButton.setOnClickListener {
            client.gui.openInBrowser("https://www.omg-link.com/IM/")
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

}