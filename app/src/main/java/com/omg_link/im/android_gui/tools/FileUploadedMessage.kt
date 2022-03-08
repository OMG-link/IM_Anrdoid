package com.omg_link.im.android_gui.tools

import GUI.IFileTransferringPanel
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import com.omg_link.im.tools.AndroidUtils
import com.omg_link.im.tools.UriUtils
import mutils.FileUtils
import protocol.dataPack.FileTransferType
import java.io.File
import java.util.*
import kotlin.concurrent.thread

enum class FilePanelState{
    READY,DOWNLOADING,DOWNLOADED
}

class FileUploadedMessage(
    username:String,stamp:Long,
    val activity:Activity,var fileName:String,val fileSize:Long,val fileId:UUID
    ):Message(username,stamp),IFileTransferringPanel {

    var infoAreaHolder: TextView? = null
    lateinit var file:File

    var panelState = FilePanelState.READY
    set(value) {
        field = value
        activity.runOnUiThread {
            onDataUpdated()
        }
    }

    private var info:String = ""
    set(value){
        field = value
        //Do not call onDataUpdated since this is called very often!
        if(infoAreaHolder!=null){
            activity.runOnUiThread {
                if (value.isEmpty()) {
                    infoAreaHolder?.visibility = View.GONE
                } else {
                    infoAreaHolder?.visibility = View.VISIBLE
                    infoAreaHolder?.text = value
                }
            }
        }
    }

    override fun setProgress(downloadedSize: Long) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_download_progress),
            fileName,
            FileUtils.sizeToString(downloadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        info = activity.resources.getString(R.string.frame_room_file_download_start)
        panelState = FilePanelState.DOWNLOADING
    }

    override fun onTransferSucceed(file: File) {
        this.file = file
        fileName = file.name
        info = String.format(activity.resources.getString(R.string.frame_room_file_download_succeed),fileName)
        panelState = FilePanelState.DOWNLOADED
    }

    override fun onTransferFailed(reason: String) {
        info = String.format(activity.resources.getString(R.string.frame_room_file_download_failed),reason)
        panelState = FilePanelState.READY
    }

    override fun removeHolder() {
        super.removeHolder()
        infoAreaHolder = null
    }

    /**
     * Run on UI Thread
     */
    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_filepanel)
        view.findViewById<TextView>(R.id.fileNameArea).text = fileName
        view.findViewById<TextView>(R.id.fileSizeArea).text = "(${FileUtils.sizeToString(fileSize)})"
        infoAreaHolder = view.findViewById(R.id.downloadInfoArea)
        info = info
        when(panelState){
            FilePanelState.READY -> {
                val downloadButton = view.findViewById<Button>(R.id.downloadButton)
                downloadButton.setOnClickListener {
                    val client = MainActivity.getActiveClient()
                        ?: return@setOnClickListener
                    thread {
                        client.downloadFile(fileName, fileId, FileTransferType.ChatFile, this)
                    }
                    panelState = FilePanelState.DOWNLOADING
                }
                downloadButton.visibility = View.VISIBLE
            }
            FilePanelState.DOWNLOADING -> {

            }
            FilePanelState.DOWNLOADED ->{
                val openButton = view.findViewById<Button>(R.id.openButton)
                openButton.setOnClickListener {
                    if(!AndroidUtils.openFile(file,activity)){
                        Toast.makeText(activity,R.string.activity_room_cannot_open_file,Toast.LENGTH_SHORT).show()
                    }
                }
                openButton.visibility = View.VISIBLE
            }
        }
        holder.addView(view)
    }

}