package nesski.de

import io.ktor.server.application.Application
import nesski.de.modules.wahooSystmWeb

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    wahooSystmWeb()
}
