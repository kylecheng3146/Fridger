package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.security.userId
import fridger.backend.services.RecipeFeedbackService
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.SubmitRecipeFeedbackRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.recipeFeedbackRoutes(service: RecipeFeedbackService) {
    authenticate("access") {
        post(ApiPaths.RECIPE_FEEDBACK) {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val body = call.receive<SubmitRecipeFeedbackRequest>()
            if (body.recipeId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing recipeId"))
                return@post
            }

            service.submitFeedback(principal.userId(), body.copy(recipeId = body.recipeId.trim()))
            call.respond(ApiResponse.ok(Unit))
        }
    }
}
