package nesski.de

import io.ktor.server.application.Application
import nesski.de.plugins.configureAuthentication
import nesski.de.modules.wahooSystmWeb
import nesski.de.routes.configureSystmPlans

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    configureAuthentication()
    wahooSystmWeb()
    configureSystmPlans()
}
