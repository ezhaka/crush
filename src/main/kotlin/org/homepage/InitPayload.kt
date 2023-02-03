package org.homepage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.server.config.*
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

        val publicUrl = config.tryGetString("crush.publicUrl")?.trimEnd('/') ?: "http://localhost:3001"

        setBody("{\"contextIdentifier\":\"global\",\"extensions\":[" +
                "{\"className\":\"TopLevelPageUiExtensionIn\",\"displayName\":\"Secret Crush\",\"uniqueCode\":\"crush\",\"iframeUrl\": \"$publicUrl/\"}" +
//                ",{\"className\":\"SidebarHeaderIconExtensionIn\",\"iframeUrl\": \"$publicUrl/notification.html\"}" +
                "]}")
    }

    // TODO: write to logs
    println("status: ${res.status}, response: ${res.bodyAsText()}")

}
