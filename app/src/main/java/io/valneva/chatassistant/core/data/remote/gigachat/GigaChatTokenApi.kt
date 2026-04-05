package io.valneva.chatassistant.core.data.remote.gigachat

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.Header
import retrofit2.http.POST

interface GigaChatTokenApi {

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("oauth")
    suspend fun getToken(
        @Header("Authorization") authorization: String,
        @Header("RqUID") requestId: String,
        @Field("scope") scope: String,
    ): GigaChatTokenResponse
}
