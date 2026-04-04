package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.models.ShoppingSyncRequest
import fridger.backend.models.ShoppingSyncResponse
import fridger.backend.security.userId
import fridger.backend.services.ShoppingSyncService
import fridger.shared.models.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.shoppingSyncRoutes(service: ShoppingSyncService) {
    authenticate("access") {
        post("${ApiPaths.SHOPPING_LISTS}/{listId}/sync") {
            val listId = call.parameters["listId"]?.trim().orEmpty()
            if (listId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing listId"))
                return@post
            }
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val body = call.receive<ShoppingSyncRequest>()
            val results = service.sync(userId, listId, body.actions)
            call.respond(ApiResponse.ok(ShoppingSyncResponse(results)))
        }
    }
}
