package com.omg_link.sqlite_bridge.android

import android.database.sqlite.SQLiteDatabase
import com.omg_link.sqlite_bridge.Connection
import com.omg_link.sqlite_bridge.SqlComponentFactory

class SqlComponentFactory: SqlComponentFactory() {
    override fun createConnection(fileName: String): Connection {
        return Connection(SQLiteDatabase.openOrCreateDatabase(fileName,null))
    }

}