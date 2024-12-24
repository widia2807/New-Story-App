package com.example.mystoryapp.ui.detailstory

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.databinding.ActivityDetailBinding
import com.example.mystoryapp.ui.main.main1.MainViewModel
import com.example.mystoryapp.ui.main.main2.ViewModelFactory
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, com.example.mystoryapp.data.retrofit.ApiConfig().getApiService("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXlqNXBjX0xBUkNfQWdLNjEiLCJpYXQiOjE2NDE3OTk5NDl9.flEMaQ7zsdYkxuyGbiXjEDXO8kuDTcI__3UjCwt6R_I"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID) ?: run {
            Log.e(TAG, "No Story ID passed to activity")
            return
        }

        displayStoryDetails(storyId)
    }

    private fun displayStoryDetails(storyId: String) {
        lifecycleScope.launch {
            try {
                val storyDetails = viewModel.fetchStoryDetail(storyId)
                bindDataToUI(storyDetails)
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to load story details: ${exception.localizedMessage}", exception)
            }
        }
    }

    private fun bindDataToUI(storyDetail: DetailStoryResponse) {
        storyDetail.story?.let { story ->
            with(binding) {
                tvItemNameDesc.text = story.name
                tvItemDescriptionDesc.text = story.description
                Glide.with(this@DetailActivity)
                    .load(story.photoUrl)
                    .into(imgItemPhotoDesc)
            }
        } ?: Log.e(TAG, "Story data is null")
    }

    companion object {
        private const val TAG = "DetailActivity"
        const val EXTRA_STORY_ID = "STORY_ID"
    }
}