import io.ktor.server.locations.*
import kotlinx.serialization.Serializable

object Routes {
    @Location("/api/get-profiles")
    class GetProfiles(val query: String)

    @Serializable
    class SendValentineBody(val receiverId: String, val messageText: String, val cardType: Int)

    @Location("/api/read-valentine")
    class ReadValentine(val valentineId: Long)
}
