package org.homepage.db

import org.homepage.db.AppInstallation.index
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object AppInstallation : Table("app_installation") {
    val clientId = varchar("client_id", 36).index(isUnique = true)
    val clientSecret = varchar("client_secret", 64)
    val serverUrl = varchar("server_url", 256)

    override val primaryKey = PrimaryKey(clientId)
}

object IncomingValentine : LongIdTable("incoming_valentine2") {
    val clientId = varchar("client_id", 36)
    val receiver = varchar("receiver_id", 64)
    val message = varchar("message", 1024)
    val read = bool("read").default(false)

    init {
        uniqueIndex(clientId, receiver)
    }
}

