package com.example.emedibotsimpleuserlogin

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

import com.google.firebase.database.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleViewModel : ViewModel() {

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    // Reference to the logged-in user's medicines
    private val databaseRef: DatabaseReference? = Firebase.auth.currentUser?.uid?.let { uid ->
        Firebase.database.getReference("users").child(uid).child("medicines")
    }

    init {
        observeMedicineChanges()
    }

    private fun observeMedicineChanges() {
        databaseRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicineList = mutableListOf<Medicine>()
                for (child in snapshot.children) {
                    val name = child.key?.replace("_", " ") ?: continue
                    val time = child.getValue(String::class.java) ?: continue
                    medicineList.add(Medicine(name, time))
                }
                _medicines.value = medicineList.sortedBy { it.time }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle error
            }
        })
    }
}
