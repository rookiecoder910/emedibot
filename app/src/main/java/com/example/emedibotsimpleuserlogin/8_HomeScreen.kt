package com.example.emedibotsimpleuserlogin

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

data class Medicine(val name: String, var time: String)

@SuppressLint("NewApi")
@Composable
fun HomeScreen(onSignOut: () -> Unit) {
    val context = LocalContext.current

    var medicines by remember {
        mutableStateOf(
            listOf(
                Medicine("Tablet A", "8:00 AM"),
                Medicine("Tablet B", "12:00 PM"),
                Medicine("Tablet C", "6:00 PM")
            )
        )
    }

    val nextMedicine = medicines.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Welcome to E-medibot",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            // Upcoming Dose
            nextMedicine?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Upcoming Dose", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it.name, fontSize = 20.sp)
                        Text("Time: ${it.time}", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            Toast.makeText(context, "Viewing Instructions...", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("View Instructions")
                        }
                    }
                }
            }

            // Device Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dispenser: ✅ Connected")
                    Text("Battery Level: 🔋 85%")
                }
            }

            // Order Button
            Button(
                onClick = {
                    Toast.makeText(context, "Ordering new medicines...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Order New Medicines")
            }

            // Schedule Header
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Medicine Schedule List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                medicines.forEachIndexed { index, medicine ->
                    MedicineScheduleItem(medicine) { newTime ->
                        val updated = medicines.toMutableList()
                        updated[index] = updated[index].copy(time = newTime)
                        medicines = updated

                    }
                }
            }

            // Emergency Button (Add if needed)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicineScheduleItem(medicine: Medicine, onTimeChange: (String) -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }
    var newTime by remember { mutableStateOf(medicine.time) }

    val context = LocalContext.current

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = formatTime(selectedHour, selectedMinute)
                newTime = formattedTime
                onTimeChange(formattedTime)
            },
            hour,
            minute,
            false
        ).show()

        showTimePicker = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = medicine.name, style = MaterialTheme.typography.titleSmall)
            Text(text = "Time: $newTime", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Set Time")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(hour: Int, minute: Int): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val time = LocalTime.of(hour, minute)
    return time.format(formatter)
}

@Composable
fun QuickActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(label, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onSignOut = {})
}
