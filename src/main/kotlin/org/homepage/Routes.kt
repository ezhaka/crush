import io.ktor.server.locations.*

object Routes {
    @Location("/homepage/get-profiles")
    class GetProfiles(val query: String)

    @Location("/homepage/send-valentine")
    class SendValentine(val receiverId: String, val messageText: String)

    @Location("/homepage/read-valentine")
    class ReadValentine(val valentineId: Long)

    @Location("/homepage/get-incoming-valentines")
    object GetIncomingValentines

    @Location("/homepage/app-has-permissions")
    object AppHasPermissions
}
