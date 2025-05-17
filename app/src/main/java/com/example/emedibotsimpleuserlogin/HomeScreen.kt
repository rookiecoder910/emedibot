package com.example.emedibotsimpleuserlogin

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.core.net.toUri


data class Medicine(val name: String, var time: String)
@SuppressLint("NewApi")
@Composable
fun HomeScreen(onSignOut: () -> Unit) {

    val context = LocalContext.current

    // ✅ Declare medicines state at the top
    var medicines by remember {
        mutableStateOf(emptyList<Medicine>())
    }


    LaunchedEffect(Unit) {
        val database = Firebase.database
        val ref = database.getReference("medicines")

        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val medicineList = mutableListOf<Medicine>()
                for (child in snapshot.children) {
                    val name = child.key?.replace("_", " ") ?: continue
                    val time = child.getValue(String::class.java) ?: continue
                    medicineList.add(Medicine(name, time))
                }
                medicines = medicineList.sortedBy { it.time }  // ✅ update state properly
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to load medicines: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }





    val nextMedicine = medicines.firstOrNull()


    var newMedicineName by remember { mutableStateOf("") }
    var newMedicineTime by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                newMedicineTime = formatTime(selectedHour, selectedMinute)
                showTimePicker = false
            },
            hour,
            minute,
            false
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to E-medibot",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            nextMedicine?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Upcoming Dose", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it.name, fontSize = 20.sp)
                        Text("Time: ${it.time}", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            Toast.makeText(context, "Viewing Instructions for ${it.name}...", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("View Instructions")
                        }
                    }
                }
            }

            DeviceStatusCard()

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.pharmeasy.in".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Order New Medicines", fontSize = 16.sp)
            }

            // 🔽 New Section: Add Medicine
            Text(
                text = "Add New Medicine",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp)
            )

            OutlinedTextField(
                value = newMedicineName,
                onValueChange = { newMedicineName = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selected Time: ${if (newMedicineTime.isBlank()) "None" else newMedicineTime}")
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showTimePicker = true }) {
                    Text("Set Time")
                }
            }

            Button(
                onClick = {
                    if (newMedicineName.isNotBlank() && newMedicineTime.isNotBlank()) {
                        val updated = medicines + Medicine(newMedicineName.trim(), newMedicineTime)
                        medicines = updated
                        updateFirebaseMedicineTime(updated)

                        newMedicineName = ""
                        newMedicineTime = ""
                    } else {
                        Toast.makeText(context, "Please enter name and time", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Medicine")
            }

            // 🔽 Existing Schedule Section
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                medicines.forEachIndexed { index, medicine ->
                    MedicineScheduleItem(medicine) { newTime ->
                        val updated = medicines.toMutableList()
                        updated[index] = updated[index].copy(time = newTime)
                        medicines = updated
                        updateFirebaseMedicineTime(updated)
                    }
                }
            }

            Button(
                onClick = {
                    onSignOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Sign Out", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DeviceStatusCard() {
    val context = LocalContext.current
    val database = Firebase.database

    var dispenserStatus by remember { mutableStateOf("Not connected") }
    var batteryLevel by remember { mutableStateOf("N/A") }

    LaunchedEffect(Unit) {
        val statusRef = database.getReference("device_status")

        statusRef.child("dispenser").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                dispenserStatus = when (status) {
                    "connected" -> "✅ Connected"
                    else -> "❌ Not connected"
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to load dispenser status", Toast.LENGTH_SHORT).show()
                dispenserStatus = "❌ Not connected"
            }
        })

        statusRef.child("battery").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val battery = snapshot.getValue(String::class.java)
                batteryLevel = battery?.let { "🔋 $battery%" } ?: "N/A"
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to load battery level", Toast.LENGTH_SHORT).show()
                batteryLevel = "N/A"
            }
        })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Status", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dispenser: $dispenserStatus")
            Text("Battery Level: $batteryLevel")
        }
    }
}

fun updateFirebaseMedicineTime(medicines: List<Medicine>) {
    val database = Firebase.database
    val ref = database.getReference("medicines")

    medicines.forEach { medicine ->
        val medicineRef = ref.child(medicine.name.replace(" ", "_"))
        medicineRef.setValue(medicine.time) // This writes to Firebase!
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = medicine.name, style = MaterialTheme.typography.titleSmall)
            Text(text = "Time: $newTime", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Set Time", fontSize = 14.sp)
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
fun PreviewHomeScreen() {
    HomeScreen(onSignOut = {})
}