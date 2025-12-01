package com.depi.budgettracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

fun LocalDateTime.toFormattedString(pattern: String = "dd MMM yyyy"): String {
    return this.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}

@Composable
fun BudgetHome(viewModel: BudgetViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {

        BalanceSummaryCard(balance = state.balance)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Income vs Expense", style = MaterialTheme.typography.titleMedium)
                TotalBalanceChart(reportData = state.totalBalanceReport)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transactions History",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.transactions.sortedByDescending { it.date }, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onDelete = { id -> viewModel.deleteTransaction(id) },
                    onEdit = { trans -> viewModel.startEditingTransaction(trans) }
                )
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun TotalBalanceChart(reportData: TotalBalanceReport, chartSize: Dp = 150.dp) {
    val slices = listOf(reportData.incomeSlice, reportData.expenseSlice)
        .filter { it.sliceAmount > 0 }
        .sortedByDescending { it.percentage }

    if (slices.isEmpty()) {
        Text("No transactions to display.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Canvas(modifier = Modifier
            .size(chartSize)
            .align(Alignment.CenterVertically)) {
            var startAngle = 0f
            slices.forEach { slice ->
                val sweepAngle = (slice.percentage.toFloat() / 100f) * 360f

                drawArc(
                    color = slice.sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width, size.height),
                    style = Fill
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            slices.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.sliceColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${slice.categoryName}: EGP %.0f (%.1f%%)".format(
                            slice.sliceAmount, slice.percentage
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun GoalScreen(
    viewModel: BudgetViewModel,
    goalType: GoalType,
    title: String
) {
    val state by viewModel.state.collectAsState()

    val goals = remember(state.expenseGoals, state.savingGoals, goalType) {
        when (goalType) {
            GoalType.EXPENSE_LIMIT -> state.expenseGoals.toList()
            GoalType.SAVING_TARGET -> state.savingGoals.toList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (goals.isEmpty()) {
            Text("No goals set yet for ${title.lowercase()}.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(goals, key = { it.id }) { goal ->
                    GoalItem(
                        goal = goal,
                        goalType = goalType,
                        onEdit = { viewModel.startEditingGoal(it) },
                        onDelete = { viewModel.deleteGoal(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Button(
            onClick = { viewModel.showAddGoalDialog(goalType) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Add New Goal")
        }
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    goalType: GoalType,
    onEdit: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    val actualProgress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val percentage = (actualProgress * 100).roundToInt()
    val isExpenseExceeded = goalType == GoalType.EXPENSE_LIMIT && actualProgress > 1f
    val isSavingCompleted = goalType == GoalType.SAVING_TARGET && actualProgress >= 1f

    val color = when {
        isExpenseExceeded -> Color(0xFFFF5252)
        isSavingCompleted -> Color(0xFF42A5F5)
        goalType == GoalType.EXPENSE_LIMIT -> Color(0xFFFF7043)
        else -> Color(0xFF66BB6A)
    }
    val monthsToReach = remember(goal.targetAmount, goal.currentAmount, goal.contributionAmount) {
        if (goalType == GoalType.SAVING_TARGET && goal.contributionAmount != null && goal.contributionAmount > 0) {
            val remaining = goal.targetAmount - goal.currentAmount
            if (remaining > 0) {
                ceil(remaining / goal.contributionAmount.toDouble()).toInt()
            } else {
                0
            }
        } else {
            null
        }
    }

    val remainingValue = goal.targetAmount - goal.currentAmount


    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))

                    if (goalType == GoalType.SAVING_TARGET) {
                        Text(
                            text = "Saved: EGP ${"%.2f".format(goal.currentAmount)} / Target: EGP ${"%.2f".format(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )

                        Text(
                            text = "Remaining: EGP ${"%.2f".format(kotlin.math.max(0.0, remainingValue))}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))

                        monthsToReach?.let { months ->
                            if (months > 0) {
                                Text(
                                    text = "Monthly Contribution: EGP ${"%.2f".format(goal.contributionAmount!!)} (${months} months to reach goal)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "Goal Reached! 🎉",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        val statusText = if (remainingValue >= 0) {
                            "Remaining: EGP ${"%.2f".format(remainingValue)}"
                        } else {
                            "Overspent: EGP ${"%.2f".format(kotlin.math.abs(remainingValue))}"
                        }

                        Text(
                            text = "Spent: EGP ${"%.2f".format(goal.currentAmount)} / Limit: EGP ${"%.2f".format(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (remainingValue < 0) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { onEdit(goal) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Goal",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = { onDelete(goal) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(color = color),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: (String) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) Color(0xFFFF7043) else Color(0xFF66BB6A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (isExpense) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                contentDescription = transaction.type.name,
                tint = amountColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = transaction.date.toFormattedString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "EGP ${"%.2f".format(transaction.amount)}",
                color = amountColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onEdit(transaction) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Transaction",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = { onDelete(transaction.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Transaction", tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun BalanceSummaryCard(balance: Double) {
    val balanceColor = if (balance >= 0) Color(0xFF66BB6A) else Color(0xFFFF7043)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "EGP ${"%.2f".format(balance)}",
                style = MaterialTheme.typography.headlineLarge,
                color = balanceColor
            )
        }
    }
}