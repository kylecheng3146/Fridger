package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.security.userId
import fridger.backend.services.InventoryRecipeGenerator
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.GenerateRecipeRequest
import fridger.shared.recipe.SaveRecipeRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.recipeGenerationRoutes(generator: InventoryRecipeGenerator) {
    post(ApiPaths.RECIPE_GENERATION) {
        val body = call.receive<GenerateRecipeRequest>()
        val normalizedIngredients =
            body.ingredients
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

        if (normalizedIngredients.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing ingredients"))
            return@post
        }

        val recipe = generator.generate(normalizedIngredients, body.styles)
        call.respond(ApiResponse.ok(recipe))
    }

    authenticate("access") {
        post(ApiPaths.RECIPE_SAVE) {
            val principal =
                call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val body = call.receive<SaveRecipeRequest>()
            if (body.recipeId.isBlank() || body.title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing recipe payload"))
                return@post
            }

            println("🍽️ Save recipe request accepted for user=${principal.userId()}, recipeId=${body.recipeId}")
            call.respond(ApiResponse.ok(Unit))
        }
    }
}
