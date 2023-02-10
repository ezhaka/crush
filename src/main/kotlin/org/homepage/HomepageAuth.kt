package org.homepage

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.parseAndVerifyAccessToken

suspend fun PipelineContext<Unit, ApplicationCall>.runAuthorized(
    handler: suspend (SpaceTokenInfo) -> Unit
) {
    getSpaceTokenInfo(spaceHttpClient)?.let { spaceTokenInfo ->
        try {
            handler(spaceTokenInfo)
        } catch (e: Exception) {
            log.error("Exception while processing the \"${call.request.uri}\" call from homepage UI", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    } ?: run {
        call.respond(HttpStatusCode.Unauthorized, "Invalid access token")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getSpaceTokenInfo(ktorClient: HttpClient): SpaceTokenInfo? {
    return getSpaceTokenInfo(context.request, ktorClient)
}

suspend fun getSpaceTokenInfo(request: ApplicationRequest, ktorClient: HttpClient): SpaceTokenInfo? {
    return (request.parseAuthorizationHeader() as? HttpAuthHeader.Single)?.blob
        ?.let { getSpaceTokenInfo(it, ktorClient) }
}

suspend fun getSpaceTokenInfo(spaceUserToken: String, ktorClient: HttpClient): SpaceTokenInfo? {
    return Space.parseAndVerifyAccessToken(spaceUserToken, ktorClient, AppInstanceStorage)?.let {
        SpaceTokenInfo(
            spaceAppInstance = it.spaceAppInstance,
            spaceUserId = it.spaceUserId,
            spaceAccessToken = it.spaceAccessToken,
        )
    }
}

data class SpaceGlobalUserId(
    val spaceServerUrl: String,
    val spaceUserId: String,
)

data class SpaceTokenInfo(
    val spaceAppInstance: SpaceAppInstance,
    val spaceUserId: String,
    val spaceAccessToken: String,
)

fun SpaceTokenInfo.globalUserId() = SpaceGlobalUserId(
    spaceServerUrl = spaceAppInstance.spaceServer.serverUrl,
    spaceUserId = spaceUserId,
)

/**
 * Space API methods called with this client will be called on behalf of the application (using
 * Client Credentials Flow).
 */
fun SpaceTokenInfo.appSpaceClient() =
    SpaceClient(spaceHttpClient, spaceAppInstance, SpaceAuth.ClientCredentials())


private val log: Logger = LoggerFactory.getLogger("Routing.kt")
