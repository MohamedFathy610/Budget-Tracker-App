package com.depi.budgettracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons


@Composable
fun ReportScreen(viewModel: BudgetViewModel) {
    val state by viewModel.state.collectAsState()

    val incomeSlice = state.totalBalanceReport.incomeSlice
    val expenseSlice = state.totalBalanceReport.expenseSlice

    val totalIncome = incomeSlice.sliceAmount
    val totalExpense = expenseSlice.sliceAmount
    val totalBalance = state.balance


    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportSummaryCard(
                    title = "Total Income",
                    amount = totalIncome,
                    color = Color(0xFF66BB6A), // Green
                    modifier = Modifier.weight(1f)
                )

                ReportSummaryCard(
                    title = "Total Expense",
                    amount = totalExpense,
                    color = Color(0xFFFF7043), // Orange
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "${formatCurrency(totalBalance)} SAR",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (totalBalance >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Income vs. Expense Breakdown",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PieChart(
                slices = listOf(incomeSlice, expenseSlice),
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        items(state.transactions.sortedByDescending { it.date.toInstant(TimeZone.currentSystemDefault()) }) { transaction ->
            TransactionItem(
                transaction = transaction,
                onEditClicked = { viewModel.startEditingTransaction(transaction) },
                onDeleteClicked = { viewModel.deleteTransaction(transaction.id) }
            )
        }
    }
}


@Composable
fun ReportSummaryCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "SAR",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onEditClicked: (Transaction) -> Unit,
    onDeleteClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onEditClicked(transaction) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconColor = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFE53935)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME) "↑" else "↓",
                    fontSize = 18.sp,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.date.toFormattedString("yyyy-MM-dd HH:mm"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${formatCurrency(transaction.amount)} SAR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )

            IconButton(onClick = { onDeleteClicked(transaction.id) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return String.format("%.2f", amount)
}

@Composable
fun PieChart(slices: List<PieChartSlice>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(50))
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        if (slices.any { it.sliceAmount > 0 }) {
            Text("Chart Placeholder", color = Color.Gray)
        } else {
            Text("No Data", color = Color.Gray)
        }
    }
}
