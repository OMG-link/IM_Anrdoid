package com.omg_link.im.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.android.file.DownloadedFile
import com.omg_link.im.android.file.FilePanelAdapter
import com.omg_link.im.core.Client
import com.omg_link.im.core.config.Config
import com.omg_link.im.core.file_manager.ClientFileManager
import com.omg_link.im.databinding.ActivityFileManagerBinding
import java.io.File
import java.io.FileNotFoundException

class FileManagerActivity : AppCompatActivity() {

    private val client: Client
    private lateinit var binding: ActivityFileManagerBinding

    private val downloadedFileList = ArrayList<DownloadedFile>()
    private val adapter = FilePanelAdapter(downloadedFileList)

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            this.client = activeClient
        } else {
            throw RuntimeException("Failed to initialize connect roomActivity: main client not found!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = resources.getString(R.string.frame_filemanager_title)

        val downloadedFileRecyclerView = binding.rvFiles
        downloadedFileRecyclerView.layoutManager = LinearLayoutManager(this)
        downloadedFileRecyclerView.adapter = adapter

        binding.btnRefresh.setOnClickListener { updateFromDirectory() }
        binding.btnClear.setOnClickListener { deleteAll() }

        updateFromDirectory()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFromDirectory() {
        val files = client.fileManager.openFolder(ClientFileManager.downloadFolder).listFiles()
            ?: return
        downloadedFileList.clear()
        for (file in files) {
            try{
                downloadedFileList.add(
                    DownloadedFile(
                        this,
                        file
                    )
                )
            }catch (ignored:FileNotFoundException){}
        }
        adapter.notifyDataSetChanged()
    }

    fun deleteFile(downloadedFile: DownloadedFile) {
        downloadedFile.file.delete()
        updateFromDirectory()
    }

    fun deleteAll() {
        for(downloadedFile in downloadedFileList){
            downloadedFile.file.delete()
        }
        updateFromDirectory()
    }

}