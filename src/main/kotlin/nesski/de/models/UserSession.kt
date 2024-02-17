package nesski.de.models

import io.ktor.http.URLBuilder
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

data class UserSession(val state: String, val token: String) {
    companion object {
        suspend fun getSession(call: ApplicationCall): UserSession? {
            val userSession: UserSession? = call.sessions.get()
            // if there is no session, redirect to login
            if (userSession == null) {
                val redirectUrl =
                    URLBuilder("http://wahoo.calendar:8484/login").run {
                        parameters.append("redirectUrl", call.request.uri)
                        build()
                    }
                call.respondRedirect(redirectUrl)
                return null
            }
            return userSession
        }
    }
}
