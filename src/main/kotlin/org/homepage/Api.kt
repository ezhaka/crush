package org.homepage

import kotlinx.serialization.Serializable

@Serializable
class IncomingValentine(
    val id: Long,
    val message: String,
    val type: Int,
    val read: Boolean
)

@Serializable
sealed class WebsocketMessage {
    @Serializable
    class ValentineListInit(val data: List<IncomingValentine>) : WebsocketMessage()
    @Serializable
    class ValentineReceived(val valentine: IncomingValentine) : WebsocketMessage()
    @Serializable
    class ValentineRead(val valentineId: Long) : WebsocketMessage()
    @Serializable
    class Pong() : WebsocketMessage()
}
@Serializable
class Profile(
    val id: String,
    val firstName: String,
    val lastName: String
)

@Serializable
class ProfileListResponse(
    val data: List<Profile>
)