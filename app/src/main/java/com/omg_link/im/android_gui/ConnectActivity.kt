package com.omg_link.im.android_gui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.MainActivity
import com.omg_link.im.R
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
        val connectButton: Button = findViewById(R.id.connectButton)
        val downloadOtherVersionButton: Button = findViewById(R.id.downloadApkButton)

        urlInputArea.setText(Config.getUrl())
        nameInputArea.setText(Config.getUsername())

        connectButton.setOnClickListener {
            thread {
                try{
                    client.setConfigAndStart(
                        urlInputArea.text.toString(),
                        nameInputArea.text.toString(),
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