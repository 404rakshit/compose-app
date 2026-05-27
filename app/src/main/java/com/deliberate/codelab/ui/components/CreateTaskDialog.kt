package com.deliberate.codelab.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deliberate.quickalarm.domain.model.Repetition
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    // We update the onSave lambda to pass back our new complex data types!
    onSave: (String, String, Repetition, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Dropdown State
    var repetitionExpanded by remember { mutableStateOf(false) }
    var selectedRepetition by remember { mutableStateOf(Repetition.NONE) }

    // Time Picker State
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTimeInMillis by remember { mutableStateOf<Long?>(null) }
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // === 1. The Native Dropdown for Repetition ===
                ExposedDropdownMenuBox(
                    expanded = repetitionExpanded,
                    onExpandedChange = { repetitionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRepetition.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repetitionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = repetitionExpanded,
                        onDismissRequest = { repetitionExpanded = false }
                    ) {
                        Repetition.entries.forEach { rep ->
                            DropdownMenuItem(
                                text = { Text(rep.name) },
                                onClick = {
                                    selectedRepetition = rep
                                    repetitionExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 2. The Native Time Picker Trigger ===
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    val timeText = if (selectedTimeInMillis != null) {
                        // Formatting strictly for UI feedback
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeInMillis!! }
                        val hour = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
                        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
                        "Time: $hour:$minute"
                    } else "Set Exact Time"

                    Text(timeText)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, description, selectedRepetition, selectedTimeInMillis) }) {
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    // === 3. The Time Picker Popup ===
    if (showTimePicker) {
        TimePickerDialogWrapper(
            onCancel = { showTimePicker = false },
            onConfirm = {
                // Convert the selected hour/minute into a real Unix Timestamp
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                cal.set(Calendar.SECOND, 0)

                selectedTimeInMillis = cal.timeInMillis
                showTimePicker = false
            }
        ) {
            // The actual physical clock UI
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimePickerDialogWrapper(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        text = { content() }
    )
}