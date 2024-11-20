package com.pkmkcub.spectragrow.view.ui.userstory

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pkmkcub.spectragrow.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StoryViewModel : ViewModel() {

    private val _storyList = MutableStateFlow<List<Story>>(emptyList())
    val storyList: StateFlow<List<Story>> = _storyList

    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress

    private val firestore = FirebaseFirestore.getInstance("plant")
    private val storage = FirebaseStorage.getInstance()

    init {
        fetchAllStories()
    }

    private fun fetchAllStories() {
        _isLoading.value = true
        firestore.collection("stories")
            .get()
            .addOnSuccessListener { result ->
                val stories = result.map { document ->
                    document.toObject(Story::class.java)
                }
                _storyList.value = stories
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                Log.e("StoryViewModel", "Error fetching stories", exception)
            }
    }

    fun fetchStoryByTitle(title: String) {
        firestore.collection("stories")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val story = document.toObject(Story::class.java)
                    _selectedStory.value = story
                } else {
                    Log.e("StoryViewModel", "No story found with the given title")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("StoryViewModel", "Error fetching story by title", exception)
            }
    }

    fun addStory(story: Story, imageUri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true
        uploadImage(imageUri) { imageUrl ->
            val newStory = story.copy(photo_url = imageUrl)
            firestore.collection("stories")
                .add(newStory)
                .addOnSuccessListener {
                    fetchAllStories() // Refresh the story list
                    _isLoading.value = false
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    _isLoading.value = false
                    onFailure(exception.message ?: "Error adding story")
                }
        }
    }

    private fun uploadImage(imageUri: Uri, onComplete: (String) -> Unit) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageRef = storage.reference.child("images/${timeStamp}.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                Log.e("StoryViewModel", "Image upload failed", exception)
            }

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            _uploadProgress.value = progress
        }
    }

    fun saveImageToCache(context: Context, bitmap: Bitmap): Uri? {
        val fileName = "image_${UUID.randomUUID()}.jpg"
        val cacheDir = context.cacheDir

        val file = File(cacheDir, fileName)

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            return FileProvider.getUriForFile(context, "com.pkmkcub.spectragrow.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}
