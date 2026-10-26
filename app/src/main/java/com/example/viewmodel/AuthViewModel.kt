package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.BloodDonorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserEntity) : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface RegisterState {
    data object Idle : RegisterState
    data object Loading : RegisterState
    data class Success(val user: UserEntity) : RegisterState
    data class Error(val message: String) : RegisterState
}

class AuthViewModel(
    private val repository: BloodDonorRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(repository.getLoggedInUserId())

    val currentUser: StateFlow<UserEntity?> = _currentUserId.value?.let { id ->
        repository.getUserById(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    } ?: MutableStateFlow(null)

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val savedUserId = repository.getLoggedInUserId()
        if (savedUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserByIdSync(savedUserId)
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                    _currentUserId.value = user.id
                } else {
                    repository.logout()
                    _authState.value = AuthState.Idle
                    _currentUserId.value = null
                }
            }
        }
    }

    fun login(phone: String, pass: String) {
        val trimmedPhone = phone.trim()
        val trimmedPass = pass.trim()

        if (trimmedPhone.isBlank()) {
            _authState.value = AuthState.Error("Please enter your phone number")
            return
        }
        if (trimmedPass.isBlank()) {
            _authState.value = AuthState.Error("Please enter your password")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginUser(trimmedPhone, trimmedPass)
            result.fold(
                onSuccess = { user ->
                    _currentUserId.value = user.id
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { err ->
                    _authState.value = AuthState.Error(err.message ?: "Login failed. Please check your credentials.")
                }
            )
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,
        age: String,
        bloodGroup: String,
        location: String,
        isAvailable: Boolean
    ) {
        val fName = firstName.trim()
        val lName = lastName.trim()
        val phone = phoneNumber.trim()
        val pass = password.trim()
        val cPass = confirmPassword.trim()
        val loc = location.trim()
        val parsedAge = age.trim().toIntOrNull()

        if (fName.isBlank()) {
            _registerState.value = RegisterState.Error("First name is required")
            return
        }
        if (lName.isBlank()) {
            _registerState.value = RegisterState.Error("Last name is required")
            return
        }
        if (phone.length < 8) {
            _registerState.value = RegisterState.Error("Please enter a valid phone number (at least 8 digits)")
            return
        }
        if (pass.length < 4) {
            _registerState.value = RegisterState.Error("Password must be at least 4 characters")
            return
        }
        if (pass != cPass) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }
        if (parsedAge == null || parsedAge < 18 || parsedAge > 65) {
            _registerState.value = RegisterState.Error("Eligible donor age must be between 18 and 65 years")
            return
        }
        if (bloodGroup.isBlank()) {
            _registerState.value = RegisterState.Error("Please select your blood group")
            return
        }
        if (loc.isBlank()) {
            _registerState.value = RegisterState.Error("Please enter your location/city")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val newUser = UserEntity(
                firstName = fName,
                lastName = lName,
                phoneNumber = phone,
                password = pass,
                age = parsedAge,
                bloodGroup = bloodGroup,
                location = loc,
                isAvailable = isAvailable,
                lastDonationDate = "",
                totalDonations = 0
            )

            val result = repository.registerUser(newUser)
            result.fold(
                onSuccess = { insertedId ->
                    val userWithId = newUser.copy(id = insertedId.toInt())
                    // Auto login after registration
                    repository.loginUser(phone, pass)
                    _currentUserId.value = userWithId.id
                    _authState.value = AuthState.Authenticated(userWithId)
                    _registerState.value = RegisterState.Success(userWithId)
                },
                onFailure = { err ->
                    _registerState.value = RegisterState.Error(err.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _currentUserId.value = null
        _authState.value = AuthState.Idle
        _registerState.value = RegisterState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}

class AuthViewModelFactory(
    private val repository: BloodDonorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
