package com.omg_link.im

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.file.DownloadedFile
import com.omg_link.im.file.FilePanelAdapter
import im.Client
import java.io.File
import java.io.FileNotFoundException

class FileManagerActivity : AppCompatActivity() {

    private val downloadedFileList = ArrayList<DownloadedFile>()
    private val adapter = FilePanelAdapter(downloadedFileList)
    private val client: Client

    init {
        val client = MainActivity.getActiveClient()
        if (client != null) {
            this.client = client
        } else {
            throw RuntimeException("Failed to initialize connect activity: main client not found!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)

        title = resources.getString(R.string.frame_filemanager_title)

        val downloadedFileRecyclerView = findViewById<RecyclerView>(R.id.rvFiles)
        downloadedFileRecyclerView.layoutManager = LinearLayoutManager(this)
        downloadedFileRecyclerView.adapter = adapter

        findViewById<Button>(R.id.btnRefresh).setOnClickListener { updateFromDirectory() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { deleteAll() }

        updateFromDirectory()

    }

    fun updateFromDirectory() {
        val files = File(client.fileManager.folderName).listFiles() ?: return
        downloadedFileList.clear()
        for (file in files) {
            try{
                downloadedFileList.add(
                    DownloadedFile(
                        this,
                        client.fileManager.openFile(file)
                    )
                )
            }catch (ignored:FileNotFoundException){}
        }
        adapter.notifyDataSetChanged()
    }

    fun deleteFile(downloadedFile: DownloadedFile) {
        client.fileManager.deleteFile(downloadedFile.fileObject)
        updateFromDirectory()
    }

    fun deleteAll() {
        for(downloadedFile in downloadedFileList){
            client.fileManager.deleteFile(downloadedFile.fileObject)
        }
        updateFromDirectory()
    }

}