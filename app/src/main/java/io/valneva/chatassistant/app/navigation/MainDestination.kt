package io.valneva.chatassistant.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class MainDestination(
    val route: String,
    @StringRes
    val titleRes: Int,
    val icon: ImageVector,
)
