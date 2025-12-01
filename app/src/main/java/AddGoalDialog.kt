package com.depi.budgettracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.UUID

@Composable
fun AddGoalDialog(
    goalType: GoalType,
    onDismiss: () -> Unit,
    viewModel: BudgetViewModel
) {
    val state by viewModel.state.collectAsState()
    val goalInput = state.goalInput
    val name = goalInput.name
    val targetAmount = goalInput.targetAmount
    val contributionAmount = goalInput.contributionAmount
    val isEditMode = goalInput.id != null

    val dialogTitle = if (isEditMode) {
        if (goalType == GoalType.EXPENSE_LIMIT) "Edit Expense Limit" else "Edit Saving Target"
    } else {
        if (goalType == GoalType.EXPENSE_LIMIT) "Set Expense Limit" else "Set Saving Target"
    }

    val targetLabel = if (goalType == GoalType.EXPENSE_LIMIT) "Total Expense Limit" else "Target Saving Goal"
    val contributionLabel = if (goalType == GoalType.EXPENSE_LIMIT) "Monthly Max Expenditure" else "Monthly Contribution"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        viewModel.updateGoalInput(goalInput.id, newName, targetAmount, goalType, contributionAmount)
                    },
                    label = { Text("Goal Name / Category") },
                    leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { newAmount ->
                        val cleanInput = newAmount.filter { char -> char.isDigit() || char == '.' }
                        viewModel.updateGoalInput(goalInput.id, name, cleanInput, goalType, contributionAmount)
                    },
                    label = { Text(targetLabel) },
                    leadingIcon = { Icon(Icons.Filled.Money, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contributionAmount,
                    onValueChange = { newContribution ->
                        val cleanInput = newContribution.filter { char -> char.isDigit() || char == '.' }
                        viewModel.updateGoalInput(goalInput.id, name, targetAmount, goalType, cleanInput)
                    },
                    label = { Text(contributionLabel) },
                    leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull()
                    val contribution = contributionAmount.toDoubleOrNull()

                    val isValid = name.isNotBlank() && amount != null && amount > 0 && contribution != null && contribution > 0

                    if (isValid) {
                        viewModel.addGoal()
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() &&
                        targetAmount.toDoubleOrNull() != null && targetAmount.toDouble() > 0 &&
                        contributionAmount.toDoubleOrNull() != null && contributionAmount.toDouble() > 0
            ) {
                Text(if (isEditMode) "Save Changes" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}