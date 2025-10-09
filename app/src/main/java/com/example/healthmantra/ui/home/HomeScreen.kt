package com.example.healthmantra.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.healthmantra.ui.components.EmptyState
import com.example.healthmantra.ui.components.ExerciseCard
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToConflicts: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Health Connect Permission Launcher
    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            viewModel.syncFromHealthConnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Mantra",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.conflictCount > 0) {
                        Badge(
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text("${uiState.conflictCount}")
                        }
                    }
                    IconButton(onClick = onNavigateToConflicts) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "View Conflicts"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        healthConnectPermissionLauncher.launch(
                            arrayOf("android.permission.health.READ_EXERCISE")
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Health Connect"
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Exercise"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.exercises.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.FitnessCenter,
                    message = "No exercises scheduled.\nTap + to schedule one!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onDelete = { viewModel.deleteExercise(exercise) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, duration, calories, scheduledTime ->
                viewModel.addExercise(name, duration, calories, scheduledTime)
                showAddDialog = false
            }
        )
    }
}

// Keep your existing AddExerciseDialog function here - don't change it

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    // Store as separate date and time components
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Build display date
    val displayCal = Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
        set(Calendar.MILLISECOND, 0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Schedule Exercise",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    leadingIcon = {
                        Icon(Icons.Default.FitnessCenter, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { char -> char.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { char -> char.isDigit() } },
                    label = { Text("Calories Burned") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalFireDepartment, null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Schedule Date & Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Date Picker Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(dateFormat.format(displayCal.time))
                }

                // Time Picker Button
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(timeFormat.format(displayCal.time))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && duration.isNotBlank() && calories.isNotBlank()) {
                        val finalCal = Calendar.getInstance()
                        finalCal.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
                        finalCal.set(Calendar.MILLISECOND, 0)
                        onAdd(name, duration.toInt(), calories.toInt(), finalCal.timeInMillis)
                    }
                },
                enabled = name.isNotBlank() && duration.isNotBlank() && calories.isNotBlank()
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val initialMillis = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }.timeInMillis

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = millis

                        selectedYear = utcCal.get(Calendar.YEAR)
                        selectedMonth = utcCal.get(Calendar.MONTH)
                        selectedDay = utcCal.get(Calendar.DAY_OF_MONTH)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}