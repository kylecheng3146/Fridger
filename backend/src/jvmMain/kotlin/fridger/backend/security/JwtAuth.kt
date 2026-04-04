package fridger.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fridger.backend.config.JwtClaims
import fridger.backend.config.TokenTypes
import fridger.backend.config.appConfig
import fridger.shared.models.ApiResponse
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.util.UUID

fun Application.configureJwtAuth() {
    val cfg = appConfig()
    val algorithm = Algorithm.HMAC256(cfg.jwtSecret)

    install(Authentication) {
        jwt("access") {
            realm = cfg.jwtIssuer
            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(cfg.jwtIssuer)
                    .build()
            )
            validate { credential ->
                val type = credential.payload.getClaim(JwtClaims.TYPE).asString()
                if (type == TokenTypes.ACCESS) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(ApiResponse.fail<Unit>("Authentication failed"))
            }
        }
    }
}

fun JWTPrincipal.userId(): UUID {
    val raw = payload.getClaim(JwtClaims.USER_ID).asString()
    return UUID.fromString(raw)
}
