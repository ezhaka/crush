package org.homepage

import Routes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.put
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.channels.SendChannel
import org.homepage.actors.MainActorMsg
import org.homepage.actors.ValentineMod
import org.homepage.db.IncomingValentineTable
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

fun Application.configureRouting(mainActor: SendChannel<MainActorMsg>) {
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

        post("/api/send-valentine") {
            val params = call.receive<Routes.SendValentineBody>()

            runAuthorized { spaceTokenInfo ->
                val spaceAppInstance = spaceTokenInfo.spaceAppInstance

                if (params.cardType < 0 || params.cardType > 4) {
                    call.respond(HttpStatusCode.BadRequest, "Card type should be in range 0..4, got ${params.cardType}")
                    return@runAuthorized
                }

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
                        it[this.cardType] = params.cardType
                    }.resultedValues!!.first()
                }

                val id = row[IncomingValentineTable.id].value

                mainActor.trySend(
                    MainActorMsg.Modification(
                        ValentineMod.Created(
                            spaceAppInstance,
                            SpaceGlobalUserId(spaceAppInstance.spaceServer.serverUrl, params.receiverId),
                            IncomingValentine(id, params.messageText, params.cardType, read = false)
                        )
                    )
                )

                call.respond(HttpStatusCode.OK)
            }
        }

        webSocket("/api/websocket") {
            val rawToken = call.parameters["token"]
            if (rawToken == null) {
                call.respond(HttpStatusCode.Unauthorized, "No token param is passed")
                return@webSocket
            }

            val token = getSpaceTokenInfo(rawToken)
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token is not found or invalid")
                return@webSocket
            }

            // TODO: log every trySend
            mainActor.trySend(MainActorMsg.ConnectionOpened(token.globalUserId(), this))

            for (frame in incoming) {}

            mainActor.trySend(MainActorMsg.ConnectionClosed(token.globalUserId(), this))
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
                    mainActor.trySend(
                        MainActorMsg.Modification(
                            ValentineMod.Read(
                                spaceAppInstance,
                                params.valentineId,
                                spaceTokenInfo.globalUserId(),
                            )
                        )
                    )

                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

fun SqlExpressionBuilder.matchUser(spaceTokenInfo: SpaceTokenInfo) =
    (IncomingValentineTable.serverUrl eq spaceTokenInfo.spaceAppInstance.spaceServer.serverUrl) and
            (IncomingValentineTable.receiver eq spaceTokenInfo.spaceUserId)

fun SqlExpressionBuilder.matchUser(userId: SpaceGlobalUserId) =
    (IncomingValentineTable.serverUrl eq userId.spaceServerUrl) and
        (IncomingValentineTable.receiver eq userId.spaceUserId)

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
