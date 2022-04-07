package com.omg_link.im.android.file

import com.omg_link.im.android.FileManagerActivity
import com.omg_link.im.android.tools.AndroidUtils
import com.omg_link.utils.FileUtils
import java.io.File

class DownloadedFile(val activity: FileManagerActivity, val file: File) {

    fun onDataUpdated(holder: FilePanelHolder){
        holder.tvFileName.text = file.name
        holder.tvFileSize.text = FileUtils.sizeToString(file.length())
        holder.btnOpen.setOnClickListener {
            AndroidUtils.openFile(file,it.context)
        }
        holder.btnDelete.setOnClickListener {
            activity.deleteFile(this)
        }
    }

}