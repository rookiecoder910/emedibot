package com.example.emedibotsimpleuserlogin

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class Medicine(val name: String, var time: String)

val SoftGreen = Color(0xFF4CAF50)
val SoftRed = Color(0xFFFF5252)

@Composable
fun ImprovedDropdown() {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(4.dp)) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Profile") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Notifications") },
                leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("About us") },
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = { expanded = false }
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun HomeScreen(onSignOut: () -> Unit, navController: NavHostController) {
    val context = LocalContext.current
    var medicines by remember { mutableStateOf(emptyList<Medicine>()) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val ref = Firebase.database.getReference("users").child(uid).child("medicines")

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

            override fun onCancelled(error: DatabaseError) {}
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.emedibot),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Emedibot",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Your Health Companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                ImprovedDropdown()
            }

            // Upcoming Dose
            nextMedicine?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Upcoming Dose",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = it.time,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Viewing Instructions for ${it.name}...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Instructions")
                        }
                    }
                }
            }

            // Device Status
            ImprovedDeviceStatusCard()

            // Order Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.pharmeasy.in".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Order New Medicines")
            }

            // Add Medicine
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add New Medicine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newMedicineName,
                        onValueChange = { newMedicineName = it },
                        label = { Text("Medicine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Schedule Time",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (newMedicineTime.isBlank()) "Not Set" else newMedicineTime,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newMedicineName.isNotBlank() && newMedicineTime.isNotBlank()) {
                                addMedicineToFirebase(
                                    Medicine(
                                        newMedicineName.trim(),
                                        newMedicineTime
                                    )
                                )

                                val timeParts = newMedicineTime.split(":", " ")
                                if (timeParts.size >= 3) {
                                    var hour = timeParts[0].toInt()
                                    val minute = timeParts[1].toInt()
                                    val amPm = timeParts[2]

                                    if (amPm.equals("PM", ignoreCase = true) && hour != 12) hour += 12
                                    if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

                                    scheduleDailyAlarm(context, hour, minute, newMedicineName.trim())
                                }

                                newMedicineName = ""
                                newMedicineTime = ""
                                Toast.makeText(
                                    context,
                                    "Medicine added successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter name and time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Medicine")
                    }
                }
            }

            // Today's Schedule
            if (medicines.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "${medicines.size} medications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                // Medicine List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    medicines.forEach { medicine ->
                        MedicineScheduleItem(
                            medicine = medicine,
                            onTimeChange = { newTime ->
                                updateSingleFirebaseMedicineTime(medicine.name, newTime)

                                val timeParts = newTime.split(":", " ")
                                if (timeParts.size >= 3) {
                                    var hour = timeParts[0].toInt()
                                    val minute = timeParts[1].toInt()
                                    val amPm = timeParts[2]
                                    if (amPm.equals("PM", ignoreCase = true) && hour != 12) hour += 12
                                    if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0
                                    scheduleDailyAlarm(context, hour, minute, medicine.name)
                                }
                            },
                            onDelete = {
                                deleteMedicine(medicine) {
                                    medicines = medicines.filter { it.name != medicine.name }
                                    Toast.makeText(
                                        context,
                                        "${medicine.name} deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Chat FAB
        FloatingActionButton(
            onClick = { navController.navigate("chatbot") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(R.drawable.gemii),
                contentDescription = "Gemini Chat",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ImprovedDeviceStatusCard() {
    val database = Firebase.database

    var dispenserStatus by remember { mutableStateOf("Not connected") }
    var batteryLevel by remember { mutableStateOf("N/A") }

    LaunchedEffect(Unit) {
        val statusRef = database.getReference("device_status")

        statusRef.child("dispenser").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                dispenserStatus = when (status) {
                    "connected" -> "Connected"
                    else -> "Not connected"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        statusRef.child("battery").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val battery = snapshot.getValue(String::class.java)
                batteryLevel = battery ?: "N/A"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val isConnected = dispenserStatus == "Connected"
    val statusColor = if (isConnected) SoftGreen else SoftRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Device Status",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dispenser", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        dispenserStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Battery", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (batteryLevel != "N/A") "$batteryLevel%" else batteryLevel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (batteryLevel != "N/A") SoftGreen else Color.Gray
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicineScheduleItem(
    medicine: Medicine,
    onTimeChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formattedTime = formatTime(hour, minute)
                onTimeChange(formattedTime)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = medicine.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Time",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun deleteMedicine(medicine: Medicine, onSuccess: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ref = Firebase.database.getReference("users")
        .child(uid)
        .child("medicines")
        .child(medicine.name.replace(" ", "_"))
    ref.removeValue().addOnSuccessListener {
        onSuccess()
    }
}

fun addMedicineToFirebase(medicine: Medicine) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ref = Firebase.database.getReference("users").child(uid).child("medicines")
    ref.child(medicine.name.replace(" ", "_")).setValue(medicine.time)
}

fun updateSingleFirebaseMedicineTime(medicineName: String, newTime: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ref = Firebase.database.getReference("users")
        .child(uid)
        .child("medicines")
        .child(medicineName.replace(" ", "_"))
    ref.setValue(newTime)
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(hour: Int, minute: Int): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val time = LocalTime.of(hour, minute)
    return time.format(formatter)
}