// MedicineViewModel.kt
package com.example.emedibotsimpleuserlogin

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MedicineViewModel : ViewModel() {
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> get() = _medicines

    private val dbRef = Firebase.database.getReference("medicines")

    init {
        loadMedicinesFromFirebase()
    }

    private fun loadMedicinesFromFirebase() {
        dbRef.get().addOnSuccessListener { snapshot ->
            val loadedMedicines = mutableListOf<Medicine>()
            snapshot.children.forEach {
                val name = it.key?.replace("_", " ") ?: return@forEach
                val time = it.getValue(String::class.java) ?: return@forEach
                loadedMedicines.add(Medicine(name, time))
            }
            _medicines.value = loadedMedicines
        }
    }

    fun addMedicine(medicine: Medicine) {
        val updated = _medicines.value + medicine
        _medicines.value = updated
        dbRef.child(medicine.name.replace(" ", "_")).setValue(medicine.time)
    }

    fun updateMedicineTime(index: Int, newTime: String) {
        val updatedList = _medicines.value.toMutableList()
        val updatedMedicine = updatedList[index].copy(time = newTime)
        updatedList[index] = updatedMedicine
        _medicines.value = updatedList
        dbRef.child(updatedMedicine.name.replace(" ", "_")).setValue(updatedMedicine.time)
    }
}
