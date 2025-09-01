package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.config.appConfig
import fridger.backend.models.MessageDto
import fridger.backend.repositories.RefreshTokenRepository
import fridger.backend.repositories.UserRepository
import fridger.backend.security.GoogleTokenValidator
import fridger.backend.services.AuthService
import fridger.backend.services.AuthTokens
import fridger.shared.models.ApiResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequest(val idToken: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

fun Route.authRoutes(app: Application) {
    val cfg = app.appConfig()
    val userRepo = UserRepository()
    val refreshRepo = RefreshTokenRepository()
    val googleValidator = GoogleTokenValidator.instance(app)
    val authService = AuthService(cfg, userRepo, refreshRepo, googleValidator)

    route(ApiPaths.AUTH) {
        post(ApiPaths.AUTH_GOOGLE) {
            val body = call.receive<GoogleSignInRequest>()
            val tokens: AuthTokens = authService.signInWithGoogle(body.idToken)
            call.respond(ApiResponse.ok(tokens))
        }
        post(ApiPaths.AUTH_REFRESH) {
            val body = call.receive<RefreshRequest>()
            val tokens = authService.refreshAccessToken(body.refreshToken)
            call.respond(ApiResponse.ok(tokens))
        }
        post(ApiPaths.AUTH_LOGOUT) {
            val body = call.receive<RefreshRequest>()
            authService.logout(body.refreshToken)
            call.respond(ApiResponse.ok(MessageDto("Logged out")))
        }
    }
}
