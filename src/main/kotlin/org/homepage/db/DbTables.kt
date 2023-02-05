package org.homepage.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object AppInstallationTable : Table("app_installation") {
    val clientId = varchar("client_id", 36).index(isUnique = true)
    val clientSecret = varchar("client_secret", 64)
    val serverUrl = varchar("server_url", 256).index()

    override val primaryKey = PrimaryKey(clientId)
}

object IncomingValentineTable : LongIdTable("incoming_valentine3") {
    val serverUrl = varchar("server_url", 256)
    val receiver = varchar("receiver_id", 64)
    val message = varchar("message", 1024)
    val read = bool("read").default(false)
    val counterUpdated = bool("counterUpdated").default(false)

    init {
        index(isUnique = false, serverUrl, receiver)
    }
}

