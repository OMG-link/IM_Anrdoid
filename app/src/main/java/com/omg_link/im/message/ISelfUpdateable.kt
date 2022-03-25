package com.omg_link.im.message

interface ISelfUpdatable<Holder> {
    fun removeHolder()
    fun setHolder(holder:Holder)
}