package com.novumlogic.credmanager.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.novumlogic.credmanager.R
import com.novumlogic.credmanager.theme.CredManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredManagerAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        actions = actions,
        title = title,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
//            AppIcon(
//                contentDescription = stringResource(id = R.string.app_icon),
//                modifier = Modifier
//                    .size(64.dp)
//                    .clickable(onClick = onNavIconPressed)
//                    .padding(16.dp)
//            )
        }
    )
}

@Composable
fun ToolbarTitleText() {
    Text(
        stringResource(id = R.string.app_title),
        style = MaterialTheme.typography.titleMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    CredManagerTheme {
        CredManagerAppBar(title = {
            ToolbarTitleText()
        })
    }
}