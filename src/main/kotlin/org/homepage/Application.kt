package org.homepage

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.homepage.actors.mainActor
import org.homepage.db.initDbConnection
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.SpaceAuthFlow
import space.jetbrains.api.runtime.appInstallUrl
import space.jetbrains.api.runtime.ktorClientForSpace

@Suppress("unused")
fun Application.module() {

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    initDbConnection()

    val mainActor = mainActor()

    configureRouting(mainActor)

    println(
        "Public install url ${
            Space.appInstallUrl(
                spaceServerUrl = "https://spacerschoice.jetbrains.space/",
                name = "Secret Crush",
                appEndpoint = "https://secret-crush-test.labs.jb.gg/api/space",
                authFlows = setOf(
                    SpaceAuthFlow.ClientCredentials,
                    SpaceAuthFlow.AuthorizationCode(listOf("https://nowhere.domain"), false)
                ),
            )
        }")

    println(
        "Internal install url ${
            Space.appInstallUrl(
                spaceServerUrl = "http://localhost:8000/",
                name = "Secret Crush",
                appEndpoint = "http://localhost:3001/api/space",
                authFlows = setOf(
                    SpaceAuthFlow.ClientCredentials,
                    SpaceAuthFlow.AuthorizationCode(listOf("https://nowhere.domain"), false)
                ),
            )
        }")
}

val spaceHttpClient by lazy { ktorClientForSpace() }

val config: Config by lazy { ConfigFactory.load() }
