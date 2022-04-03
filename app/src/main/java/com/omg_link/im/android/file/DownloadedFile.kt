package com.omg_link.im.android.file

import com.omg_link.im.android.FileManagerActivity
import com.omg_link.im.android.tools.AndroidUtils
import im.file_manager.FileObject
import mutils.FileUtils

class DownloadedFile(val activity: FileManagerActivity, val fileObject:FileObject) {

    fun onDataUpdated(holder: FilePanelHolder){
        holder.tvFileName.text = fileObject.file.name
        holder.tvFileSize.text = FileUtils.sizeToString(fileObject.file.length())
        holder.btnOpen.setOnClickListener {
            AndroidUtils.openFile(fileObject.file,it.context)
        }
        holder.btnDelete.setOnClickListener {
            activity.deleteFile(this)
        }
    }

}