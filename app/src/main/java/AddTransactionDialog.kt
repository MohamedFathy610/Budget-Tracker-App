package com.depi.budgettracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddTransactionDialog(viewModel: BudgetViewModel) {

    val state by viewModel.state.collectAsState()
    val input = state.transactionInput

    val isEditMode = input.id != null
    val dialogTitle = if (isEditMode) "Edit Transaction" else "Add New Transaction"
    val confirmButtonText = if (isEditMode) "Save Changes" else "Add"

    if (state.isAddingTransaction) {
        Dialog(onDismissRequest = viewModel::hideAddTransactionDialog) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = input.description,
                        onValueChange = { newDescription ->
                            viewModel.updateTransactionInput(
                                id = input.id,
                                description = newDescription,
                                amount = input.amount,
                                type = input.type
                            )
                        },
                        label = { Text("Description") },
                        leadingIcon = {
                            Icon(Icons.Filled.Description, contentDescription = "Description")
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = input.amount,
                        onValueChange = { newAmount ->
                            val filteredAmount = newAmount.filter { it.isDigit() || it == '.' }
                            viewModel.updateTransactionInput(
                                id = input.id,
                                description = input.description,
                                amount = filteredAmount,
                                type = input.type
                            )
                        },
                        label = { Text("Amount") },
                        leadingIcon = {
                            Icon(Icons.Filled.AttachMoney, contentDescription = "Amount")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TransactionType.entries.forEach { type ->
                            ChoiceButton(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = input.type == type,
                                selectedColor = if (type == TransactionType.INCOME) Color(0xFF66BB6A) else Color(0xFFFF7043)
                            ) {
                                viewModel.updateTransactionInput(
                                    id = input.id,
                                    description = input.description,
                                    amount = input.amount,
                                    type = type
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = viewModel::hideAddTransactionDialog) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.addOrUpdateTransaction()
                            },
                            enabled = input.description.isNotBlank() && input.amount.toDoubleOrNull() != null
                        ) {
                            Text(confirmButtonText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val buttonBorder = if (isSelected) {
        BorderStroke(1.dp, selectedColor)
    } else {
        ButtonDefaults.outlinedButtonBorder(enabled = true)
    }

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) selectedColor.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface,
        ),
        border = buttonBorder,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}