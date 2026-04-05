package io.valneva.chatassistant.core.data.remote.gigachat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GigaChatTokenResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "expires_at")
    val expiresAt: Long,
)

@JsonClass(generateAdapter = true)
data class GigaChatChatRequest(
    val model: String,
    val messages: List<GigaChatMessageDto>,
    val stream: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class GigaChatMessageDto(
    val role: String,
    val content: String,
)

@JsonClass(generateAdapter = true)
data class GigaChatChatResponse(
    val choices: List<GigaChatChoiceDto> = emptyList(),
    val usage: GigaChatUsageDto? = null,
)

@JsonClass(generateAdapter = true)
data class GigaChatChoiceDto(
    val message: GigaChatMessageDto,
)

@JsonClass(generateAdapter = true)
data class GigaChatUsageDto(
    @Json(name = "prompt_tokens")
    val promptTokens: Int = 0,
    @Json(name = "completion_tokens")
    val completionTokens: Int = 0,
    @Json(name = "total_tokens")
    val totalTokens: Int = 0,
)

data class GigaChatReply(
    val content: String,
    val totalTokens: Int?,
)
