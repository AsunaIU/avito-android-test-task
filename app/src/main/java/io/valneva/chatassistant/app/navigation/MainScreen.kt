package io.valneva.chatassistant.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ChatBubbleOutline
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.valneva.chatassistant.R
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
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    var chatsSearchActionTrigger by remember { mutableIntStateOf(0) }

    val destinations = remember {
        listOf(
            MainDestination(
                route = AppRoute.Chats.route,
                titleRes = R.string.nav_chats,
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.main_drawer_header),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                destinations.forEach { destination ->
                    NavigationDrawerItem(
                        label = {
                            Text(text = stringResource(id = destination.titleRes))
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        selected = destination.route == currentRoute,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
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
                    actions = {
                        if (currentRoute == AppRoute.Chats.route) {
                            IconButton(
                                onClick = {
                                    chatsSearchActionTrigger++
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(id = R.string.search_chats),
                                )
                            }
                        }
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
                            searchActionTrigger = chatsSearchActionTrigger,
                        )
                    }

                    composable(route = AppRoute.Profile.route) {
                        ProfileScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle,
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
