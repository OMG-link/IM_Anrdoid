package com.omg_link.im.message

import android.view.View
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.RoomActivity
import im.gui.IFileTransferringPanel
import mutils.FileUtils
import mutils.IStringGetter
import java.io.File

class FileUploadingMessage(
    username:String, stamp:Long,
    val activity: RoomActivity, val fileNameGetter: IStringGetter, val fileSize: Long
    ) : Message(username,stamp), IFileTransferringPanel {

    private var infoAreaHolder: TextView? = null

    private var info:String = ""
    set(value) {
        field = value
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

    override fun removeHolder() {
        super.removeHolder()
        infoAreaHolder = null
    }

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)
        val view = holder.createLayoutFromXML(R.layout.message_uploading)
        infoAreaHolder = view.findViewById(R.id.uploadInfo)
        info = info
        holder.addView(view)
    }

    override fun setProgress(uploadedSize: Long) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_progress),
            fileNameGetter.string,
            FileUtils.sizeToString(uploadedSize),
            FileUtils.sizeToString(fileSize)
        )
    }

    override fun onTransferStart() {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_start),
            fileNameGetter.string
        )
    }

    override fun onTransferSucceed(file: File?) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_succeed),
            fileNameGetter.string
        )
        activity.runOnUiThread {
            activity.getMessageManager().removeMessage(this)
        }
    }

    override fun onTransferFailed(reason: String?) {
        info = String.format(
            activity.resources.getString(R.string.frame_room_file_upload_failed),
            fileNameGetter.string,
            reason
        )
    }

}