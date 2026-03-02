package nesski.de.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import nesski.de.models.SystmSession

fun Application.configureSystmAuthentication() {
    install(Sessions) {
        cookie<SystmSession>("systm_session")
    }
}
