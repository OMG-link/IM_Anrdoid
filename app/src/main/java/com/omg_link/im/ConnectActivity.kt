package com.omg_link.im

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.tools.AndroidUtils
import im.Client
import im.config.Config
import im.config.InvalidUserNameException
import im.gui.IConnectFrame
import kotlin.concurrent.thread

class ConnectActivity : AppCompatActivity(), IConnectFrame {

    private val client: Client

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            client = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect activity: main client not found!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val urlInputArea: EditText = findViewById(R.id.urlInputArea)
        val nameInputArea: EditText = findViewById(R.id.nameInputArea)
        val tokenInputArea: EditText = findViewById(R.id.tokenInputArea)
        val fileManagerButton: Button = findViewById(R.id.fileManagerButton)
        val connectButton: Button = findViewById(R.id.connectButton)
        val downloadOtherVersionButton: Button = findViewById(R.id.downloadApkButton)

        urlInputArea.setText(Config.getUrl())
        nameInputArea.setText(Config.getUsername())
        tokenInputArea.setText(Config.getToken())

        fileManagerButton.setOnClickListener {
            val intent = Intent(this,FileManagerActivity::class.java)
            startActivity(intent)
        }

        connectButton.setOnClickListener {
            thread {
                try{
                    client.setConfigAndStart(
                        urlInputArea.text.toString(),
                        nameInputArea.text.toString(),
                        tokenInputArea.text.toString(),
                        false
                    )
                }catch (e:InvalidUserNameException){
                    client.showInfo(resources.getString(R.string.frame_login_invalid_username))
                }
            }
        }

        downloadOtherVersionButton.setOnClickListener {
            client.gui.openInBrowser("https://www.omg-link.com/IM/")
        }

        /*val debugButton: Button = findViewById(R.id.debugButton)
        debugButton.setOnClickListener {
            handler.showCheckBox("Test message.",object:IConfirmDialogCallback{
                override fun onPositiveInput() {

                }

                override fun onNegativeInput() {

                }

            })
        }*/

        findViewById<TextView>(R.id.tvCoreVersion).text = String.format(
            resources.getString(R.string.frame_login_core_version),
            Config.version
        )

        findViewById<TextView>(R.id.tvApkVersion).text = String.format(
            resources.getString(R.string.frame_login_apk_version),
            AndroidUtils.getApkVersion(this)
        )

        client.connectFrame = this

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