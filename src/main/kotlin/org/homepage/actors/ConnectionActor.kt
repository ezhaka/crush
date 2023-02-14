package org.homepage.actors

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.homepage.IncomingValentine
import org.homepage.WebsocketMessage
import org.homepage.db.IncomingValentineTable
import org.homepage.matchUser
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("ConnectionActor.kt")

class ConnectionActor(
    val session: WebSocketServerSession,
    val inbox: SendChannel<ValentineMod>
)

fun CoroutineScope.connectionActor(ctx: MainActorMsg.ConnectionOpened) = ConnectionActor(
    ctx.session,
    actor<ValentineMod>(capacity = 16) {
        try {
            val valentines = transaction {
                IncomingValentineTable
                    .select { matchUser(ctx.userId) }
                    .limit(1001)
                    .orderBy(IncomingValentineTable.id, SortOrder.DESC)
                    .map {
                        IncomingValentine(
                            it[IncomingValentineTable.id].value,
                            it[IncomingValentineTable.message],
                            it[IncomingValentineTable.cardType],
                            it[IncomingValentineTable.read],
                        )
                    }
            }

            ctx.session.sendSerialized<WebsocketMessage>(WebsocketMessage.ValentineListInit(valentines))

            for (event in channel) {
                when (event) {
                    is ValentineMod.Created -> {
                        ctx.session.sendSerialized<WebsocketMessage>(WebsocketMessage.ValentineReceived(event.valentine))
                    }

                    is ValentineMod.Read -> {
                        ctx.session.sendSerialized<WebsocketMessage>(WebsocketMessage.ValentineRead(event.id))
                    }
                }
            }
        } catch (e: CancellationException) {
            log.info("Cancellation exception has happened in the connection actor")
        } catch (e: Exception) {
            log.error("An exception occurred in the connection actor", e)
            ctx.session.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Something went wrong"))
        }
    }
)