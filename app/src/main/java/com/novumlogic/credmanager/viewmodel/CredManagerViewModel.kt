package com.novumlogic.credmanager.viewmodel

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novumlogic.credmanager.data.CredManagerRepository
import com.novumlogic.credmanager.data.CredManagerResult
import com.novumlogic.credmanager.data.CredMangerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredManagerViewModel @Inject constructor(private val credManagerRepository: CredManagerRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(CredMangerUiState())
    val uiState: StateFlow<CredMangerUiState> = _uiState.asStateFlow()

    var isCredentialsSaved = mutableStateOf(false)

    private lateinit var credentialManager: CredentialManager

    /**
     * Validates username field
     *
     * @param username - username value entered by user
     */
    fun isUsernameValid(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                isUsernameValid = username.isNotEmpty()
            )
        }
    }

    /**
     * Validates password field
     *
     * @param psword - password value entered by user
     */
    fun isPasswordValid(psword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                isPasswordValid = psword.isNotEmpty()
            )
        }
    }

    fun createCredentialManager(activity: Activity) {
        credentialManager = CredentialManager.create(activity)
    }

    fun signInUser(
        activity: Activity,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            val result = credManagerRepository.signIn(
                activity,
                credentialManager,
                username,
                password,
                viewModelScope
            )

            // handle the response from saving credentials
            handleResult(result)
        }
    }

    private fun handleResult(result: CredManagerResult) {
        when {
            result.credentials != null -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        signedInPasswordCredential = result.credentials,
                        errorMessage = ""
                    )
                }
            }

            result.error != null -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        signedInPasswordCredential = null,
                        errorMessage = result.error.errorMessage
                    )
                }
            }
        }
    }

    fun simulateLogOut() {
        _uiState.update {
            it.copy(signedInPasswordCredential = null, errorMessage = "")
        }
    }

    /**
     * In-memory flag to check if user creds are saved or not
     *
     */
    suspend fun isCredentialsSaved(activity: Activity) {
        val credentials = credManagerRepository.isCredentialsSaved(activity, credentialManager, viewModelScope)
        isCredentialsSaved.value = credentials != null

        if (credentials == null) { // need to login
            _uiState.update {
                it.copy(signedInPasswordCredential = null, errorMessage = "")
            }
        } else { // if got creds, sign in the user
            _uiState.update {
                it.copy(signedInPasswordCredential = credentials)
            }
        }
    }
}