package com.depi.budgettracker

import androidx.compose.ui.graphics.Color

data class PieChartSlice(
    val categoryName: String,
    val sliceAmount: Double,
    val percentage: Double,
    val sliceColor: Color
)

data class ExpenseReportData(
    val totalExpense: Double = 0.0,
    val slices: List<PieChartSlice> = emptyList()
)

data class TotalBalanceReport(
    val incomeSlice: PieChartSlice,
    val expenseSlice: PieChartSlice
)
