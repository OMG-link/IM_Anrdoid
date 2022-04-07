package com.omg_link.sqlite_bridge.android

import android.database.Cursor

class Cursor(private val cursor: Cursor): com.omg_link.sqlite_bridge.Cursor() {

    override fun close() {
        cursor.close()
    }

    override fun next(): Boolean {
        return cursor.moveToNext()
    }

    override fun getString(columnName: String): String? {
        val index = cursor.getColumnIndex(columnName)
        return if(index<0) null
        else cursor.getString(index)
    }

    override fun getBytes(columnName: String): ByteArray? {
        val index = cursor.getColumnIndex(columnName)
        return if(index<0) null
        else cursor.getBlob(index)
    }

    override fun getLong(columnName: String): Long {
        val index = cursor.getColumnIndex(columnName)
        return if(index<0) 0
        else cursor.getLong(index)
    }

}