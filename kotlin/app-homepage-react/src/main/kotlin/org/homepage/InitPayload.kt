package org.homepage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
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

//        setBody(object {
//            val contextIdentifier = "global"
//            val extensions = listOf(
//                object {
//                    val className = "TopLevelPageUiExtensionIn"
//                    val displayName = "kek"
//                    val uniqueCode = "kek"
//                    val iframeUrl = "http://localhost:8080/space-iframe"
//                }
//            )
//        })

        setBody("{\"contextIdentifier\":\"global\",\"extensions\":[{\"className\":\"TopLevelPageUiExtensionIn\",\"displayName\":\"kek\",\"uniqueCode\":\"kek\",\"iframeUrl\": \"http://localhost:3001/\"}]}")
    }

    println(
     "status: ${res.status}"
    )

    println(
     "response: ${res.bodyAsText()}"
    )

//    client.applications.setUiExtensions(
//        contextIdentifier = GlobalPermissionContextIdentifier,
//        extensions = listOf(
////            TopLevelPageUiExtensionIn
//            ApplicationHomepageUiExtensionIn(iframeUrl = "/space-iframe")
//        )
//    )
}
