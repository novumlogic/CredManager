package com.novumlogic.credmanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novumlogic.credmanager.R
import com.novumlogic.credmanager.viewmodel.CredManagerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    credManagerViewModel: CredManagerViewModel,
    modifier: Modifier = Modifier,
    onResetScroll: () -> Unit = {},
    onSignIn: (String, String) -> Unit
) {
    var usernameTextState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var passwordTextState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    // Used to decide if the keyboard should be shown
    var usernameTextFieldFocusState by remember { mutableStateOf(false) }
    var passwordTextFieldFocusState by remember { mutableStateOf(false) }

    var isCredentialsSaved by remember { credManagerViewModel.isCredentialsSaved }

    val uiState = credManagerViewModel.uiState.collectAsState().value

    Surface(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(contentDescription = stringResource(id = R.string.app_icon))

            if (uiState.signedInPasswordCredential == null) {

                if (!isCredentialsSaved) {
                    UserInputText(
                        credManagerViewModel,
                        textFieldValue = usernameTextState,
                        onTextChanged = {
                            credManagerViewModel.isUsernameValid(it.text)
                            usernameTextState = it
                        },
                        keyboardShown = usernameTextFieldFocusState,
                        onTextFieldFocused = { focused ->
                            if (focused) onResetScroll.invoke()

                            usernameTextFieldFocusState = focused
                        },
                        label = stringResource(id = R.string.username),
                        inputType = InputType.USERNAME
                    )

                    UserInputText(
                        credManagerViewModel,
                        textFieldValue = passwordTextState,
                        onTextChanged = {
                            credManagerViewModel.isPasswordValid(it.text)
                            passwordTextState = it
                        },
                        keyboardShown = passwordTextFieldFocusState,
                        onTextFieldFocused = { focused ->
                            if (focused) onResetScroll.invoke()
                            passwordTextFieldFocusState = focused
                        },
                        label = stringResource(id = R.string.password),
                        inputType = InputType.PASSWORD
                    )
                } else {
                    TextMessage(
                        message = "Welcome Back to Cred Manager, Hit \'Sign In to\' Get Started!",
                    )
                }

                val buttonText =
                    if (isCredentialsSaved) stringResource(id = R.string.sign_in) else stringResource(
                        id = R.string.save_creds
                    )
                RequestAccessButton(
                    buttonText,
                    modifier = Modifier.fillMaxWidth(),
                    onMessageSent = {
                        if (usernameTextState.text.isEmpty() || passwordTextState.text.isEmpty()) return@RequestAccessButton
                        onSignIn.invoke(usernameTextState.text, passwordTextState.text)
                    }
                )

                if (uiState.errorMessage.isNotEmpty()) TextFieldError(uiState.errorMessage)
            } else {
                TextFieldLogout("Signed in successfully! Click to Logout") {
                    usernameTextState = TextFieldValue()
                    passwordTextState = TextFieldValue()
                    credManagerViewModel.simulateLogOut()
                }
            }
        }
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalFoundationApi
@Composable
fun UserInputText(
    credManagerViewModel: CredManagerViewModel,
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    label: String,
    inputType: InputType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = label
                keyboardShownProperty = keyboardShown
            },
        horizontalArrangement = Arrangement.End
    ) {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    Column {
                        var lastFocusState by remember { mutableStateOf(false) }
                        val uiState by credManagerViewModel.uiState.collectAsState()

                        OutlinedTextField(
                            value = textFieldValue,
                            visualTransformation = if (inputType == InputType.PASSWORD) PasswordVisualTransformation() else VisualTransformation.None,
                            label = { UserInputTextLabel(label) },
                            onValueChange = {
                                onTextChanged(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    if (lastFocusState != state.isFocused) {
                                        onTextFieldFocused(state.isFocused)
                                    }
                                    lastFocusState = state.isFocused
                                },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = keyboardType,
                                imeAction = if (inputType == InputType.PASSWORD) ImeAction.Done else ImeAction.Next
                            ),
                            singleLine = true,
                            maxLines = 1,
                            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        )

                        if (!uiState.isUsernameValid && inputType == InputType.USERNAME) TextFieldError(
                            stringResource(id = R.string.username_blank_err)
                        )
                        if (!uiState.isPasswordValid && inputType == InputType.PASSWORD) TextFieldError(
                            stringResource(id = R.string.password_blank_err)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInputTextLabel(label: String) {
    Text(text = label)
}

@Composable
fun TextFieldError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error)
    )
}

@Composable
fun TextFieldLogout(message: String, onLogout: () -> Unit) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.clickable {
            onLogout.invoke()
        }
    )
}

@Composable
fun TextMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp
        ),
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun RequestAccessButton(title: String, modifier: Modifier, onMessageSent: () -> Unit) {
    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    val buttonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = Color.Transparent,
        disabledContentColor = disabledContentColor
    )

    // Request Access button
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
    ) {
        TextButton(
            onClick = onMessageSent,
            colors = buttonColors,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

enum class InputType {
    USERNAME, PASSWORD
}