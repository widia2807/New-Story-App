package com.example.mystoryapp.ui.story

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.R
import com.example.mystoryapp.data.response.AddResponse
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import com.example.mystoryapp.databinding.ActivityUploadBinding
import com.example.mystoryapp.ui.main.main1.MainViewModel
import com.example.mystoryapp.ui.main.main2.ViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class UploadStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var userPreference: UserPreference
    private var currentImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let {
            outState.putString(EXTRA_IMAGE_URI, it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this.dataStore)

        savedInstanceState?.getString(EXTRA_IMAGE_URI)?.let {
            currentImageUri = Uri.parse(it)
            showImage()
        } ?: run {
            binding.previewImageView.setImageResource(R.drawable.ic_place_holder)
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.edAddDescription.text.toString()

            showLoading(true)

            lifecycleScope.launch {
                try {
                    val token = userPreference.getSession().first().token

                    if (token.isNotEmpty()) {
                        val requestBody = description.toRequestBody("text/plain".toMediaType())
                        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                        val multipartBody = MultipartBody.Part.createFormData(
                            "photo",
                            imageFile.name,
                            requestImageFile
                        )

                        val apiService = ApiConfig.getAuthenticatedApiService(token)
                        Log.d("PhotoActivity", "Request: Sending upload request to server")
                        val successResponse = apiService.addStory(
                            token = "Bearer $token",
                            photo = multipartBody,
                            description = requestBody
                        )
                        successResponse.message?.let {
                            showToast(it)
                            Log.d("PhotoActivity", "Response message: $it")
                        }
                        finish()
                    } else {
                        showToast(getString(R.string.empty_image_warning))
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, AddResponse::class.java)
                    errorResponse.message?.let {
                        showToast(it)
                        Log.e("PhotoActivity", "Error message: $it")
                    }
                } finally {
                    showLoading(false)
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_IMAGE_URI = "image_uri"
    }
}