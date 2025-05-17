package com.example.emedibotsimpleuserlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    private val databaseRef: DatabaseReference = Firebase.database.getReference("medicines")

    init {
        observeMedicineChanges()
    }

    private fun observeMedicineChanges() {
        databaseRef.addValueEventListener(object : ValueEventListener {
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
                // You might want to set an error state here
            }
        })
    }
}
