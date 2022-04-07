package com.omg_link.im.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.omg_link.im.R
import com.omg_link.im.core.Client
import com.omg_link.im.core.gui.IConfirmDialogCallback
import com.omg_link.im.core.gui.IGui
import com.omg_link.sqlite_bridge.android.SqlComponentFactory

class AndroidGUI(private val appCompatActivity: AppCompatActivity) : IGui {
    private val client: Client = Client(this)

    init {
        MainActivity.setActiveClient(client)
        client.gui.createConnectFrame()
    }

    override fun createConnectFrame() {
        val intent = Intent(appCompatActivity, ConnectActivity::class.java)
        appCompatActivity.startActivity(intent)
    }

    override fun createRoomFrame() {
        val intent = Intent(appCompatActivity, RoomActivity::class.java)
        appCompatActivity.startActivity(intent)
    }

    override fun showMessageDialog(message: String) {
        Log.d("AndroidGUI",message)
        val context = MainActivity.getActiveActivity()
        if(context!=null){
            appCompatActivity.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showException(e: Exception) {
        Log.d("AndroidGUI",e.toString())
        e.printStackTrace()
    }

    override fun showConfirmDialog(message: String, callback: IConfirmDialogCallback) {
        appCompatActivity.runOnUiThread {
            MainActivity.getActiveActivity()?.let {
                AlertDialog.Builder(it)
                    .setMessage(message)
                    .setPositiveButton(appCompatActivity.resources.getString(R.string.yes)) { dialogInterface, _ ->
                        callback.onPositiveInput()
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(appCompatActivity.resources.getString(R.string.no)) { dialogInterface, _ ->
                        callback.onNegativeInput()
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
            } ?: let {
                Log.d("Android", message)
            }
        }
    }

    override fun openInBrowser(uri: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(
            Uri.parse(uri),
            "text/html"
        )
        appCompatActivity.startActivity(intent)
    }

    override fun alertVersionMismatch(serverVersion: String?, clientVersion: String?) {
        showConfirmDialog(
            String.format(appCompatActivity.resources.getString(R.string.activity_room_version_warning),serverVersion,clientVersion),
            object : IConfirmDialogCallback{
                override fun onPositiveInput() {
                    openInBrowser("https://www.omg-link.com:8888/IM/")
                }

                override fun onNegativeInput() {

                }

            }
        )
    }

    override fun alertVersionIncompatible(serverVersion: String?, clientVersion: String?) {
        showConfirmDialog(
            String.format(appCompatActivity.resources.getString(R.string.activity_room_version_error),serverVersion,clientVersion),
            object : IConfirmDialogCallback{
                override fun onPositiveInput() {
                    val currentActivity =
                        MainActivity.getActiveActivity()
                    if(currentActivity is RoomActivity){
                        currentActivity.finish()
                    }
                    openInBrowser("https://www.omg-link.com:8888/IM/")
                }

                override fun onNegativeInput() {
                    val currentActivity =
                        MainActivity.getActiveActivity()
                    if(currentActivity is RoomActivity){
                        currentActivity.finish()
                    }
                }

            }
        )
    }

    override fun getSqlComponentFactory(): SqlComponentFactory {
        return SqlComponentFactory()
    }

    override fun alertVersionUnrecognizable(clientVersion: String?) {
        showConfirmDialog(
            String.format(appCompatActivity.resources.getString(R.string.activity_room_version_unrecognizable)),
            object : IConfirmDialogCallback{
                override fun onPositiveInput() {
                    val currentActivity =
                        MainActivity.getActiveActivity()
                    if(currentActivity is RoomActivity){
                        currentActivity.finish()
                    }
                    openInBrowser("https://www.omg-link.com:8888/IM/")
                }

                override fun onNegativeInput() {
                    val currentActivity =
                        MainActivity.getActiveActivity()
                    if(currentActivity is RoomActivity){
                        currentActivity.finish()
                    }
                }

            }
        )
    }

}