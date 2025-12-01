package com.depi.budgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.depi.budgettracker.ui.theme.BudgetTrackerTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.depi.budgettracker.GoalListSerializer
import com.depi.budgettracker.TransactionListSerializer

private val Context.transactionDataStore: DataStore<List<Transaction>> by dataStore(
    fileName = "transactions.json",
    serializer = TransactionListSerializer
)

private val Context.goalDataStore: DataStore<List<Goal>> by dataStore(
    fileName = "goals.json",
    serializer = GoalListSerializer
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetTrackerTheme {

                val repository: BudgetRepository = remember {
                    BudgetRepository(
                        transactionDataStore = applicationContext.transactionDataStore,
                        goalDataStore = applicationContext.goalDataStore
                    )
                }

                val viewModel: BudgetViewModel = viewModel(
                    factory = BudgetViewModelFactory(repository)
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}