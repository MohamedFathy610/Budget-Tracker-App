package com.depi.budgettracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "History", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expense Limits", Icons.AutoMirrored.Filled.List)
    object Savings : Screen("savings", "Saving Targets", Icons.Default.Star)
}

val items = listOf(Screen.Home, Screen.Expenses, Screen.Savings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: BudgetViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val state by viewModel.state.collectAsState()

    val fabOnClick: () -> Unit = {
        when (currentRoute) {
            Screen.Home.route -> {
                viewModel.showAddTransactionDialog()
            }
            Screen.Expenses.route -> {
                viewModel.showAddGoalDialog(GoalType.EXPENSE_LIMIT)
            }
            Screen.Savings.route -> {
                viewModel.showAddGoalDialog(GoalType.SAVING_TARGET)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Budget Tracker") }) },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = fabOnClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->

        if (state.isAddingTransaction) {
            AddTransactionDialog(
                viewModel = viewModel
            )
        }
        if (state.isAddingGoal) {
            AddGoalDialog(
                goalType = state.goalTypeForDialog,
                onDismiss = { viewModel.hideAddGoalDialog() },
                viewModel = viewModel
            )
        }


        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                BudgetHome(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }

            composable(Screen.Expenses.route) {
                GoalScreen(
                    viewModel = viewModel,
                    goalType = GoalType.EXPENSE_LIMIT,
                    title = "Monthly Expense Goals"
                )
            }

            composable(Screen.Savings.route) {
                GoalScreen(
                    viewModel = viewModel,
                    goalType = GoalType.SAVING_TARGET,
                    title = "Saving Goals"
                )
            }
        }
    }
}