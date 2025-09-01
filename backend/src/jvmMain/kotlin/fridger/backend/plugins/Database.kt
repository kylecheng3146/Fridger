package fridger.backend.plugins

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/fridger"
    val user = System.getenv("DB_USER") ?: "postgres"
    val password = System.getenv("DB_PASSWORD") ?: "postgres"

    log.info("Running Flyway migrations on $url ...")
    Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .load()
        .migrate()
    log.info("Flyway migrations complete")

    log.info("Connecting database via Exposed")
    Database.connect(url = url, user = user, password = password)
    log.info("Database connected")
}

