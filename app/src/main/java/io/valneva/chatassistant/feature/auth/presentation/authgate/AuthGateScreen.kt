package io.valneva.chatassistant.feature.auth.presentation.authgate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.valneva.chatassistant.R
import io.valneva.chatassistant.designsystem.theme.ChatAssistantTheme

@Composable
fun AuthGateScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToChats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthGateViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.isAuthorized) {
        when (uiState.isAuthorized) {
            true -> onNavigateToChats()
            false -> onNavigateToLogin()
            null -> Unit
        }
    }
    AuthGateContent(modifier = modifier)
}

@Composable
private fun AuthGateContent(
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(id = R.string.auth_gate_loading),
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun AuthGateContentPreview() {
    ChatAssistantTheme {
        AuthGateContent()
    }
}
