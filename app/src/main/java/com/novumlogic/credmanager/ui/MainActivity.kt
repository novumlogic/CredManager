package com.novumlogic.credmanager.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novumlogic.credmanager.theme.CredManagerTheme
import com.novumlogic.credmanager.viewmodel.CredManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val credManagerViewModel: CredManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CredManagerTheme {
                HomeScreen(credManagerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    credManagerViewModel: CredManagerViewModel
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val coroutineScope = rememberCoroutineScope()
    val activity: Activity = LocalContext.current as Activity

    credManagerViewModel.createCredentialManager(activity)

    LaunchedEffect(key1 = null, block = {
        launch {
            credManagerViewModel.isCredentialsSaved(activity)
        }
    })

    Column {
        CredManagerAppBar(
            onNavIconPressed = {},
            title = {
                ToolbarTitleText()
            }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(height = 100.dp))

                UserInput(
                    credManagerViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onSignIn = { username, password ->
                        credManagerViewModel.signInUser(activity, username, password)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CredManagerTheme {
        HomeScreen(
            viewModel(modelClass = CredManagerViewModel::class.java))
    }
}