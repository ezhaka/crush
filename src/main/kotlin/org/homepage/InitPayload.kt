package org.homepage

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.ProcessingScope

private val log = LoggerFactory.getLogger("InitPayload.kt")

suspend fun ProcessingScope.setUiExtensions() {
    val client = clientWithClientCredentials()

    val publicUrl = config.tryGetString("crush.publicUrl")?.trimEnd('/') ?: "http://localhost:3001"

    val res = addUiExtensions(
        client,
        "{\"contextIdentifier\":\"global\",\"extensions\":[" +
                "{\"className\":\"TopLevelOverlayUiExtensionIn\",\"iframeUrl\": \"$publicUrl\"}" +
                "]}",
        expectSuccess = false
    )

    if (res.status.isSuccess()) {
        return
    }

    log.warn(
        "It seems current space instance doesn't support TopLevelOverlayUiExtensionIn (status: ${res.status}, response: ${res.bodyAsText()}), " +
            "falling back to TopLevelPageUiExtensionIn"
    )

    addUiExtensions(
        client,
        "{\"contextIdentifier\":\"global\",\"extensions\":[" +
                "{\"className\":\"TopLevelPageUiExtensionIn\",\"displayName\":\"Secret Crush\",\"uniqueCode\":\"crush\",\"iframeUrl\": \"$publicUrl/\"}" +
                "]}",
        true
    )
}

private suspend fun addUiExtensions(
    client: SpaceClient,
    body: String,
    expectSuccess: Boolean
): HttpResponse {
    val httpClient = client.ktorClient

    return httpClient.patch("${client.server.apiBaseUrl}/applications/ui-extensions") {
        this.expectSuccess = expectSuccess

        contentType(ContentType.Application.Json)
        bearerAuth(client.token().accessToken)
        setBody(body)
    }
}
