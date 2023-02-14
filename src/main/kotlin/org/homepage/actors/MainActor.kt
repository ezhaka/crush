package org.homepage.actors

import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.supervisorScope
import org.homepage.SpaceGlobalUserId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T> SendChannel<T>.trySendWithLogging(element: T, logger: Logger, title: String) {
    val result = trySend(element)
    if (result.isFailure) {
        logger.error("Unable to send value to channel $title", result.exceptionOrNull())
    }
}

private val log: Logger = LoggerFactory.getLogger("MainActor.kt")

sealed class MainActorMsg {
    class Modification(val mod: ValentineMod) : MainActorMsg()
}

fun CoroutineScope.mainActor() = actor<MainActorMsg>(capacity = 4096) {
    supervisorScope {
        val counterUpdateActor = counterUpdateActor()

        for (msg in channel) {
            when (msg) {
                is MainActorMsg.Modification -> {
                    counterUpdateActor.trySendWithLogging(msg.mod, log, "counterUpdater inbox")
                }
            }
        }
    }
}