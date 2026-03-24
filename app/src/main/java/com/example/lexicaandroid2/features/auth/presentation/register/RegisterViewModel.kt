package com.example.lexicaandroid2.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
    val connectedEmail: String? = null
)

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        isRegistered = user != null,
                        connectedEmail = user?.email,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun registerWithEmailPassword() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password
        val confirmPassword = uiState.value.confirmPassword

        val validationError = validate(email, password, confirmPassword)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.registerWithEmail(email, password)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Échec d'inscription"
                        )
                    }
                }
        }
    }

    fun registerWithGoogleIdToken(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Token Google invalide") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.signInWithGoogleIdToken(idToken)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Échec de connexion Google"
                        )
                    }
                }
        }
    }

    fun onGoogleSignInCancelled() {
        _uiState.update { it.copy(isLoading = false, errorMessage = "Connexion Google annulée") }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    private fun validate(email: String, password: String, confirmPassword: String): String? {
        if (email.isBlank()) return "Email requis"
        if (!EMAIL_REGEX.matches(email)) return "Format email invalide"
        if (password.length < MIN_PASSWORD_LENGTH) return "Mot de passe trop court"
        if (password != confirmPassword) return "Les mots de passe ne correspondent pas"
        return null
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private const val MIN_PASSWORD_LENGTH = 6
    }
}

class RegisterViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
