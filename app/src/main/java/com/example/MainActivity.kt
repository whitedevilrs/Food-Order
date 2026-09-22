package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.MenuRepository
import com.example.ui.*
import com.example.ui.theme.LucknawiChaiTheme

import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val database = AppDatabase.getDatabase(this, lifecycleScope)
    val repository = MenuRepository(database.menuDao())
    val viewModelFactory = MenuViewModelFactory(repository)

    enableEdgeToEdge()
    setContent {
      LucknawiChaiTheme {
        val navController = rememberNavController()
        val menuViewModel: MenuViewModel = viewModel(factory = viewModelFactory)
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        val homeDestination = if (currentUser != null) "menu" else "login"

        NavHost(navController = navController, startDestination = "splash", modifier = Modifier.fillMaxSize()) {
          composable("splash") {
            SplashScreen(
              onAnimationFinished = {
                navController.navigate(homeDestination) {
                  popUpTo("splash") { inclusive = true }
                }
              }
            )
          }
          composable("login") {
            LoginScreen(
              onLoginSuccess = {
                navController.navigate("menu") {
                  popUpTo("login") { inclusive = true }
                }
              }
            )
          }
          composable("menu") {
            MenuScreen(
              viewModel = menuViewModel,
              onNavigateToCart = { navController.navigate("cart") },
              onLogout = {
                navController.navigate("login") {
                  popUpTo("menu") { inclusive = true }
                }
              }
            )
          }
          composable("cart") {
            CartScreen(
              viewModel = menuViewModel,
              onBack = { navController.popBackStack() },
              onOrderPlaced = { navController.navigate("confirmation") }
            )
          }
          composable("confirmation") {
            OrderConfirmationScreen(
              onHome = { 
                navController.navigate("menu") {
                  popUpTo("menu") { inclusive = true }
                }
              }
            )
          }
        }
      }
    }
  }
}
