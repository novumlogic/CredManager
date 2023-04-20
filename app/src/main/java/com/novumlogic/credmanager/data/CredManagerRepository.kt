package com.novumlogic.credmanager.data

import android.app.Activity
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CredManagerRepository {
    /**
     * Create new credentials with credential manager
     *
     * @param activity - the activity context needed to show the credentials flow dialog to the user
     * @param username - username
     * @param password - password
     * @param coroutineScope - The coroutine scope to run the dialog on
     */
    suspend fun signIn(
        activity: Activity,
        credentialManager: CredentialManager,
        username: String,
        password: String,
        coroutineScope: CoroutineScope
    ): CredManagerResult {
        return if (isCredentialsSaved(activity, credentialManager, coroutineScope) != null) {
            getCredential(activity, credentialManager)
        } else {
            saveCredential(activity, credentialManager, username, password)
        }
    }

    /**
     * Fetches saved credentials
     *
     * @return [PasswordCredential] that contains user creds
     */
    private suspend fun getCredential(
        activity: Activity,
        credentialManager: CredentialManager
    ): CredManagerResult {
        try {
            val getCredRequest = GetCredentialRequest(listOf(GetPasswordOption()))

            // Shows the user a dialog allowing them to pick a saved credential
            val credentialResponse = credentialManager.getCredential(
                request = getCredRequest,
                activity = activity,
            )
            val credential = credentialResponse.credential as? PasswordCredential

            return CredManagerResult(credentials = credential)
        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, "User cancelled the request", e)
            return CredManagerResult(error = Error("User cancelled the request"))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "Credentials not found", e)
            return CredManagerResult(error = Error("Credentials not found"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error fetching the credentials", e)
            return CredManagerResult(error = Error("Error fetching the credentials"))
        }
    }

    /**
     * Ask the user for permission to add the credentials to their store and saves the credentials to memory
     *
     * @param username - The username for the user
     * @param password - password for the user
     */
    private suspend fun saveCredential(
        activity: Activity,
        credentialManager: CredentialManager,
        username: String,
        password: String
    ): CredManagerResult {
        try {
            val response = credentialManager.createCredential(
                request = CreatePasswordRequest(username, password),
                activity = activity,
            )

            Log.e(TAG, "Credentials successfully added: ${response.data}")
            return CredManagerResult(credentials = PasswordCredential(username, password))
        } catch (e: CreateCredentialCancellationException) {
            Log.e(TAG, "User cancelled the save flow")
            return CredManagerResult(error = Error("User cancelled the save flow"))
        } catch (e: CreateCredentialException) {
            Log.e(TAG, "Credentials cannot be saved", e)
            return CredManagerResult(error = Error("Credentials cannot be saved"))
        }
    }


    suspend fun isCredentialsSaved(
        activity: Activity,
        credentialManager: CredentialManager,
        coroutineScope: CoroutineScope
    ): PasswordCredential? = suspendCoroutine { continuation ->
        coroutineScope.launch {
            try {
                val getCredRequest = GetCredentialRequest(listOf(GetPasswordOption()))

                val credentialResponse = credentialManager.getCredential(
                    request = getCredRequest,
                    activity = activity,
                )
                val credential = credentialResponse.credential as? PasswordCredential
                continuation.resume(credential)
            } catch (e: GetCredentialCancellationException) {
                continuation.resume(null)
                Log.d(TAG,  e.toString())
            } catch (e: NoCredentialException) {
                continuation.resume(null)
                Log.d(TAG,  e.toString())
            } catch (e: GetCredentialException) {
                continuation.resume(null)
                Log.d(TAG,  e.toString())
            }
        }
    }

    companion object {
        const val TAG: String = "CredManager"
    }

}