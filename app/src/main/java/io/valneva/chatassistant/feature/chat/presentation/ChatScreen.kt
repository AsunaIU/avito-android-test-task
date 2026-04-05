package io.valneva.chatassistant.feature.chat.presentation

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.valneva.chatassistant.R
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.core.data.local.model.MessageRole
import io.valneva.chatassistant.core.data.local.model.MessageStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    LaunchedEffect(messages.lastOrNull()?.id, messages.lastOrNull()?.status) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }
                },
                title = {
                    Text(
                        text = uiState.title.ifBlank { stringResource(id = R.string.chat_placeholder_title) },
                    )
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
            if (uiState.isMissingChat) {
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
                            text = stringResource(id = R.string.chat_not_found_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(id = R.string.chat_not_found_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                ) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.chat_empty_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            items(
                                items = messages,
                                key = { message -> message.id },
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    onRetryClick = viewModel::onRetryAssistantMessage,
                                    onShareClick = { text ->
                                        shareText(
                                            text = text,
                                            chooserTitle = context.resources.getString(R.string.share_response),
                                            context = context,
                                        )
                                    },
                                )
                            }
                        }
                    }

                    MessageComposer(
                        value = uiState.input,
                        isSending = uiState.isSending,
                        onValueChange = viewModel::onInputChanged,
                        onSendClick = viewModel::onSendClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageComposer(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = {
                Text(text = stringResource(id = R.string.message_input_label))
            },
            minLines = 1,
            maxLines = 5,
            enabled = !isSending,
            shape = MaterialTheme.shapes.large,
        )

        FilledIconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank() && !isSending,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(id = R.string.send_message),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: MessageEntity,
    onRetryClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
) {
    val isUserMessage = message.role == MessageRole.USER
    val shareableText = message.text.ifBlank { message.errorMessage.orEmpty() }
    val canShare = !isUserMessage && shareableText.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = when {
                isUserMessage -> MaterialTheme.colorScheme.primaryContainer
                message.status == MessageStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .then(
                    if (canShare) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { onShareClick(shareableText) },
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isUserMessage) {
                        stringResource(id = R.string.you_label)
                    } else {
                        stringResource(id = R.string.assistant_label)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                when {
                    message.status == MessageStatus.GENERATING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(2.dp),
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = stringResource(id = R.string.assistant_generating),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    message.text.isNotBlank() -> {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (message.status == MessageStatus.ERROR) {
                    Text(
                        text = message.errorMessage ?: stringResource(id = R.string.assistant_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }

                if (!isUserMessage && (canShare || message.status == MessageStatus.ERROR)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (canShare) {
                            TextButton(onClick = { onShareClick(shareableText) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                )
                                Text(
                                    text = stringResource(id = R.string.share_response),
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }

                        if (message.status == MessageStatus.ERROR) {
                            TextButton(onClick = { onRetryClick(message.id) }) {
                                Text(text = stringResource(id = R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareText(
    text: String,
    chooserTitle: String,
    context: android.content.Context,
) {
    if (text.isBlank()) return

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
