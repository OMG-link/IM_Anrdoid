package com.omg_link.im.android_gui

import GUI.IFileTransferringPanel

class FileTransferringPanel : IFileTransferringPanel {
    override fun setVisible(b: Boolean) {
        //always visible
    }

    override fun setProgress(progress: Double) {
        //not implemented
    }

    override fun setInfo(info: String?) {
        //not implemented
    }
}