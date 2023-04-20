package com.novumlogic.credmanager.data

import androidx.credentials.PasswordCredential

data class CredMangerUiState(
    var isUsernameValid: Boolean = true,
    var isPasswordValid: Boolean = true,
    var errorMessage: String = "",
    var signedInPasswordCredential: PasswordCredential? = null,
    var signInSuccess: Boolean = false
)