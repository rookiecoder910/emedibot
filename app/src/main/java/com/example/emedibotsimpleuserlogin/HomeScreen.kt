package com.example.emedibotsimpleuserlogin
import com.example.emedibotsimpleuserlogin.showNotification

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.fonts.Font
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import com.google.firebase.database.ValueEventListener

import com.google.firebase.database.DatabaseError
import java.util.Collections.frequency

data class Medicine(val name: String, var time: String)



@SuppressLint("NewApi")
@Composable
fun HomeScreen(onSignOut: () -> Unit) {
    val context = LocalContext.current
    var medicines by remember { mutableStateOf(emptyList<Medicine>()) }

    LaunchedEffect(Unit) {
        val ref = Firebase.database.getReference("medicines")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicineList = mutableListOf<Medicine>()
                for (child in snapshot.children) {
                    val name = child.key?.replace("_", " ") ?: continue
                    val time = child.getValue(String::class.java) ?: continue
                    medicineList.add(Medicine(name, time))
                }
                medicines = medicineList.sortedBy { it.time }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load medicines: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val nextMedicine = medicines.firstOrNull()
    LaunchedEffect(nextMedicine) {
        nextMedicine?.let {
            showNotification(

                context,

                "",
                "${it.name} at ${it.time}"
            )
        }
    }
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
    ) {
        Image(
            painter = painterResource(id = R.drawable.homescr_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.1f), blendMode = BlendMode.Darken)
        )

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Image(
                    painter = painterResource(id = R.drawable.emedibot),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .width(48.dp)
                        .height(48.dp)
                )
                Text("Hi, User!", style = typography.titleMedium, modifier = Modifier.align(Alignment.CenterVertically))
            }

            nextMedicine?.let {
                var isUpcomingExpanded by remember { mutableStateOf(true) }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isUpcomingExpanded = !isUpcomingExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upcoming Dose", style = typography.titleLarge)

                            val icon = if (isUpcomingExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = "Toggle Upcoming Dose",
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                        }

                        AnimatedVisibility(visible = isUpcomingExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${it.name}", fontSize = 20.sp)
                                Text("Time: ${it.time}", fontSize = 16.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                ElevatedButton(
                                    onClick = {
                                        Toast.makeText(context, "Viewing Instructions for ${it.name}...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Instructions")
                                }
                            }
                        }
                    }
                }
            }


            DeviceStatusCard()

            ElevatedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.pharmeasy.in".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Red,
                    contentColor = Black
                )
            ) {
                Text("Order New Medicines", fontSize = 16.sp)
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Text("Add New Medicine", style = typography.titleMedium, color = colorScheme.onSurface)
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
                    Text("Time: ${if (newMedicineTime.isBlank()) "Not Set" else newMedicineTime}",)
                    Button(onClick = { showTimePicker = true }) {
                        Text("Set Time")
                    }
                }

                ElevatedButton(
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
            }

            Text("Today's Schedule", style = typography.titleLarge, color = Black)

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


    var isExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val statusRef = database.getReference("device_status")

        statusRef.child("dispenser").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                dispenserStatus = when (status) {
                    "connected" -> "✅ Connected"
                    else -> "❌ Not connected"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load dispenser status", Toast.LENGTH_SHORT).show()
            }
        })

        statusRef.child("battery").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val battery = snapshot.getValue(String::class.java)
                batteryLevel = battery?.let { "\uD83D\uDD0B $battery%" } ?: "N/A"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load battery level", Toast.LENGTH_SHORT).show()
            }
        })
    }


    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with toggle icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Device Status", style = typography.titleMedium)


                val icon = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                        .clickable { isExpanded = !isExpanded }
                )
            }


            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dispenser: $dispenserStatus")
                    Text("Battery Level: $batteryLevel")
                }
            }
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

    ElevatedCard  (
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
                OutlinedButton (
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

