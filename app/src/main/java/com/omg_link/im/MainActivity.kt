package com.omg_link.im

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import im.Client
import im.config.Config
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private var activeClient: Client? = null

        fun setActiveClient(client: Client) {
            activeClient = client
        }

        fun getActiveClient(): Client?{
            return activeClient
        }

        private var activityStack = Stack<Activity>()

        fun setActiveActivity(activity: Activity) {
            activityStack.push(activity)
        }

        fun getActiveActivity(): Activity? {
            return try{
                activityStack.peek()
            }catch (e:EmptyStackException){
                null
            }
        }

        fun removeActivity(){
            activityStack.pop()
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
        MainActivity.setActiveActivity(this)
        AndroidGUI(this)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.removeActivity()
    }

}