package io.valneva.chatassistant.feature.chats.presentation

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import io.valneva.chatassistant.R
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.designsystem.component.ErrorSnackbar

@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit,
    searchActionTrigger: Int,
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val hasAuthenticatedUser = viewModel.hasAuthenticatedUser.collectAsStateWithLifecycle().value
    val chats = viewModel.chats.collectAsLazyPagingItems()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChatsUiEffect.OpenChat -> onOpenChat(effect.chatId)
            }
        }
    }

    LaunchedEffect(searchActionTrigger) {
        if (searchActionTrigger > 0) {
            focusManager.clearFocus()
            viewModel.onSearchClick()
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (hasAuthenticatedUser) {
                FloatingActionButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onCreateChatClick()
                    },
                ) {
                    if (uiState.isCreatingChat) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(12.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(id = R.string.create_chat),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                ErrorSnackbar(snackbarData = snackbarData)
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SearchSection(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onSearchClick = {
                        focusManager.clearFocus()
                        viewModel.onSearchClick()
                    },
                    onClearClick = {
                        focusManager.clearFocus()
                        viewModel.onSearchCleared()
                    },
                )

                when {
                    !hasAuthenticatedUser -> {
                        EmptyChatsState(
                            title = stringResource(id = R.string.chats_auth_required_title),
                            body = stringResource(id = R.string.chats_auth_required_body),
                        )
                    }

                    chats.loadState.refresh is LoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    chats.itemCount == 0 -> {
                        val isSearchActive = uiState.appliedQuery.isNotBlank()
                        EmptyChatsState(
                            title = stringResource(
                                id = if (isSearchActive) {
                                    R.string.chats_empty_search_title
                                } else {
                                    R.string.chats_empty_title
                                },
                            ),
                            body = stringResource(
                                id = if (isSearchActive) {
                                    R.string.chats_empty_search_body
                                } else {
                                    R.string.chats_empty_body
                                },
                            ),
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                count = chats.itemCount,
                                key = { index -> chats[index]?.id ?: index },
                            ) { index ->
                                val chat = chats[index] ?: return@items
                                ChatRow(
                                    chat = chat,
                                    onClick = { onOpenChat(chat.id) },
                                )
                            }

                            if (chats.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(text = stringResource(id = R.string.chats_search_label))
        },
        trailingIcon = {
            if (searchQuery.isNotBlank()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(id = R.string.clear_search),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun ChatRow(
    chat: ChatEntity,
    onClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            headlineContent = {
                Text(
                    text = chat.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    text = chat.lastMessagePreview ?: stringResource(id = R.string.chat_without_messages),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingContent = {
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        chat.updatedAt,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Composable
private fun EmptyChatsState(
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
