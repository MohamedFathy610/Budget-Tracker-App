package com.depi.budgettracker

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

class BudgetRepository (
    private val transactionDataStore: DataStore<List<Transaction>>,
    private val goalDataStore: DataStore<List<Goal>>
) {

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyList())
            } else {
                throw exception
            }
        }

    suspend fun addOrUpdateTransaction(transaction: Transaction) {
        transactionDataStore.updateData { currentList ->
            val existingTransaction = currentList.find { it.id == transaction.id }

            if (existingTransaction != null) {
                currentList.map {
                    if (it.id == transaction.id) transaction else it
                }
            } else {
                currentList.toMutableList().apply {
                    add(0, transaction)
                }.toList()
            }
        }
    }

    suspend fun deleteTransaction(id: String) {
        transactionDataStore.updateData { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun getAllGoals(): Flow<List<Goal>> = goalDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyList())
            } else {
                throw exception
            }
        }

    suspend fun addOrUpdateGoal(goal: Goal) {
        goalDataStore.updateData { currentList ->
            val existingGoal = currentList.find { it.id == goal.id }
            if (existingGoal != null) {
                currentList.map { if (it.id == goal.id) goal else it }
            } else {
                currentList.toMutableList().apply { add(goal) }.toList()
            }
        }
    }

    suspend fun deleteGoal(id: String) {
        goalDataStore.updateData { currentList ->
            currentList.filter { it.id != id }
        }
    }
}