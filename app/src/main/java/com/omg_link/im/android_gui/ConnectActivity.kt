package com.omg_link.im.android_gui

import IM.Client
import IM.Config
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import kotlin.concurrent.thread

class ConnectActivity : AppCompatActivity() {

    private val handler: Client

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            handler = activeClient
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

        urlInputArea.setText(Config.getUrl())
        nameInputArea.setText(Config.getUsername())

        connectButton.setOnClickListener {
            thread(start = true) {
                handler.setConfigAndStart(
                    urlInputArea.text.toString(),
                    nameInputArea.text.toString(),
                    false
                )
            }
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

    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveContext(this)
    }

}