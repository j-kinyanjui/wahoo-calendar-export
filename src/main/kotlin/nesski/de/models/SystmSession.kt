package nesski.de.models

import io.ktor.http.URLBuilder
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import java.util.concurrent.TimeUnit

data class SystmSession(val token: String, val expiresAt: Long) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }

    companion object {
        suspend fun getSession(call: ApplicationCall): SystmSession? {
            val systmSession: SystmSession? = call.sessions.get()
            if (systmSession == null) {
                val redirectUrl =
                    URLBuilder("http://wahoo.calendar:8484/systm-login").run {
                        parameters.append("redirectUrl", call.request.uri)
                        build()
                    }
                call.respondRedirect(redirectUrl)
                return null
            }
            if (systmSession.isExpired()) {
                val redirectUrl =
                    URLBuilder("http://wahoo.calendar:8484/systm-login").run {
                        parameters.append("redirectUrl", call.request.uri)
                        parameters.append("reason", "expired")
                        build()
                    }
                call.respondRedirect(redirectUrl)
                return null
            }
            return systmSession
        }

        /**
         * Create a new SystmSession with token and expiration
         * @param token The JWT token from Systm
         * @param validityDays How many days the token is valid (default 30)
         */
        fun create(token: String, validityDays: Long = 30): SystmSession {
            val expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(validityDays)
            return SystmSession(token, expiresAt)
        }
    }
}
