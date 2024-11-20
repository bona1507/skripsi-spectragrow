import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkmkcub.spectragrow.model.Plant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _plantList = MutableStateFlow<List<Plant>>(emptyList())
    val plantList: StateFlow<List<Plant>> = _plantList

    init {
        fetchPlants()
    }

    private fun fetchPlants() {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance("plant")
            db.collection("plant")
                .get()
                .addOnSuccessListener { documents ->
                    val plants = documents.mapNotNull { doc ->
                        doc.toObject(Plant::class.java)
                    }
                    _plantList.value = plants
                }
                .addOnFailureListener {
                    _plantList.value = emptyList()
                }
        }
    }
}
