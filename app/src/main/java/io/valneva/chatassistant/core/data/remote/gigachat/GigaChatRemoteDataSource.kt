package io.valneva.chatassistant.core.data.remote.gigachat

import io.valneva.chatassistant.BuildConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
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
        return executeWithAccessToken { accessToken ->
            requestReply(
                accessToken = accessToken,
                messages = messages,
            )
        }
    }

    suspend fun generateImage(prompt: String): GigaChatGeneratedImage {
        return executeWithAccessToken { accessToken ->
            requestImage(
                accessToken = accessToken,
                prompt = prompt,
            )
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
        } catch (_: SocketTimeoutException) {
            throw GigaChatException.Timeout
        } catch (error: HttpException) {
            throw error.toGigaChatException()
        } catch (error: IOException) {
            throw GigaChatException.Network
        }
    }

    private suspend fun requestImage(
        accessToken: String,
        prompt: String,
    ): GigaChatGeneratedImage {
        return try {
            val response = gigaChatApi.getChatCompletion(
                authorization = "Bearer $accessToken",
                request = GigaChatChatRequest(
                    model = BuildConfig.GIGACHAT_MODEL,
                    messages = listOf(
                        GigaChatMessageDto(
                            role = ROLE_USER,
                            content = prompt,
                        ),
                    ),
                    functionCall = FUNCTION_CALL_AUTO,
                    functions = listOf(GigaChatFunctionDto(name = FUNCTION_TEXT_TO_IMAGE)),
                ),
            )

            val content = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            val fileId = extractImageFileId(content) ?: throw GigaChatException.ImageNotFound
            val bytes = gigaChatApi.downloadFile(
                authorization = "Bearer $accessToken",
                fileId = fileId,
            ).bytes()

            if (bytes.isEmpty()) throw GigaChatException.ImageNotFound

            GigaChatGeneratedImage(
                fileId = fileId,
                bytes = bytes,
                message = IMG_TAG_REGEX.replace(content, "").trim(),
                totalTokens = response.usage?.totalTokens,
            )
        } catch (_: SocketTimeoutException) {
            throw GigaChatException.Timeout
        } catch (error: HttpException) {
            throw error.toGigaChatException()
        } catch (error: IOException) {
            throw GigaChatException.Network
        }
    }

    private suspend fun <T> executeWithAccessToken(
        block: suspend (String) -> T,
    ): T {
        return try {
            block(getAccessToken(forceRefresh = false))
        } catch (error: HttpException) {
            if (error.code() == 401) {
                block(getAccessToken(forceRefresh = true))
            } else {
                throw error.toGigaChatException()
            }
        } catch (_: SocketTimeoutException) {
            throw GigaChatException.Timeout
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
            } catch (_: SocketTimeoutException) {
                throw GigaChatException.Timeout
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

    private fun extractImageFileId(content: String): String? {
        return IMG_TAG_REGEX.find(content)?.groupValues?.getOrNull(1)
    }

    private companion object {
        const val TOKEN_EXPIRATION_BUFFER_SECONDS = 60L
        const val FUNCTION_CALL_AUTO = "auto"
        const val FUNCTION_TEXT_TO_IMAGE = "text2image"
        const val ROLE_USER = "user"
        val IMG_TAG_REGEX = Regex("""<img\s+src="([^"]+)"[^>]*/?>""")
    }
}

sealed class GigaChatException(
    override val message: String,
) : IllegalStateException(message) {
    data object Network : GigaChatException("Проверьте подключение к интернету")
    data object Timeout : GigaChatException("Сервер отвечает слишком долго. Попробуйте ещё раз")
    data object Auth : GigaChatException("Ошибка авторизации GigaChat")
    data object RateLimit : GigaChatException("Слишком много запросов. Попробуйте позже")
    data object EmptyResponse : GigaChatException("GigaChat вернул пустой ответ")
    data object ImageNotFound : GigaChatException("Не удалось получить изображение")
    data object Unknown : GigaChatException("Не удалось получить ответ")
}
