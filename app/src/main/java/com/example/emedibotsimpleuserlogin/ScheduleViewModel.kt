import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

// Data model for Medicine (you can modify it based on your needs)
data class Medicine(val name: String, var time: String)

class ScheduleViewModel : ViewModel() {
    private val _medicines = mutableStateOf<List<Medicine>>(emptyList())
    val medicines: State<List<Medicine>> = _medicines

    // Method to update the schedule
    fun setMedicineTime(medicine: Medicine, newTime: String) {
        _medicines.value = _medicines.value.map {
            if (it.name == medicine.name) {
                it.copy(time = newTime)
            } else {
                it
            }
        }
    }

    // Method to add new medicine (optional)
    fun addMedicine(medicine: Medicine) {
        _medicines.value = _medicines.value + medicine
    }
}


