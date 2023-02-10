package org.homepage

import org.homepage.db.AppInstallationTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.helpers.SpaceAppInstanceStorage

object AppInstanceStorage : SpaceAppInstanceStorage {
    override suspend fun loadAppInstance(clientId: String): SpaceAppInstance? {
        return transaction {
            AppInstallationTable.select { AppInstallationTable.clientId.eq(clientId) }
                .map {
                    SpaceAppInstance(
                        it[AppInstallationTable.clientId],
                        it[AppInstallationTable.clientSecret],
                        it[AppInstallationTable.serverUrl],
                    )
                }
                .firstOrNull()
        }
    }

    override suspend fun removeAppInstance(clientId: String) {
        AppInstallationTable.deleteWhere { AppInstallationTable.clientId eq clientId }
    }

    override suspend fun saveAppInstance(appInstance: SpaceAppInstance): Unit = transaction {
        with(AppInstallationTable) {
            val serverUrlAlreadyExists = slice(clientId)
                .select { serverUrl eq appInstance.spaceServer.serverUrl }
                .forUpdate()
                .any()

            if (serverUrlAlreadyExists) {
                update(where = { serverUrl eq appInstance.spaceServer.serverUrl }) {
                    it[clientId] = appInstance.clientId
                    it[clientSecret] = appInstance.clientSecret
                }
            } else {
                replace {
                    it[clientId] = appInstance.clientId
                    it[clientSecret] = appInstance.clientSecret
                    it[serverUrl] = appInstance.spaceServer.serverUrl
                }
            }
        }
    }
}
