package org.homepage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import kotlinx.serialization.Serializable
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.ApplicationHomepageUiExtensionIn
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier

suspend fun ProcessingScope.setUiExtensions() {
    val client = clientWithClientCredentials()
    val httpClient = client.ktorClient

    val res = httpClient.patch("${client.server.apiBaseUrl}/applications/ui-extensions") {
        contentType(ContentType.Application.Json)
        bearerAuth(client.token().accessToken)

        setBody("{\"contextIdentifier\":\"global\",\"extensions\":[" +
                "{\"className\":\"TopLevelPageUiExtensionIn\",\"displayName\":\"Secret Crush\",\"uniqueCode\":\"crush\",\"iframeUrl\": \"http://localhost:3001/\"}," +
                "{\"className\":\"SidebarHeaderIconExtensionIn\",\"iframeUrl\": \"http://localhost:3001/\"}" +
                "]}")
    }

    // TODO: write to logs
    println("status: ${res.status}, response: ${res.bodyAsText()}")

}
