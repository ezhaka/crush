package org.homepage

import Routes
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.CacheControl
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.put
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.SendChannel
import org.homepage.actors.MainActorMsg
import org.homepage.actors.ValentineMod
import org.homepage.actors.myUserId
import org.homepage.actors.trySendWithLogging
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
import space.jetbrains.api.runtime.types.*
import java.time.ZonedDateTime

val valentineTypes = listOf("piggy", "pill", "chocolate", "cloud", "match")

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
                            rightCodes = listOf(PermissionIdentifier.ViewMemberProfiles)
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

        resource(remotePath = "/", resource = "index.html", resourcePackage = "static")

        static("/static") {
            install(CachingHeaders) {
                options { _, _ ->
                    CachingOptions(
                        CacheControl.MaxAge(maxAgeSeconds = 604800),
                        ZonedDateTime.now().plusDays(30)
                    )
                }
            }

            staticBasePackage = "static"
            resources(".")
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

        get("/api/avatar/{userId}") {
            val userId = call.parameters["userId"] ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            runAuthorized { spaceTokenInfo ->
                val client = spaceTokenInfo.appSpaceClient()
                val smallAvatarTid = client.teamDirectory.profiles.getProfile(ProfileIdentifier.Id(userId)) {
                    this.smallAvatar()
                }.smallAvatar

                val response = client.ktorClient.get("${client.server.serverUrl}/d/$smallAvatarTid") {
                    this.expectSuccess = false
                    bearerAuth(client.token().accessToken)
                }

                call.respondBytesWriter(response.contentType(), response.status, response.contentLength()) {
                    val byteWriteChannel = this
                    response.bodyAsChannel().copyTo(byteWriteChannel)
                }
            }
        }

        post("/api/send-valentine") {
            val params = call.receive<Routes.SendValentineBody>()

            runAuthorized { spaceTokenInfo ->
                val spaceAppInstance = spaceTokenInfo.spaceAppInstance

                if (params.cardType < 0 || params.cardType >= valentineTypes.size) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Card type should be in range 0..${valentineTypes.size - 1}, got ${params.cardType}"
                    )
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

                mainActor.trySendWithLogging(
                    MainActorMsg.Modification(
                        ValentineMod.Created(
                            spaceAppInstance,
                            SpaceGlobalUserId(spaceAppInstance.spaceServer.serverUrl, params.receiverId),
                            IncomingValentine(id, params.messageText, params.cardType, read = false)
                        )
                    ),
                    log,
                    "mainActor inbox"
                )

                log.info("Someone has sent a valentine of type '${valentineTypes[params.cardType]}' to someone")

                call.respond(HttpStatusCode.OK)
            }
        }

        webSocket("/api/websocket") {
            val rawToken = call.parameters["token"]
            if (rawToken == null) {
                call.respond(HttpStatusCode.Unauthorized, "No token param is passed")
                return@webSocket
            }

            val token = getSpaceTokenInfo(rawToken, spaceHttpClient)
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token is not found or invalid")
                return@webSocket
            }

            try {
                mainActor.trySendWithLogging(
                    MainActorMsg.ConnectionOpened(token.globalUserId(), this),
                    log,
                    "mainActor inbox"
                )

                for (frame in incoming) {
                    if (token.spaceUserId == myUserId) {
                        log.info("Anton Sukhonosenko's ping received")
                    }
                    sendSerialized<WebsocketMessage>(WebsocketMessage.Pong())
                }
            } finally {
                if (token.spaceUserId == myUserId) {
                    log.info("Anton Sukhonosenko's websocket connection finished")
                }

                mainActor.trySendWithLogging(
                    MainActorMsg.ConnectionClosed(token.globalUserId(), this),
                    log,
                    "mainActor inbox"
                )
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
                    mainActor.trySendWithLogging(
                        MainActorMsg.Modification(
                            ValentineMod.Read(
                                spaceAppInstance,
                                params.valentineId,
                                spaceTokenInfo.globalUserId(),
                            )
                        ),
                        log,
                        "mainActor inbox"
                    )

                    log.info("Someone has read their valentine")

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
