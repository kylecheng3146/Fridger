package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.services.InventoryRecipeGenerator
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.GenerateRecipeRequest
import io.ktor.http.HttpStatusCode
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

        val recipe = generator.generate(normalizedIngredients)
        call.respond(ApiResponse.ok(recipe))
    }
}
