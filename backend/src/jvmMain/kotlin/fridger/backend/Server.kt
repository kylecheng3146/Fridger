package fridger.backend

import fridger.backend.config.ApiPaths
import fridger.backend.config.loadAppConfig
import fridger.backend.models.HealthDto
import fridger.shared.models.ApiResponse
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

private data class DbConfig(
    val url: String,
    val user: String,
    val password: String
)

fun main() {
    val cfg = loadAppConfig()
    val db = DbConfig(cfg.dbUrl, cfg.dbUser, cfg.dbPassword)
    migrate(db)
    initDatabase(db)

    embeddedServer(Netty, port = cfg.port) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = false
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(Authentication) {
            // Placeholder for future Google JWT auth configuration
        }
        routing {
            get(ApiPaths.HEALTH) {
                call.respond(ApiResponse.ok(HealthDto("OK")))
            }
        }
    }.start(wait = true)
}

private fun migrate(cfg: DbConfig) {
    Flyway.configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}

private fun initDatabase(cfg: DbConfig) {
    Database.connect(cfg.url, user = cfg.user, password = cfg.password)
    // Quick connection validation
    transaction {
        addLogger(StdOutSqlLogger)
        // no-op
    }
}
