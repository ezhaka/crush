package org.homepage.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.homepage.db.AppInstallationTable
import org.homepage.db.IncomingValentineTable
import org.homepage.setUiExtensions
import org.homepage.spaceHttpClient
import org.homepage.valentineTypes
import org.jetbrains.exposed.sql.*
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

val serverUrl = "https://jetbrains.team"
val sukhonosenkoUserId = "1tRthh1xbcbA"

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

        launch {
            collectStats()
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

private fun collectStats() {
    log.info("Collecting stats")

    val totalNumberOfValentines = transaction {
        IncomingValentineTable
            .select { defaultValentineOp() }
            .count()
    }

    log.info("STATS: total number of valentines ${totalNumberOfValentines}")

    val totalNumberOfReadValentines = transaction {
        IncomingValentineTable
            .select { defaultValentineOp() and IncomingValentineTable.read }
            .count()
    }

    log.info("STATS: total number of read valentines ${totalNumberOfReadValentines}")

    val valentineRecipientsCount = transaction {
        IncomingValentineTable
            .slice(IncomingValentineTable.receiver)
            .select { defaultValentineOp() }
            .withDistinct(true)
            .count()
    }

    log.info("STATS: total number of people who received a valentine ${valentineRecipientsCount}")

    val top6Recipients = transaction {
        IncomingValentineTable
            .slice(IncomingValentineTable.receiver, IncomingValentineTable.id.count())
            .select { defaultValentineOp() }
            .groupBy(IncomingValentineTable.receiver)
            .orderBy(IncomingValentineTable.id.count(), SortOrder.DESC)
            .limit(6)
            .associate { it[IncomingValentineTable.receiver] to it[IncomingValentineTable.id.count()] }
    }

    log.info("STATS: top 6 recipients ${top6Recipients}")

    val typesDistribution = transaction {
        IncomingValentineTable
            .slice(IncomingValentineTable.cardType, IncomingValentineTable.id.count())
            .select { defaultValentineOp() }
            .groupBy(IncomingValentineTable.cardType)
            .associate { valentineTypes[it[IncomingValentineTable.cardType]] to it[IncomingValentineTable.id.count()] }
    }

    log.info("STATS: types distribution ${typesDistribution}")
}

private fun SqlExpressionBuilder.defaultValentineOp(): Op<Boolean> {
    return (IncomingValentineTable.serverUrl eq serverUrl) and
            not((IncomingValentineTable.receiver eq sukhonosenkoUserId) and (IncomingValentineTable.cardType eq 1))
}