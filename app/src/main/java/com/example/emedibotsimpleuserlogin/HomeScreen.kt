package com.example.emedibotsimpleuserlogin

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.fonts.Font
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class Medicine(val name: String, var time: String)

@SuppressLint("NewApi")
@Composable
fun HomeScreen(onSignOut: () -> Unit) {
    val context = LocalContext.current
    var medicines by remember { mutableStateOf(emptyList<Medicine>()) }

    LaunchedEffect(Unit) {
        val ref = Firebase.database.getReference("medicines")
        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val medicineList = mutableListOf<Medicine>()
                for (child in snapshot.children) {
                    val name = child.key?.replace("_", " ") ?: continue
                    val time = child.getValue(String::class.java) ?: continue
                    medicineList.add(Medicine(name, time))
                }
                medicines = medicineList.sortedBy { it.time }
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
        TimePickerDialog(
            context,
            { _, hour, minute ->
                newMedicineTime = formatTime(hour, minute)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {Image(
        painter = painterResource(id = R.drawable.homescr_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {


            nextMedicine?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F7F9) ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Upcoming Dose", style = typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(" ${it.name}", fontSize = 20.sp)
                        Text(" Time: ${it.time}", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            Toast.makeText(context, "Viewing Instructions for ${it.name}...", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.align(Alignment.End)) {
                            Text("View Instructions", fontFamily = FontFamily.SansSerif)
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text("Order New Medicines", fontSize = 16.sp)
            }

            Text(" Add New Medicine", color = colorScheme.primary, fontWeight = FontWeight.ExtraBold)

            OutlinedTextField(
                value = newMedicineName,
                onValueChange = { newMedicineName = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("\uD83D\uDD52 Time: ${if (newMedicineTime.isBlank()) "Not Set" else newMedicineTime}")
                Spacer(Modifier.width(8.dp))
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

            Text("Today's Schedule", style = typography.titleLarge)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                medicines.forEachIndexed { index, medicine ->
                    MedicineScheduleItem(
                        medicine = medicine,
                        onTimeChange = { newTime ->
                            val updated = medicines.toMutableList()
                            updated[index] = updated[index].copy(time = newTime)
                            medicines = updated
                            updateFirebaseMedicineTime(updated)
                        },
                        onDelete = {
                            deleteMedicine(medicine, medicines) { updatedList ->
                                medicines = updatedList
                            }
                        }
                    )
                }
            }


            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.errorContainer)
            ) {
                Text("Sign Out", fontSize = 16.sp, color = colorScheme.onErrorContainer)
            }
        }
    }
}

fun deleteMedicine(
    medicine: Medicine,
    medicines: List<Medicine>,
    updateMedicines: (List<Medicine>) -> Unit
) {
    val database = Firebase.database
    val ref = database.getReference("medicines").child(medicine.name.replace(" ", "_"))

    ref.removeValue().addOnSuccessListener {
        val updatedList = medicines.filter { it.name != medicine.name }
        updateMedicines(updatedList)
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
                batteryLevel = battery?.let { "\uD83D\uDD0B $battery%" } ?: "N/A"
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to load battery level", Toast.LENGTH_SHORT).show()
                batteryLevel = "N/A"
            }
        })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F7F9) ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Status", style = typography.titleMedium)
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
        medicineRef.setValue(medicine.time)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicineScheduleItem(medicine: Medicine, onTimeChange: (String) -> Unit, onDelete: () -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }
    var newTime by remember { mutableStateOf(medicine.time) }

    val context = LocalContext.current

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formattedTime = formatTime(hour, minute)
                newTime = formattedTime
                onTimeChange(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
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
            Text(text = medicine.name, style = typography.titleSmall)
            Text(text = "Time: $newTime", style = typography.bodySmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Set Time", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Delete", fontSize = 14.sp, color =colorScheme.error)
                }
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

