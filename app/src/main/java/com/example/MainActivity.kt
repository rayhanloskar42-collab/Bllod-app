package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.repository.BloodDonorRepository
import com.example.data.session.SessionManager
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppContainer
import com.example.ui.screens.RegisterScreen
import com.example.ui.theme.BloodDonorTheme
import com.example.util.BloodNotificationHelper
import com.example.viewmodel.AuthState
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.AuthViewModelFactory
import com.example.viewmodel.BloodDonorViewModel
import com.example.viewmodel.BloodDonorViewModelFactory

enum class AuthScreenState {
    LOGIN,
    REGISTER
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: BloodDonorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        repository = BloodDonorRepository(database.userDao(), database.bloodRequestDao(), sessionManager)

        val authViewModelFactory = AuthViewModelFactory(repository)
        val authViewModel: AuthViewModel by viewModels { authViewModelFactory }

        val donorViewModelFactory = BloodDonorViewModelFactory(repository) {
            repository.getLoggedInUserId()
        }
        val bloodDonorViewModel: BloodDonorViewModel by viewModels { donorViewModelFactory }

        setContent {
            BloodDonorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BloodDonorApp(
                        authViewModel = authViewModel,
                        donorViewModel = bloodDonorViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BloodDonorApp(
    authViewModel: AuthViewModel,
    donorViewModel: BloodDonorViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val registerState by authViewModel.registerState.collectAsStateWithLifecycle()
    var currentAuthScreen by remember { mutableStateOf(AuthScreenState.LOGIN) }

    // Sync current logged-in user with DonorViewModel
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            donorViewModel.setCurrentUserId(user.id)
        } else {
            donorViewModel.setCurrentUserId(null)
        }
    }

    val context = LocalContext.current

    // Request notification permission on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* permission result handled */ }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Listen to emergency alerts and post system notification to matching donors
    LaunchedEffect(Unit) {
        donorViewModel.emergencyAlertFlow.collect { request ->
            BloodNotificationHelper.showEmergencyBloodAlert(context, request)
        }
    }

    AnimatedContent(
        targetState = authState is AuthState.Authenticated,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "auth_content_transition"
    ) { isAuthenticated ->
        if (isAuthenticated) {
            MainAppContainer(
                viewModel = donorViewModel,
                onLogout = {
                    authViewModel.logout()
                    currentAuthScreen = AuthScreenState.LOGIN
                }
            )
        } else {
            when (currentAuthScreen) {
                AuthScreenState.LOGIN -> {
                    LoginScreen(
                        authState = authState,
                        onLoginClick = { phone, pass ->
                            authViewModel.login(phone, pass)
                        },
                        onNavigateToRegister = {
                            authViewModel.resetRegisterState()
                            currentAuthScreen = AuthScreenState.REGISTER
                        },
                        onClearError = {
                            authViewModel.clearAuthError()
                        }
                    )
                }

                AuthScreenState.REGISTER -> {
                    BackHandler {
                        currentAuthScreen = AuthScreenState.LOGIN
                    }

                    RegisterScreen(
                        registerState = registerState,
                        onRegisterSubmit = { fName, lName, phone, pass, cPass, age, bg, loc, isAvail ->
                            authViewModel.register(
                                firstName = fName,
                                lastName = lName,
                                phoneNumber = phone,
                                password = pass,
                                confirmPassword = cPass,
                                age = age,
                                bloodGroup = bg,
                                location = loc,
                                isAvailable = isAvail
                            )
                        },
                        onNavigateBackToLogin = {
                            currentAuthScreen = AuthScreenState.LOGIN
                        }
                    )
                }
            }
        }
    }
}
