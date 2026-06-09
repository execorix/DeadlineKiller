@file:OptIn(ExperimentalMaterial3Api::class)

package com.bignerdranch.android.deadlinetimer


import android.Manifest
import androidx.room.Room
import androidx.activity.ComponentActivity
import android.annotation.SuppressLint
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
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import com.bignerdranch.android.deadlinetimer.ui.details.DetailsScreen
import com.bignerdranch.android.deadlinetimer.ui.details.DetailsViewModel
import com.bignerdranch.android.deadlinetimer.ui.main.MainScreen

import com.bignerdranch.android.deadlinetimer.ui.main.MainViewModel
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileScreen
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileViewModel
import com.bignerdranch.android.deadlinetimer.ui.theme.DeadlineTimerTheme
import kotlin.getValue
import kotlin.jvm.java




class MainActivity : ComponentActivity() {
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,
            "deadline_database"
        ).build()
    }

    val repository by lazy { DeadlineRepository(database.deadlineDao()) }

    val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }
    }

    @SuppressLint("ComposableDestinationInComposeScope")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    color = MaterialTheme.colorScheme.background // или Color(0xFF121214)
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "main_screen"
                    ) {
                        // 1. Главный экран дедлайнов
                        composable(route = "main_screen") {
                            val mainViewModel: MainViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
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
                                onNavigateToProfile = {
                                    navController.navigate("profile_screen")
                                },
                                onNavigateToCompleted = {
                                }
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
                                    onNavigateToMain = {
                                        navController.navigate("main_screen") {
                                            popUpTo("main_screen") { inclusive = true }
                                        }
                                    },
                                    onNavigateToProfile = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }



