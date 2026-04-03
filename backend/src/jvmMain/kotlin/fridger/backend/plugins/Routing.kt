package fridger.backend.plugins

import fridger.backend.config.ApiPaths
import fridger.backend.models.HealthDto
import fridger.backend.models.MessageDto
import fridger.backend.repositories.FridgeItemRepository
import fridger.backend.routes.authRoutes
import fridger.backend.routes.healthDashboardRoutes
import fridger.backend.repositories.ShoppingListRepository
import fridger.backend.routes.shoppingSyncRoutes
import fridger.backend.routes.shoppingListRoutes
import fridger.backend.services.HealthDashboardService
import fridger.backend.services.ShoppingListService
import fridger.backend.services.ShoppingSyncService
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val dashboardService = HealthDashboardService(FridgeItemRepository())
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
        shoppingListRoutes(shoppingListService)
        shoppingSyncRoutes(shoppingSyncService)
    }
}
