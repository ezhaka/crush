package org.homepage.actors

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.homepage.SpaceGlobalUserId
import org.homepage.db.AppInstallationTable
import org.homepage.db.IncomingValentineTable
import org.homepage.setUiExtensions
import org.homepage.spaceHttpClient
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient

private val log: Logger = LoggerFactory.getLogger("CounterUpdateActor.kt")

@Serializable
class HeartCountersIn(
    val profileId: String,
    val unreadCount: Long,
    val totalCount: Long,
)

@Serializable
class HeartCountersBody(val counters: List<HeartCountersIn>)

fun CoroutineScope.userCounterUpdateActor() = actor<ValentineMod>(capacity = 4) {
    for (event in channel) {
        try {
            val (totalCount, unreadCount) = transaction {
                val unreadCountExpr = Sum(
                    Case(null).When(EqOp(IncomingValentineTable.read, booleanLiteral(false)), intLiteral(1))
                        .Else(intLiteral(0)), IntegerColumnType()
                ).alias("unreadCount")

                val (totalCount, unreadCount) = IncomingValentineTable
                    .slice(IncomingValentineTable.id.count(), unreadCountExpr)
                    .select { matches(event) }
                    .single()
                    .let { it[IncomingValentineTable.id.count()] to it[unreadCountExpr] }

                totalCount to (unreadCount?.toLong() ?: 0L)
            }

            val client = SpaceClient(spaceHttpClient, event.spaceServerInstance, SpaceAuth.ClientCredentials())

            sendCounters(
                client, listOf(
                    HeartCountersIn(
                        profileId = event.userId.spaceUserId,
                        unreadCount = unreadCount,
                        totalCount = totalCount
                    )
                )
            )

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

fun CoroutineScope.counterUpdateActor() = actor<ValentineMod>(capacity = 1024) {

    log.info("Updating stale counters")
    try {
        updateStaleCounters()
    } catch (e: Exception) {
        log.error("Stale counters update failed", e)
    }

    log.info("Starting reading inbox events")

    val userActors = mutableMapOf<SpaceGlobalUserId, SendChannel<ValentineMod>>()

    supervisorScope {
        for (event in channel) {
            val userActor = userActors.getOrPut(event.userId) {
                userCounterUpdateActor()
            }

            userActor.send(event)
        }
    }
}

private suspend fun updateStaleCounters() {
    val staleCounters = transaction {
        IncomingValentineTable
            .slice(IncomingValentineTable.serverUrl, IncomingValentineTable.receiver)
            .select {
                (IncomingValentineTable.incomingCounterUpdated eq false) or (IncomingValentineTable.readCounterUpdated eq false)
            }
            .map {
                it[IncomingValentineTable.serverUrl] to it[IncomingValentineTable.receiver]
            }
    }

    for ((serverUrl, receiverIds) in staleCounters.groupBy({ it.first }, { it.second })) {
        try {
            val appInstance = transaction {
                AppInstallationTable.select { AppInstallationTable.serverUrl eq serverUrl }.firstOrNull()?.let {
                    SpaceAppInstance(
                        clientId = it[AppInstallationTable.clientId],
                        clientSecret = it[AppInstallationTable.clientSecret],
                        spaceServerUrl = it[AppInstallationTable.serverUrl],
                    )
                }
            } ?: continue

            val client = SpaceClient(spaceHttpClient, appInstance, SpaceAuth.ClientCredentials())

            val counters = transaction {
                val unreadCountExpr = Sum(
                    Case(null).When(EqOp(IncomingValentineTable.read, booleanLiteral(false)), intLiteral(1))
                        .Else(intLiteral(0)), IntegerColumnType()
                ).alias("unreadCount")
                val totalCountExpr = IncomingValentineTable.id.count()

                IncomingValentineTable
                    .slice(
                        IncomingValentineTable.receiver,
                        totalCountExpr,
                        unreadCountExpr
                    )
                    .select {
                        (IncomingValentineTable.serverUrl eq serverUrl) and
                                (IncomingValentineTable.receiver inList receiverIds)
                    }
                    .groupBy(IncomingValentineTable.receiver)
                    .map {
                        HeartCountersIn(
                            profileId = it[IncomingValentineTable.receiver],
                            unreadCount = (it[unreadCountExpr] ?: 0).toLong(),
                            totalCount = it[totalCountExpr]
                        )
                    }
            }

            sendCounters(client, counters)

            transaction {
                IncomingValentineTable.update(where = {
                    (IncomingValentineTable.serverUrl eq serverUrl) and
                            (IncomingValentineTable.receiver inList receiverIds)
                }) {
                    it[incomingCounterUpdated] = true
                    it[readCounterUpdated] = Case(null)
                        .When(EqOp(readCounterUpdated, booleanLiteral(false)), booleanLiteral(true))
                        .Else(Op.nullOp())
                }
            }
        } catch (e: Exception) {
            log.error("Stale counters update failed for server $serverUrl", e)
        }
    }
}

private suspend fun sendCounters(client: SpaceClient, counters: List<HeartCountersIn>) {
    val body = Json.encodeToString(
        HeartCountersBody(counters)
    )

    client.ktorClient.put("${client.server.apiBaseUrl}/internal-heart/update-counters") {
        this.expectSuccess = true

        contentType(ContentType.Application.Json)
        bearerAuth(client.token().accessToken)
        setBody(body)
    }
}

private fun SqlExpressionBuilder.matches(event: ValentineMod) =
    (IncomingValentineTable.serverUrl eq event.spaceServerInstance.spaceServer.serverUrl) and
            (IncomingValentineTable.receiver eq event.userId.spaceUserId)