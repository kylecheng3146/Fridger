package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.models.ShoppingListUpsertRequest
import fridger.backend.security.userId
import fridger.backend.services.ShoppingListService
import fridger.shared.models.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.shoppingListRoutes(service: ShoppingListService) {
    authenticate("access") {
        get(ApiPaths.SHOPPING_LISTS) {
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val lists = service.fetchLists(userId)
            call.respond(ApiResponse.ok(lists))
        }

        get("${ApiPaths.SHOPPING_LISTS}/{listId}") {
            val listId = call.parameters["listId"]?.trim().orEmpty()
            if (listId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing listId"))
                return@get
            }
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val items = service.fetchItems(userId, listId)
            call.respond(ApiResponse.ok(items))
        }

        post(ApiPaths.SHOPPING_LISTS) {
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val body = call.receive<ShoppingListUpsertRequest>()
            if (body.id.isBlank() || body.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing id or name"))
                return@post
            }
            val response = service.upsert(userId, body.id, body.name, body.date)
            call.respond(ApiResponse.ok(response))
        }

        put("${ApiPaths.SHOPPING_LISTS}/{listId}") {
            val listId = call.parameters["listId"]?.trim().orEmpty()
            if (listId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing listId"))
                return@put
            }
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val body = call.receive<ShoppingListUpsertRequest>()
            val name = body.name.ifBlank { "未命名清單" }
            val response = service.upsert(userId, listId, name, body.date)
            call.respond(ApiResponse.ok(response))
        }

        delete("${ApiPaths.SHOPPING_LISTS}/{listId}") {
            val listId = call.parameters["listId"]?.trim().orEmpty()
            if (listId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing listId"))
                return@delete
            }
            val principal = call.principal<JWTPrincipal>()
            val userId =
                principal?.userId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            service.deleteList(userId, listId)
            call.respond(ApiResponse.ok(Unit))
        }
    }
}
