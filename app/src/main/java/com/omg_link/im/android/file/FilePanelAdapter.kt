package com.omg_link.im.android.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R

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
    val tvFileName = itemView.findViewById<TextView>(R.id.tvDownloadedFileFileName)
    val tvFileSize = itemView.findViewById<TextView>(R.id.tvDownloadedFileFileSize)
    val btnOpen = itemView.findViewById<Button>(R.id.btnOpen)
    val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

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