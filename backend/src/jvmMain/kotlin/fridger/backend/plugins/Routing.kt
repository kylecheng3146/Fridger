package fridger.backend.plugins

import fridger.backend.config.ApiPaths
import fridger.backend.config.appConfig
import fridger.backend.models.HealthDto
import fridger.backend.models.MessageDto
import fridger.backend.repositories.FridgeItemRepository
import fridger.backend.repositories.RecipeFeedbackRepository
import fridger.backend.repositories.ShoppingListRepository
import fridger.backend.routes.authRoutes
import fridger.backend.routes.healthDashboardRoutes
import fridger.backend.routes.recipeFeedbackRoutes
import fridger.backend.routes.recipeGenerationRoutes
import fridger.backend.routes.shoppingSyncRoutes
import fridger.backend.routes.shoppingListRoutes
import fridger.backend.services.HealthDashboardService
import fridger.backend.services.GroqInventoryRecipeGenerator
import fridger.backend.services.ShoppingListService
import fridger.backend.services.ShoppingSyncService
import fridger.backend.services.DefaultRecipeFeedbackService
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val config = appConfig()
    val dashboardService = HealthDashboardService(FridgeItemRepository())
    val recipeGenerator = GroqInventoryRecipeGenerator(config)
    val recipeFeedbackService = DefaultRecipeFeedbackService(RecipeFeedbackRepository())
    val shoppingRepository = ShoppingListRepository()
    val shoppingSyncService = ShoppingSyncService(shoppingRepository)
    val shoppingListService = ShoppingListService(shoppingRepository)
    routing {
        get(ApiPaths.ROOT) {
            call.respond(ApiResponse.ok(MessageDto("Fridger backend running")))
        }
        get(ApiPaths.HEALTH) {
            call.respond(ApiResponse.ok(HealthDto("OK")))
        }
        authRoutes(this@configureRouting)
        healthDashboardRoutes(dashboardService)
        recipeGenerationRoutes(recipeGenerator)
        recipeFeedbackRoutes(recipeFeedbackService)
        shoppingListRoutes(shoppingListService)
        shoppingSyncRoutes(shoppingSyncService)
    }
}
