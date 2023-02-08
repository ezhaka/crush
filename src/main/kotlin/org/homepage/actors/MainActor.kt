package org.homepage.actors

import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import org.homepage.SpaceGlobalUserId

sealed class MainActorMsg {
    class ConnectionOpened(val userId: SpaceGlobalUserId, val session: WebSocketServerSession) : MainActorMsg()
    class ConnectionClosed(val userId: SpaceGlobalUserId, val session: WebSocketServerSession) : MainActorMsg()
    class Modification(val mod: ValentineMod) : MainActorMsg()
}

fun CoroutineScope.mainActor() = actor<MainActorMsg> {
    val counterUpdateActor = counterUpdateActor()
    val userToConnections = mutableMapOf<SpaceGlobalUserId, List<ConnectionActor>>()

    for (msg in channel) {
        when (msg) {
            is MainActorMsg.ConnectionOpened -> {
                userToConnections[msg.userId] =
                    (userToConnections[msg.userId] ?: emptyList()) + connectionActor(msg)
            }
            is MainActorMsg.Modification -> {
                userToConnections[msg.mod.userId]?.forEach {
                    it.inbox.trySend(msg.mod)
                }

                counterUpdateActor.trySend(msg.mod)
            }

            is MainActorMsg.ConnectionClosed -> {
                userToConnections[msg.userId] = userToConnections[msg.userId]?.filter { it.session != msg.session } ?: emptyList()
            }
        }
    }
}