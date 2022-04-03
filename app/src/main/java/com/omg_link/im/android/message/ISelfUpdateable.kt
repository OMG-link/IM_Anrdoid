package com.omg_link.im.android.message

interface ISelfUpdatable<Holder> {
    fun removeHolder()
    fun setHolder(holder:Holder)
}