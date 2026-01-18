package fridger.backend.plugins

import fridger.backend.config.appConfig
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val cfg = appConfig()
    val url = cfg.dbUrl
    val user = cfg.dbUser
    val password = cfg.dbPassword

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
