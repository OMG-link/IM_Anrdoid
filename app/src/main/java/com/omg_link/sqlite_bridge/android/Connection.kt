package com.omg_link.sqlite_bridge.android

import android.database.sqlite.SQLiteDatabase
import com.omg_link.sqlite_bridge.Connection
import com.omg_link.sqlite_bridge.PreparedStatement
import com.omg_link.sqlite_bridge.Statement

class Connection(private val connection: SQLiteDatabase): Connection() {

    override fun createStatement(): Statement {
        return Statement(connection)
    }

    override fun prepareStatement(sql: String): PreparedStatement {
        return PreparedStatement(connection.compileStatement(sql))
    }

    override fun close() {
        connection.close()
    }

}