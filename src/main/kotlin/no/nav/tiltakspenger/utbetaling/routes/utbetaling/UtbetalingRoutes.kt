package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post("$utbetalingPath/{id}") {
        LOG.info("Mottatt request på $utbetalingPath/id")
        val utbetalingDTOUt = call.receive<UtbetalingDTOUt>()
        val id = call.parameters["id"]?.let {
            LOG.info { "iden var ikke null! Og er $it" }
        }

        utbetalingService.sendUtbetalingTilIverksett(utbetalingDTOUt)

        call.respond(status = HttpStatusCode.OK, "Kallet fungerte")
    }
}
