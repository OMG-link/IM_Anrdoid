package com.omg_link.im.message

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.omg_link.im.R
import com.omg_link.im.tools.ViewUtils.createLayoutFromXML

abstract class Message(val stamp: Long) {

    object Type {
        const val UNKNOWN = 0
        const val TEXT = 1
        const val IMAGE = 2
        const val FILE = 3
        const val UPLOADING = 4
        const val TIME = 5
        const val SYSTEM = 6
    }

    abstract val isUserMessage: Boolean
    abstract val type: Int

    var messageManager: MessageManager? = null

}

abstract class MessageHolder protected constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    private val context = itemView.context

    init {
        itemView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (this::message.isInitialized) {
                message.messageManager!!.keepBottom()
            }
        }
    }

    /**
     * The message that the holder is now holding.
     */
    private lateinit var message: Message

    protected open fun bind(message: Message) {
        this.message = message
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup, children: List<View>? = null): View {
            val view = createLayoutFromXML(context, parent, R.layout.message)
            if (children != null) {
                val layoutChildren: LinearLayout = view.findViewById(R.id.layoutMessageChildren)
                for (child in children) {
                    layoutChildren.addView(child)
                }
            }
            return view
        }
    }

    // Some useful functions.

    fun getColor(@ColorRes resId: Int): Int {
        return context.resources.getColor(resId, null)
    }

    fun getAttrColor(resId: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(resId, value, true)
        assert(value.isColorType)
        return value.data
    }

    fun getString(@StringRes resId: Int): String {
        return context.resources.getString(resId)
    }

}