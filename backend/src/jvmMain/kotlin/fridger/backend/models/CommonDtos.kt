package fridger.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(val message: String)

@Serializable
data class HealthDto(val status: String)
