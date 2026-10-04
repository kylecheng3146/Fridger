package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.models.FridgeItemResponse
import fridger.backend.models.FridgeItemUpsertRequest
import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.security.userId
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
import io.ktor.server.routing.put
import java.time.ZoneId
import java.util.UUID
import kotlinx.datetime.LocalDate

fun Route.fridgeInventoryRoutes(repository: FridgeItemDataSource) {
    authenticate("access") {
        get(ApiPaths.FRIDGE_ITEMS) {
            val userId = call.principal<JWTPrincipal>()?.userId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val rawTimeZoneId = call.request.queryParameters["timeZoneId"]
            val zoneId = if (rawTimeZoneId == null) {
                repository.fetchTimeZoneForUser(userId)
            } else {
                parseTimeZoneId(rawTimeZoneId)
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid timeZoneId"))
            }
            repository.updateTimeZoneForUser(userId, zoneId)
            val items = repository.fetchItemsForUser(userId).map { item ->
                FridgeItemResponse(
                    id = item.id.toString(),
                    name = item.name,
                    addDate = LocalDate.parse((item.addedDate ?: item.createdAt.atZone(ZoneId.of(zoneId)).toLocalDate()).toString()),
                    expirationDate = item.expiryDate?.let { LocalDate.parse(it.toString()) },
                    category = item.inventoryCategory,
                )
            }
            call.respond(ApiResponse.ok(items))
        }

        put("${ApiPaths.FRIDGE_ITEMS}/{id}") {
            val userId = call.principal<JWTPrincipal>()?.userId()
                ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid item id"))
            val body = call.receive<FridgeItemUpsertRequest>()
            if (body.name.isBlank() || body.name.length > 200) {
                return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid item name"))
            }
            val zoneId = parseTimeZoneId(body.timeZoneId)
                ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid timeZoneId"))
            repository.updateTimeZoneForUser(userId, zoneId)
            val saved = repository.upsertItem(
                userId = userId,
                id = id,
                name = body.name.trim(),
                category = body.category,
                addDate = java.time.LocalDate.parse(body.addDate.toString()),
                expiryDate = java.time.LocalDate.parse(body.expirationDate.toString()),
                timeZoneId = zoneId,
            )
            if (!saved) {
                return@put call.respond(HttpStatusCode.Conflict, ApiResponse.fail<Unit>("Item belongs to another user"))
            }
            call.respond(ApiResponse.ok(Unit))
        }

        delete("${ApiPaths.FRIDGE_ITEMS}/{id}") {
            val userId = call.principal<JWTPrincipal>()?.userId()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid item id"))
            repository.deleteItem(userId, id)
            call.respond(ApiResponse.ok(Unit))
        }
    }
}

private fun parseTimeZoneId(raw: String?): String? =
    raw?.takeIf { it.isNotBlank() }?.let { value ->
        runCatching { ZoneId.of(value).id }.getOrNull()
    }
