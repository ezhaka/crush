package org.homepage.actors

import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.homepage.SpaceGlobalUserId
import org.homepage.db.AppInstallationTable
import org.homepage.setUiExtensions
import org.homepage.spaceHttpClient
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient

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
        launch {
            log.info("Reinstalling UI extensions")
            try {
                val appInstances = transaction {
                    AppInstallationTable.selectAll()
                        .map {
                            SpaceAppInstance(
                                it[AppInstallationTable.clientId],
                                it[AppInstallationTable.clientSecret],
                                it[AppInstallationTable.serverUrl],
                            )
                        }
                }

                for (appInstance in appInstances) {
                    try {
                        val client = SpaceClient(spaceHttpClient, appInstance, SpaceAuth.ClientCredentials())
                        setUiExtensions(client)
                    } catch (e: Exception) {
                        log.error("Failed to reinstall UI extensions for ${appInstance.spaceServer}", e)
                    }
                }

            } catch (e: Exception) {
                log.error("Failed to reinstall UI extensions", e)
            }
        }

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