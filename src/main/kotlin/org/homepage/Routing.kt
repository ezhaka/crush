package org.homepage

import AppHasPermissionsService
import Routes
import SendMessageService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.homepage.db.IncomingValentine
import org.homepage.services.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import space.jetbrains.api.runtime.BatchInfo
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.InitPayload

fun Application.configureRouting() {
    install(Locations)
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/api/space") {
            Space.processPayload(ktorRequestAdapter(call), spaceHttpClient, AppInstanceStorage) { payload ->
                when (payload) {
                    is InitPayload -> {
                        setUiExtensions()
                        SpaceHttpResponse.RespondWithOk
                    }
                    else -> {
                        call.respond(HttpStatusCode.OK)
                        SpaceHttpResponse.RespondWithOk
                    }
                }
            }
        }

        static("/space-iframe") {
            staticBasePackage = "space-iframe"
            resources(".")
            defaultResource("index.html")
        }
        static("/") {
            staticBasePackage = "space-iframe"
            resources(".")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK)
        }

        get("/healthz") {
            call.respond(HttpStatusCode.OK)
        }

        get<Routes.GetChannels> { params ->
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, GetChannelsService(spaceTokenInfo).getChannels(params.query))
            }
        }

        get<Routes.GetProfiles> { params ->
            runAuthorized { spaceTokenInfo ->
                val profiles = spaceTokenInfo.appSpaceClient().teamDirectory.profiles.getAllProfiles(
                    query = params.query,
                    batchInfo = BatchInfo(offset = null, batchSize = 50)
                )
                    .data
                    .map { Profile(it.id, it.name.firstName, it.name.lastName) }
                call.respond(HttpStatusCode.OK, ProfileListResponse(profiles))
            }
        }

        post<Routes.SendMessage> { params ->
            runAuthorized { spaceTokenInfo ->
                SendMessageService(spaceTokenInfo).sendMessage(params.channelId, params.messageText)
                call.respond(HttpStatusCode.OK)
            }
        }

        post<Routes.SendValentine> { params ->
            runAuthorized { spaceTokenInfo ->
                // TODO
                println("SendValentine spaceUserId = ${spaceTokenInfo.spaceUserId}")

                transaction {
                    IncomingValentine.insert {
                        it[this.receiver] = params.receiverId
                        it[this.message] = params.messageText
                    }
                }

                call.respond(HttpStatusCode.OK)
            }
        }

        get<Routes.GetIncomingValentines> { params ->
            runAuthorized { spaceTokenInfo ->
                // TODO
                println("GetIncomingValentines spaceUserId = ${spaceTokenInfo.spaceUserId}")

                val valentines = transaction {
                    IncomingValentine
                        .select {
                        IncomingValentine.receiver eq spaceTokenInfo.spaceUserId
                    }
                        .map { IncomingValentine(it[IncomingValentine.id].value, it[IncomingValentine.message]) }
                }

                call.respond(HttpStatusCode.OK, IncomingValentineListResponse(valentines))
            }
        }

        get<Routes.AppHasPermissions> {
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, AppHasPermissionsService(spaceTokenInfo).appHasPermissions())
            }
        }
    }
}

private fun ktorRequestAdapter(call: ApplicationCall): RequestAdapter {
    return object : RequestAdapter {
        override suspend fun receiveText() =
            call.receiveText()

        override fun getHeader(headerName: String) =
            call.request.header(headerName)

        override suspend fun respond(httpStatusCode: Int, body: String) =
            call.respond(HttpStatusCode.fromValue(httpStatusCode), body)
    }
}
