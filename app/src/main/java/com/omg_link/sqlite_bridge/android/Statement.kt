package com.omg_link.sqlite_bridge.android

import android.database.sqlite.SQLiteDatabase
import com.omg_link.sqlite_bridge.Cursor
import com.omg_link.sqlite_bridge.Statement

class Statement(private val db:SQLiteDatabase):Statement() {
    override fun close() {
    }

    override fun executeQuery(sql: String): Cursor {
        return Cursor(db.rawQuery(sql, arrayOf()))
    }

    override fun executeUpdate(sql: String) {
        db.execSQL(sql)
    }

}