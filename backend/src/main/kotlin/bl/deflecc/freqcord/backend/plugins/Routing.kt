package bl.deflecc.freqcord.backend.plugins

import bl.deflecc.freqcord.backend.routes.messageRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            messageRouting()
        }
    }
}
