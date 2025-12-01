package com.depi.budgettracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.toJavaLocalDateTime
import androidx.compose.ui.graphics.Color
fun getStartOfMonth(now: LocalDateTime): LocalDateTime {
    return LocalDateTime(
        year = now.year,
        month = now.month,
        dayOfMonth = 1,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    )
}

data class TransactionInput(
    val id: String? = null,
    val description: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
)

data class GoalInput(
    val id: String? = null,
    val name: String = "",
    val targetAmount: String = "",
    val type: GoalType = GoalType.EXPENSE_LIMIT,
    val contributionAmount: String = "",
    val currentAmount: Double = 0.0
)


data class BudgetState(
    val transactions: List<Transaction> = emptyList(),
    val expenseGoals: List<Goal> = emptyList(),
    val savingGoals: List<Goal> = emptyList(),

    val balance: Double = 0.0,
    val totalBalanceReport: TotalBalanceReport = TotalBalanceReport(
        PieChartSlice("", 0.0, 0.0, Color.Transparent),
        PieChartSlice("", 0.0, 0.0, Color.Transparent)
    ),

    val isAddingTransaction: Boolean = false,
    val isAddingGoal: Boolean = false,
    val goalTypeForDialog: GoalType = GoalType.EXPENSE_LIMIT,

    val transactionInput: TransactionInput = TransactionInput(),

    val goalInput: GoalInput = GoalInput(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        combine(
            repository.getAllTransactions(),
            repository.getAllGoals()
        ) { transactions, goals ->

            val (balance, report) = calculateFinancialSummary(transactions)
            val (expenseGoals, savingGoals) = calculateGoalsStatus(goals, transactions)

            _state.update { currentState ->
                currentState.copy(
                    transactions = transactions,
                    balance = balance,
                    totalBalanceReport = report,
                    expenseGoals = expenseGoals,
                    savingGoals = savingGoals
                )
            }
        }.launchIn(viewModelScope)
    }

    fun updateTransactionInput(id: String?, description: String, amount: String, type: TransactionType) {
        _state.update {
            it.copy(
                transactionInput = TransactionInput(
                    id = id,
                    description = description,
                    amount = amount,
                    type = type
                )
            )
        }
    }

    fun startEditingTransaction(transaction: Transaction?) {
        val input = if (transaction == null) {
            TransactionInput()
        } else {
            TransactionInput(
                id = transaction.id,
                description = transaction.description,
                amount = transaction.amount.toString(),
                type = transaction.type
            )
        }

        _state.update {
            it.copy(
                transactionInput = input,
                isAddingTransaction = true
            )
        }
    }


    fun addOrUpdateTransaction() {
        val input = _state.value.transactionInput

        val amount = input.amount.toDoubleOrNull() ?: return
        if (input.description.isBlank() || amount <= 0) return

        viewModelScope.launch {
            val existingTransaction = input.id?.let { id ->
                _state.value.transactions.find { it.id == id }
            }

            val transaction = Transaction(
                id = input.id ?: UUID.randomUUID().toString(),
                amount = amount,
                description = input.description.trim(),
                type = input.type,
                date = existingTransaction?.date ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            repository.addOrUpdateTransaction(transaction)

            _state.update {
                it.copy(
                    isAddingTransaction = false,
                    transactionInput = TransactionInput()
                )
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun showAddTransactionDialog() {
        _state.update {
            it.copy(
                isAddingTransaction = true,
                transactionInput = TransactionInput()
            )
        }
    }

    fun hideAddTransactionDialog() {
        _state.update {
            it.copy(
                isAddingTransaction = false,
                transactionInput = TransactionInput()
            )
        }
    }


    fun showAddGoalDialog(type: GoalType) {
        _state.update {
            it.copy(
                isAddingGoal = true,
                goalTypeForDialog = type,
                goalInput = GoalInput(type = type)
            )
        }
    }

    fun hideAddGoalDialog() {
        _state.update {
            it.copy(
                isAddingGoal = false,
                goalInput = GoalInput()
            )
        }
    }

    fun updateGoalInput(id: String?, name: String, targetAmount: String, type: GoalType, contributionAmount: String) {
        _state.update {
            it.copy(
                goalInput = GoalInput(
                    id = id,
                    name = name,
                    targetAmount = targetAmount,
                    type = type,
                    contributionAmount = contributionAmount,
                    currentAmount = it.goalInput.currentAmount
                )
            )
        }
    }


    fun startEditingGoal(goal: Goal) {
        _state.update {
            it.copy(
                isAddingGoal = true,
                goalTypeForDialog = goal.type,
                goalInput = GoalInput(
                    id = goal.id,
                    name = goal.name,
                    targetAmount = goal.targetAmount.toString(),
                    type = goal.type,
                    currentAmount = goal.currentAmount,
                    contributionAmount = goal.contributionAmount?.toString() ?: ""
                )
            )
        }
    }

    fun addGoal() {
        val input = _state.value.goalInput
        val targetAmount = input.targetAmount.toDoubleOrNull() ?: return

        val contributionAmount = input.contributionAmount.toDoubleOrNull()
        if (input.name.isBlank() || targetAmount <= 0) return

        val finalContributionAmount = if (input.type == GoalType.SAVING_TARGET) contributionAmount ?: 0.0 else null

        viewModelScope.launch {
            val goal = Goal(
                id = input.id ?: UUID.randomUUID().toString(),
                name = input.name.trim(),
                targetAmount = targetAmount,
                currentAmount = input.currentAmount,
                type = input.type,
                contributionAmount = finalContributionAmount,
                startDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toFormattedString("yyyy-MM-dd"),
                endDate = null
            )
            repository.addOrUpdateGoal(goal)

            hideAddGoalDialog()
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal.id)
        }
    }


    private fun calculateGoalsStatus(allGoals: List<Goal>, transactions: List<Transaction>): Pair<List<Goal>, List<Goal>> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfMonth = getStartOfMonth(now)

        val updatedExpenseGoals = mutableListOf<Goal>()
        val updatedSavingGoals = mutableListOf<Goal>()

        allGoals.forEach { goal ->
            when (goal.type) {
                GoalType.EXPENSE_LIMIT -> {
                    val currentMonthExpense = transactions
                        .filter {
                            it.type == TransactionType.EXPENSE &&
                                    it.description.equals(goal.name, ignoreCase = true) &&
                                    it.date >= startOfMonth
                        }
                        .sumOf { it.amount }

                    updatedExpenseGoals.add(goal.copy(currentAmount = currentMonthExpense))
                }
                GoalType.SAVING_TARGET -> {
                    val totalSaved = transactions
                        .filter {
                            it.type == TransactionType.INCOME &&
                                    it.description.equals(goal.name, ignoreCase = true)
                        }
                        .sumOf { it.amount }

                    updatedSavingGoals.add(goal.copy(currentAmount = totalSaved))
                }
            }
        }

        return Pair(updatedExpenseGoals, updatedSavingGoals)
    }

    private fun calculateFinancialSummary(transactions: List<Transaction>): Pair<Double, TotalBalanceReport> {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = income - expenses
        val total = income + expenses

        val incomePercentage = if (total > 0) (income / total) * 100 else 0.0
        val expensePercentage = if (total > 0) (expenses / total) * 100 else 0.0

        val incomeSlice = PieChartSlice(
            categoryName = "Income",
            sliceAmount = income,
            percentage = incomePercentage,
            sliceColor = Color(0xFF66BB6A)
        )
        val expenseSlice = PieChartSlice(
            categoryName = "Expense",
            sliceAmount = expenses,
            percentage = expensePercentage,
            sliceColor = Color(0xFFFF7043)
        )

        val report = TotalBalanceReport(incomeSlice, expenseSlice)
        return Pair(balance, report)
    }
}