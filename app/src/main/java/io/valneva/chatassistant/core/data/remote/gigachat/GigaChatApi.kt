package io.valneva.chatassistant.core.data.remote.gigachat

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GigaChatApi {

    @Headers("Accept: application/json")
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GigaChatChatRequest,
    ): GigaChatChatResponse
}
