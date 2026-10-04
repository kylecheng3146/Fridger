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
import fridger.backend.routes.fridgeInventoryRoutes
import fridger.backend.routes.recipeFeedbackRoutes
import fridger.backend.routes.recipeGenerationRoutes
import fridger.backend.routes.shoppingListRoutes
import fridger.backend.routes.shoppingSyncRoutes
import fridger.backend.services.DefaultRecipeFeedbackService
import fridger.backend.services.GroqInventoryRecipeGenerator
import fridger.backend.services.HealthDashboardService
import fridger.backend.services.ShoppingListService
import fridger.backend.services.ShoppingSyncService
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.ApplicationStopping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun Application.configureRouting() {
    val config = appConfig()
    val fridgeItemRepository = FridgeItemRepository()
    val dashboardService = HealthDashboardService(fridgeItemRepository)
    val snapshotScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    environment.monitor.subscribe(ApplicationStopping) { snapshotScope.cancel() }
    snapshotScope.launch {
        while (isActive) {
            // Snapshot replacement locks the account row, including across backend replicas.
            runCatching { dashboardService.captureDailySnapshots() }
                .onFailure { log.warn("Health dashboard daily snapshot failed", it) }
            delay(5 * 60 * 1000L)
        }
    }
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
        fridgeInventoryRoutes(fridgeItemRepository)
        recipeGenerationRoutes(recipeGenerator)
        recipeFeedbackRoutes(recipeFeedbackService)
        shoppingListRoutes(shoppingListService)
        shoppingSyncRoutes(shoppingSyncService)
    }
}
