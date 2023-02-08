package org.homepage

import AppHasPermissionsService
import Routes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.locations.put
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.homepage.db.IncomingValentineTable
import org.homepage.services.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.BatchInfo
import space.jetbrains.api.runtime.NotFoundException
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier
import space.jetbrains.api.runtime.types.InitPayload
import space.jetbrains.api.runtime.types.ProfileIdentifier

fun Application.configureRouting() {
    val log = LoggerFactory.getLogger("Routing.kt")

    install(Locations)
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/api/space") {
            Space.processPayload(ktorRequestAdapter(call), spaceHttpClient, AppInstanceStorage) { payload ->
                when (payload) {
                    is InitPayload -> {
                        clientWithClientCredentials().applications.authorizations.authorizedRights.requestRights(
                            application = ApplicationIdentifier.Me,
                            contextIdentifier = GlobalPermissionContextIdentifier,
                            rightCodes = listOf("Profile.View")
                        )

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

        static("/") {
            staticBasePackage = "static"
            resources(".")
            defaultResource("index.html")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK)
        }

        get("/healthz") {
            call.respond(HttpStatusCode.OK)
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

        post("/homepage/send-valentine") {
            val params = call.receive<Routes.SendValentineBody>()

            runAuthorized { spaceTokenInfo ->
                val spaceAppInstance = spaceTokenInfo.spaceAppInstance

                // check that profile exists
                try {
                    spaceTokenInfo.appSpaceClient().teamDirectory.profiles.getProfile(ProfileIdentifier.Id(params.receiverId))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound)
                    return@runAuthorized
                }

                val row = transaction {
                    IncomingValentineTable.insert {
                        it[this.serverUrl] = spaceAppInstance.spaceServer.serverUrl
                        it[this.receiver] = params.receiverId
                        it[this.message] = params.messageText
                    }.resultedValues!!.first()
                }

                sendModificationEvent(spaceAppInstance, row[IncomingValentineTable.id].value, params.receiverId)

                call.respond(HttpStatusCode.OK)
            }
        }

        get<Routes.GetIncomingValentines> { params ->
            runAuthorized { spaceTokenInfo ->
                val valentines = transaction {
                    IncomingValentineTable
                        .select { matchUser(spaceTokenInfo) }
                        .map {
                            IncomingValentine(
                                it[IncomingValentineTable.id].value,
                                it[IncomingValentineTable.message],
                                it[IncomingValentineTable.type]
                            )
                        }
                        .sortedByDescending { it.id }
                }

                call.respond(HttpStatusCode.OK, IncomingValentineListResponse(valentines))
            }
        }

        put<Routes.ReadValentine> { params ->
            runAuthorized { spaceTokenInfo ->
                val spaceAppInstance = spaceTokenInfo.spaceAppInstance

                val rowsUpdated = transaction {
                    IncomingValentineTable.update(where = {
                        matchUser(spaceTokenInfo) and (IncomingValentineTable.id eq params.valentineId)
                    }) {
                        it[read] = true
                        it[readCounterUpdated] = false
                    }
                }

                if (rowsUpdated == 1) {
                    sendModificationEvent(spaceAppInstance, params.valentineId, spaceTokenInfo.spaceUserId)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        get<Routes.AppHasPermissions> {
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, AppHasPermissionsService(spaceTokenInfo).appHasPermissions())
            }
        }
    }
}

private fun SqlExpressionBuilder.matchUser(spaceTokenInfo: SpaceTokenInfo) =
    (IncomingValentineTable.serverUrl eq spaceTokenInfo.spaceAppInstance.spaceServer.serverUrl) and
            (IncomingValentineTable.receiver eq spaceTokenInfo.spaceUserId)

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
