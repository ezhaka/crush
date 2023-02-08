package org.homepage.actors

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import org.homepage.db.IncomingValentineTable
import org.homepage.spaceHttpClient
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient

private val log = LoggerFactory.getLogger("EventProcessor.kt")
fun CoroutineScope.counterUpdateActor() = actor<ValentineMod>(capacity = 1024) {
    for (event in channel) {
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

            val body =
                "{\"counters\":[{\"profileId\":\"${event.userId.spaceUserId}\",\"unreadCount\":${unreadCount},\"totalCount\":${totalCount}}]}"
            log.info("Going to update counters with the following body: $body")

            client.ktorClient.put("${client.server.apiBaseUrl}/internal-heart/update-counters") {
                this.expectSuccess = true

                contentType(ContentType.Application.Json)
                bearerAuth(client.token().accessToken)
                setBody(body)
            }

            transaction {
                IncomingValentineTable.update(where = { IncomingValentineTable.id eq event.id }) {
                    it[incomingCounterUpdated] = true
                    it[readCounterUpdated] = Case(null)
                        .When(EqOp(readCounterUpdated, booleanLiteral(false)), booleanLiteral(true))
                        .Else(Op.nullOp())
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process event $event", e)
        }
    }
}

private fun SqlExpressionBuilder.matches(event: ValentineMod) =
    (IncomingValentineTable.serverUrl eq event.spaceServerInstance.spaceServer.serverUrl) and
            (IncomingValentineTable.receiver eq event.userId.spaceUserId)