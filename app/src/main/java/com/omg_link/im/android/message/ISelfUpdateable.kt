package com.omg_link.im.android.message

interface ISelfUpdatable<Holder> {
    fun removeHolder(holder: Holder)
    fun setHolder(holder:Holder)
}