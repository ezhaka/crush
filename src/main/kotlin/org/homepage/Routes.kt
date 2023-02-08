import io.ktor.server.locations.*
import kotlinx.serialization.Serializable

object Routes {
    @Location("/homepage/get-profiles")
    class GetProfiles(val query: String)

    @Serializable
    class SendValentineBody(val receiverId: String, val messageText: String, val cardType: Int)

    @Location("/homepage/read-valentine")
    class ReadValentine(val valentineId: Long)

    @Location("/homepage/get-incoming-valentines")
    object GetIncomingValentines

    @Location("/homepage/app-has-permissions")
    object AppHasPermissions
}
