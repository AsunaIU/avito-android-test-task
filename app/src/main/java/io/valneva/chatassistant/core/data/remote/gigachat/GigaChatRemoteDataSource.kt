package io.valneva.chatassistant.core.data.remote.gigachat

import io.valneva.chatassistant.BuildConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GigaChatRemoteDataSource @Inject constructor(
    private val tokenApi: GigaChatTokenApi,
    private val gigaChatApi: GigaChatApi,
) {

    private val tokenMutex = Mutex()

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedExpiresAtSeconds: Long? = null

    suspend fun generateReply(messages: List<GigaChatMessageDto>): GigaChatReply {
        return try {
            requestReply(
                accessToken = getAccessToken(forceRefresh = false),
                messages = messages,
            )
        } catch (error: HttpException) {
            if (error.code() == 401) {
                requestReply(
                    accessToken = getAccessToken(forceRefresh = true),
                    messages = messages,
                )
            } else {
                throw error.toGigaChatException()
            }
        } catch (error: IOException) {
            throw GigaChatException.Network
        }
    }

    private suspend fun requestReply(
        accessToken: String,
        messages: List<GigaChatMessageDto>,
    ): GigaChatReply {
        return try {
            val response = gigaChatApi.getChatCompletion(
                authorization = "Bearer $accessToken",
                request = GigaChatChatRequest(
                    model = BuildConfig.GIGACHAT_MODEL,
                    messages = messages,
                ),
            )

            val content = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            if (content.isBlank()) throw GigaChatException.EmptyResponse

            GigaChatReply(
                content = content,
                totalTokens = response.usage?.totalTokens,
            )
        } catch (error: HttpException) {
            throw error.toGigaChatException()
        } catch (error: IOException) {
            throw GigaChatException.Network
        }
    }

    private suspend fun getAccessToken(forceRefresh: Boolean): String {
        return tokenMutex.withLock {
            if (!forceRefresh && hasValidToken()) {
                return@withLock cachedAccessToken.orEmpty()
            }

            try {
                val tokenResponse = tokenApi.getToken(
                    authorization = "Basic ${BuildConfig.GIGACHAT_AUTH_KEY}",
                    requestId = UUID.randomUUID().toString(),
                    scope = BuildConfig.GIGACHAT_SCOPE,
                )

                cachedAccessToken = tokenResponse.accessToken
                cachedExpiresAtSeconds = tokenResponse.expiresAt

                tokenResponse.accessToken
            } catch (error: HttpException) {
                cachedAccessToken = null
                cachedExpiresAtSeconds = null
                throw error.toGigaChatException()
            } catch (error: IOException) {
                throw GigaChatException.Network
            }
        }
    }

    private fun hasValidToken(): Boolean {
        val accessToken = cachedAccessToken
        val expiresAtSeconds = cachedExpiresAtSeconds
        if (accessToken.isNullOrBlank() || expiresAtSeconds == null) return false

        val nowSeconds = System.currentTimeMillis() / 1000
        return expiresAtSeconds > nowSeconds + TOKEN_EXPIRATION_BUFFER_SECONDS
    }

    private fun HttpException.toGigaChatException(): GigaChatException {
        return when (code()) {
            401 -> GigaChatException.Auth
            429 -> GigaChatException.RateLimit
            else -> GigaChatException.Unknown
        }
    }

    private companion object {
        const val TOKEN_EXPIRATION_BUFFER_SECONDS = 60L
    }
}

sealed class GigaChatException(
    override val message: String,
) : IllegalStateException(message) {
    data object Network : GigaChatException("Проверьте подключение к интернету")
    data object Auth : GigaChatException("Ошибка авторизации GigaChat")
    data object RateLimit : GigaChatException("Слишком много запросов. Попробуйте позже")
    data object EmptyResponse : GigaChatException("GigaChat вернул пустой ответ")
    data object Unknown : GigaChatException("Не удалось получить ответ")
}
