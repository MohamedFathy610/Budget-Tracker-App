package com.depi.budgettracker

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual


@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val description: String,
    val type: TransactionType,
    @Contextual val date: LocalDateTime)

enum class TransactionType {
    INCOME, EXPENSE
}
@Serializable
data class Goal(
    val id: String,
    val name: String,
    val type: GoalType,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val contributionAmount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

enum class GoalType {
    SAVING_TARGET, EXPENSE_LIMIT
}