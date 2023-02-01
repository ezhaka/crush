package org.homepage

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import org.homepage.db.initDbConnection
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.SpaceAuthFlow
import space.jetbrains.api.runtime.appInstallUrl
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.ktorClientForSpace

@Suppress("unused")
fun Application.module() {
    initDbConnection()

    configureRouting()

    val url = Space.appInstallUrl(
        spaceServerUrl = "http://localhost:8000",
        name = "Fuck It",
        appEndpoint = "http://localhost:8080/api/space",
        authFlows = setOf(SpaceAuthFlow.ClientCredentials, SpaceAuthFlow.AuthorizationCode(listOf("https://nowhere.domain"), false)),
    )


    println("Fuck It url $url")
}

val spaceHttpClient = ktorClientForSpace()

val config: Config by lazy { ConfigFactory.load() }
