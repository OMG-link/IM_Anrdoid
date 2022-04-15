package com.omg_link.im.android.emoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.android.tools.BitmapUtils
import com.omg_link.im.android.tools.ViewUtils
import com.omg_link.im.core.ClientRoom
import com.omg_link.im.core.file_manager.FileObject
import kotlin.concurrent.thread

class EmojiHolder (itemView: View, private val room: ClientRoom) :
    RecyclerView.ViewHolder(itemView) {

    constructor(context: Context,parent: ViewGroup,room:ClientRoom): this(createView(context,parent),room)

    private val ivEmoji: ImageView = itemView.findViewById(R.id.ivEmoji)
    private val layoutEmoji: ConstraintLayout = itemView.findViewById(R.id.layoutEmoji)

    fun bind(fileObject: FileObject){
        val bitmap = BitmapUtils.getBitmap(fileObject.file.absolutePath)
        if(bitmap==null){
            ivEmoji.setImageDrawable(ResourcesCompat.getDrawable(itemView.context.resources,R.drawable.image_resolve_failed,null))
            ivEmoji.setOnClickListener(null)
        }else{
            ivEmoji.setImageBitmap(bitmap)
            layoutEmoji.setOnClickListener {
                thread {
                    room.sendChatImage(
                        fileObject.file
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