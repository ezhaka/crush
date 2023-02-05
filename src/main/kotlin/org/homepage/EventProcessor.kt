package org.homepage

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.homepage.db.IncomingValentineTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient

private val log = LoggerFactory.getLogger("EventProcessor.kt")

class ModificationEvent(val spaceServerInstance: SpaceAppInstance, val id: Long, val receiverId: String) {
    override fun toString(): String {
        return "ModificationEvent(spaceServerInstance=${spaceServerInstance.spaceServer.serverUrl}, id=$id, receiverId='$receiverId')"
    }
}

val eventChannel = Channel<ModificationEvent>(capacity = 1024)

fun processEvents() {
    // TODO: process stale profiles

    GlobalScope.launch {
        for (event in eventChannel) {
            try {
                val (totalCount, unreadCount) = transaction {
                    val totalCount = IncomingValentineTable
                        .slice(IncomingValentineTable.id.count())
                        .select { matches(event) }
                        .single()
                        .let { it[IncomingValentineTable.id.count()] }

                    val unreadCount = IncomingValentineTable
                        .slice(IncomingValentineTable.id.count())
                        .select { matches(event) and (IncomingValentineTable.read eq false) }
                        .single()
                        .let { it[IncomingValentineTable.id.count()] }

                    totalCount to unreadCount
                }

                val client = SpaceClient(spaceHttpClient, event.spaceServerInstance, SpaceAuth.ClientCredentials())

                client.ktorClient.put("${client.server.apiBaseUrl}/internal-heart/update-counters") {
                    this.expectSuccess = true

                    contentType(ContentType.Application.Json)
                    bearerAuth(client.token().accessToken)
                    setBody("{\"counters\":[{\"profileId\":\"${event.receiverId}\",\"unreadCount\":${unreadCount},\"totalCount\":${totalCount}}]}")
                }

                transaction {
                    IncomingValentineTable.update(where = { IncomingValentineTable.id eq event.id }) {
                        it[counterUpdated] = true
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to process event $event", e)
            }
        }
    }
}

private fun SqlExpressionBuilder.matches(event: ModificationEvent) =
    (IncomingValentineTable.serverUrl eq event.spaceServerInstance.spaceServer.serverUrl) and
            (IncomingValentineTable.receiver eq event.receiverId)