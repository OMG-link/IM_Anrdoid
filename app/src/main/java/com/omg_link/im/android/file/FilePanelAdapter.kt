package com.omg_link.im.android.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.databinding.DownloadedFileBinding

class FilePanelAdapter(private val data:List<DownloadedFile>):RecyclerView.Adapter<FilePanelHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilePanelHolder {
        return FilePanelHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FilePanelHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class FilePanelHolder private constructor(itemView: View):RecyclerView.ViewHolder(itemView){
    private val binding = DownloadedFileBinding.bind(itemView)

    val tvFileName = binding.tvDownloadedFileFileName
    val tvFileSize = binding.tvDownloadedFileFileSize
    val btnOpen = binding.btnOpen
    val btnDelete = binding.btnDelete

    fun bind(file: DownloadedFile){
        file.onDataUpdated(this)
    }

    companion object{
        fun from(parent: ViewGroup): FilePanelHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val root = layoutInflater.inflate(
                R.layout.downloaded_file,
                parent,false
            )
            return FilePanelHolder(root)
        }
    }

}