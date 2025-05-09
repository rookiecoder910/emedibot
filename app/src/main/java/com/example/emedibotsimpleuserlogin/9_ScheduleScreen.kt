package com.example.emedibotsimpleuserlogin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleScreen() {
    val medications = remember {
        listOf(
            "Aspirin - 08:00 AM",
            "Vitamin D - 12:00 PM",
            "Ibuprofen - 06:00 PM"
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(medications) { med ->
            Text(med, modifier = Modifier.padding(16.dp))
        }
    }
}