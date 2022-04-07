package com.omg_link.sqlite_bridge.android

import android.database.sqlite.SQLiteStatement
import com.omg_link.sqlite_bridge.Cursor
import com.omg_link.sqlite_bridge.PreparedStatement
import java.sql.SQLException

class PreparedStatement(val preparedStatement:SQLiteStatement):PreparedStatement() {

    override fun setLong(index: Int, value: Long) {
        preparedStatement.bindLong(index,value)
    }

    override fun setBytes(index: Int, value: ByteArray) {
        preparedStatement.bindBlob(index,value)
    }

    override fun setString(index: Int, value: String) {
        preparedStatement.bindString(index,value)
    }

    override fun executeInsert() {
        preparedStatement.executeInsert()
    }

    override fun executeUpdateDelete() {
        preparedStatement.executeUpdateDelete()
    }

    override fun executeQuery(): Cursor {
        throw SQLException("Operation not supported.")
    }

}