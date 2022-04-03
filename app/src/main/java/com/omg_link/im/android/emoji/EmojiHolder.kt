package com.omg_link.im.android.emoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.android.MainActivity
import com.omg_link.im.R
import com.omg_link.im.android.tools.BitmapUtils
import com.omg_link.im.android.tools.ViewUtils
import im.file_manager.FileObject
import mutils.ImageType
import kotlin.concurrent.thread

class EmojiHolder (itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    constructor(context: Context,parent: ViewGroup): this(createView(context,parent))

    private val ivEmoji: ImageView = itemView.findViewById(R.id.ivEmoji)
    private val layoutEmoji: ConstraintLayout = itemView.findViewById(R.id.layoutEmoji)

    fun bind(fileObject: FileObject){
        val bitmap = BitmapUtils.getBitmap(fileObject.file.absolutePath)
        if(bitmap==null){
            ivEmoji.setImageDrawable(ResourcesCompat.getDrawable(itemView.context.resources,R.drawable.icon_error,null))
            ivEmoji.setOnClickListener(null)
        }else{
            ivEmoji.setImageBitmap(bitmap)
            layoutEmoji.setOnClickListener {
                thread {
                    MainActivity.getActiveClient()!!.sendChatImage(
                        fileObject.file, ImageType.PNG
                    )
                }
            }
        }
    }

    companion object{
        fun createView(context: Context, parent: ViewGroup): View {
            return ViewUtils.createLayoutFromXML(context, parent, R.layout.emoji)
        }
    }

}