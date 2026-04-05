package io.valneva.chatassistant.core.data.remote.gigachat

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface GigaChatApi {

    @Headers("Accept: application/json")
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GigaChatChatRequest,
    ): GigaChatChatResponse

    @Headers("Accept: application/jpg")
    @GET("files/{fileId}/content")
    suspend fun downloadFile(
        @Header("Authorization") authorization: String,
        @Path("fileId") fileId: String,
    ): ResponseBody
}
