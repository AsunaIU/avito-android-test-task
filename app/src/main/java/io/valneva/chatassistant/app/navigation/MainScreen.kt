package io.valneva.chatassistant.app.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.valneva.chatassistant.R
import io.valneva.chatassistant.feature.chats.presentation.ChatListUiEffect
import io.valneva.chatassistant.feature.chats.presentation.ChatListViewModel
import io.valneva.chatassistant.feature.chats.presentation.ChatsScreen
import io.valneva.chatassistant.feature.images.presentation.ImagesScreen
import io.valneva.chatassistant.feature.profile.presentation.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onOpenChat: (String) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    chatListViewModel: ChatListViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val chatListUiState by chatListViewModel.uiState.collectAsStateWithLifecycle()

    val destinations = remember {
        listOf(
            MainDestination(
                route = AppRoute.Chats.route,
                titleRes = R.string.nav_chats,
                drawerLabelRes = R.string.drawer_home,
                icon = Icons.Rounded.ChatBubbleOutline,
            ),
            MainDestination(
                route = AppRoute.Profile.route,
                titleRes = R.string.nav_profile,
                icon = Icons.Rounded.AccountCircle,
            ),
            MainDestination(
                route = AppRoute.Images.route,
                titleRes = R.string.nav_images,
                icon = Icons.Rounded.Image,
            ),
        )
    }

    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = destinations.firstOrNull { it.route == currentRoute } ?: destinations.first()

    fun navigateToDrawerDestination(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun closeDrawer() {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    LaunchedEffect(chatListViewModel) {
        chatListViewModel.effects.collect { effect ->
            when (effect) {
                is ChatListUiEffect.OpenChat -> onOpenChat(effect.chatId)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = chatListUiState.searchQuery,
                        onValueChange = chatListViewModel::onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        label = {
                            Text(text = stringResource(id = R.string.drawer_search_hint))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            if (chatListUiState.searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        chatListViewModel.onSearchCleared()
                                        navigateToDrawerDestination(AppRoute.Chats.route)
                                        closeDrawer()
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(id = R.string.clear_search),
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                chatListViewModel.onSearchClick()
                                navigateToDrawerDestination(AppRoute.Chats.route)
                                closeDrawer()
                            },
                        ),
                    )

                    DrawerActionItem(
                        icon = Icons.Rounded.Edit,
                        label = stringResource(id = R.string.drawer_new_chat),
                        onClick = {
                            focusManager.clearFocus()
                            navigateToDrawerDestination(AppRoute.Chats.route)
                            chatListViewModel.onCreateChatClick()
                            closeDrawer()
                        },
                    )

                    destinations.forEach { destination ->
                        DrawerActionItem(
                            icon = destination.icon,
                            label = stringResource(id = destination.drawerLabelRes),
                            selected = destination.route == currentRoute,
                            onClick = {
                                focusManager.clearFocus()
                                navigateToDrawerDestination(destination.route)
                                closeDrawer()
                            },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = stringResource(id = R.string.main_menu),
                            )
                        }
                    },
                    title = {
                        Text(text = stringResource(id = currentDestination.titleRes))
                    },
                )
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Chats.route,
                ) {
                    composable(route = AppRoute.Chats.route) {
                        ChatsScreen(
                            onOpenChat = onOpenChat,
                            viewModel = chatListViewModel,
                        )
                    }

                    composable(route = AppRoute.Profile.route) {
                        ProfileScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle,
                            onSignedOut = onSignedOut,
                        )
                    }

                    composable(route = AppRoute.Images.route) {
                        ImagesScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
