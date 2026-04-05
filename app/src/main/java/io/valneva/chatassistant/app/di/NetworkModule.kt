package io.valneva.chatassistant.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.valneva.chatassistant.BuildConfig
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatApi
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatTokenApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            )
        }

        return builder.build()
    }

    @Provides
    @Singleton
    @Named("gigachat_oauth")
    fun provideGigaChatOAuthRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.GIGACHAT_OAUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("gigachat_api")
    fun provideGigaChatApiRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.GIGACHAT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideGigaChatTokenApi(
        @Named("gigachat_oauth") retrofit: Retrofit,
    ): GigaChatTokenApi = retrofit.create(GigaChatTokenApi::class.java)

    @Provides
    @Singleton
    fun provideGigaChatApi(
        @Named("gigachat_api") retrofit: Retrofit,
    ): GigaChatApi = retrofit.create(GigaChatApi::class.java)
}
