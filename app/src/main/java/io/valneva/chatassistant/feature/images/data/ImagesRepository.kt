package io.valneva.chatassistant.feature.images.data

import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatGeneratedImage
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatRemoteDataSource
import javax.inject.Inject

class ImagesRepository @Inject constructor(
    private val gigaChatRemoteDataSource: GigaChatRemoteDataSource,
) {

    suspend fun generateImage(prompt: String): Result<GigaChatGeneratedImage> = runCatching {
        gigaChatRemoteDataSource.generateImage(prompt = prompt)
    }
}
