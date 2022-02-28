package com.omg_link.im

import IM.Client
import IM.Config
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.omg_link.im.android_gui.AndroidGUI
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    companion object {
        private var activeClient: Client? = null

        fun setActiveClient(client: Client) {
            activeClient = client
        }

        fun getActiveClient(): Client?{
            return activeClient
        }

        private var activeContext: Context? = null

        fun setActiveContext(context: Context) {
            activeContext = context
        }

        fun getActiveContext(): Context?{
            return activeContext
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Config.setRuntimeDir(filesDir.absolutePath)
        Config.setCacheDir(cacheDir.absolutePath)
        Config.updateFromFile()

    }

    override fun onResume() {
        super.onResume()
        MainActivity.setActiveContext(this)
        AndroidGUI(this)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }

}