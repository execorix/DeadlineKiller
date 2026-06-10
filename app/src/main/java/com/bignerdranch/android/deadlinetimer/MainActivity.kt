@file:OptIn(ExperimentalMaterial3Api::class)

package com.bignerdranch.android.deadlinetimer


import android.Manifest
import androidx.room.Room
import androidx.activity.ComponentActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import com.bignerdranch.android.deadlinetimer.ui.auth.AuthScreen
import com.bignerdranch.android.deadlinetimer.ui.complited.CompletedScreen
import com.bignerdranch.android.deadlinetimer.ui.complited.CompletedViewModel
import com.bignerdranch.android.deadlinetimer.ui.details.DetailsScreen
import com.bignerdranch.android.deadlinetimer.ui.details.DetailsViewModel
import com.bignerdranch.android.deadlinetimer.ui.main.MainScreen

import com.bignerdranch.android.deadlinetimer.ui.main.MainViewModel
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileScreen
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileViewModel
import com.bignerdranch.android.deadlinetimer.ui.settings.SettingsScreen
import com.bignerdranch.android.deadlinetimer.ui.theme.AccentPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.BackgroundDark
import com.bignerdranch.android.deadlinetimer.ui.theme.DeadlineTimerTheme
import com.bignerdranch.android.deadlinetimer.ui.theme.SurfaceDark
import org.mindrot.jbcrypt.BCrypt
import kotlin.getValue
import kotlin.jvm.java




class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,
            "deadline_database"
        ).build()
    }

    private val repository by lazy { DeadlineRepository(database.deadlineDao()) }

    private val securePrefs by lazy {
        try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                applicationContext,
                "secure_user_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            applicationContext.getSharedPreferences("user_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    @SuppressLint("ComposableDestinationInComposeScope")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUserRegistered = securePrefs.getString("user_name", null) != null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        setContent {
            DeadlineTimerTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        containerColor = BackgroundDark,
                        bottomBar = {
                            val mainRoutes = listOf("main_screen", "profile_screen", "completed")
                            if (currentRoute in mainRoutes) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .background(SurfaceDark),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (currentRoute != "profile_screen") {
                                                    navController.navigate("profile_screen") {
                                                        popUpTo("main_screen") { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_person),
                                            contentDescription = "Профиль",
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = if (currentRoute == "profile_screen")
                                                androidx.compose.ui.graphics.ColorFilter.tint(AccentPrimary) else null
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (currentRoute != "main_screen") {
                                                    navController.navigate("main_screen") {
                                                        popUpTo("main_screen") { inclusive = false }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_alarm),
                                            contentDescription = "Дедлайны",
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = if (currentRoute == "main_screen")
                                                androidx.compose.ui.graphics.ColorFilter.tint(AccentPrimary) else null
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (currentRoute != "completed") {
                                                    navController.navigate("completed") {
                                                        popUpTo("main_screen") { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_checkbox),
                                            contentDescription = "Выполненные",
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = if (currentRoute == "completed")
                                                androidx.compose.ui.graphics.ColorFilter.tint(AccentPrimary) else null
                                        )
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = if (isUserRegistered) "main_screen" else "auth_screen",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable(route = "auth_screen") {
                                AuthScreen(
                                    onAuthSuccess = { login, email, password ->
                                        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))
                                        securePrefs.edit().apply {
                                            putString("user_name", login)
                                            putString("user_email", email)
                                            putString("user_password", hashedPassword)
                                            apply()
                                        }
                                        navController.navigate("main_screen") {
                                            popUpTo("auth_screen") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(route = "main_screen") {
                                val mainViewModel: MainViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return MainViewModel(repository) as T
                                        }
                                    }
                                )

                                MainScreen(
                                    viewModel = mainViewModel,
                                    onAddDeadlineClick = {
                                        navController.navigate("details_screen/0")
                                    },
                                    onDeadlineClick = { deadlineId ->
                                        navController.navigate("details_screen/$deadlineId")
                                    },
                                    onNavigateToProfile = {},
                                    onNavigateToCompleted = {}
                                )
                            }

                            composable(
                                route = "details_screen/{deadlineId}",
                                arguments = listOf(
                                    navArgument("deadlineId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val deadlineId = backStackEntry.arguments?.getInt("deadlineId") ?: 0

                                val detailsViewModel: DetailsViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return DetailsViewModel(repository) as T
                                        }
                                    }
                                )

                                DetailsScreen(
                                    viewModel = detailsViewModel,
                                    deadlineId = deadlineId,
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )

                            }

                            composable(route = "profile_screen") {
                                val profileViewModel: ProfileViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return ProfileViewModel(
                                                repository,
                                                applicationContext
                                            ) as T
                                        }
                                    }
                                )

                                ProfileScreen(
                                    viewModel = profileViewModel,
                                    onNavigateToSettings = {
                                        navController.navigate("settings_screen")
                                    }
                                )
                            }
                            composable(route = "settings_screen") {
                                val profileViewModel: ProfileViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return ProfileViewModel(
                                                repository,
                                                applicationContext
                                            ) as T
                                        }
                                    }
                                )
                                SettingsScreen(
                                    viewModel = profileViewModel,
                                    securePrefs = securePrefs,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(route = "completed") {
                                val completedViewModel: CompletedViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return CompletedViewModel(repository) as T
                                        }
                                    }
                                )

                                CompletedScreen(
                                    viewModel = completedViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


