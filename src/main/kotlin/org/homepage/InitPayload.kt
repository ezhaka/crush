package org.homepage

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.ApplicationHomepageUiExtensionIn
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier
import space.jetbrains.api.runtime.types.TopLevelPageUiExtensionIn

suspend fun ProcessingScope.setUiExtensions() {
    val client = clientWithClientCredentials()

    setUiExtensions(client)
}

suspend fun setUiExtensions(client: SpaceClient) {
    val publicUrl = config.tryGetString("crush.publicUrl")?.trimEnd('/') ?: "http://localhost:3001"

    client.applications.setUiExtensions(
        contextIdentifier = GlobalPermissionContextIdentifier,
        extensions = listOf(
            TopLevelPageUiExtensionIn(
                displayName = "Secret Crush",
                description = null,
                uniqueCode = "secretcrush",
                iframeUrl = publicUrl
            )
        )
    )
}

