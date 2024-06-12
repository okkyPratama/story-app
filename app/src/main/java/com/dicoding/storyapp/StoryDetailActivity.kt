package com.dicoding.storyapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.databinding.ActivityStoryDetailBinding
import com.dicoding.storyapp.remote.ApiConfig

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding
    private lateinit var detailStoryViewModel: StoryViewModel
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(applicationContext.dataStore)
        val factory = ViewModelFactory(userPreference, apiService, applicationContext)

        val storyId = intent.getStringExtra(DETAIL_STORY_ID)
        if (storyId != null) {
            detailStoryViewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]
            detailStoryViewModel.getStoryDetail(storyId)
            detailStoryViewModel.storyDetail.observe(this) { story ->
                story?.let {
                    binding.tvDetailName.text = it.name
                    Glide.with(this).load(it.photoUrl).into(binding.ivDetailPhoto)
                    binding.tvDetailDescription.text = it.description
                } ?: run {
                    Toast.makeText(
                        this,
                        "Terjadi kesalahan saat memuat detail cerita.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            detailStoryViewModel.loadingStatus.observe(this) { status ->
                when (status) {
                    StoryViewModel.LoadingStatus.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    StoryViewModel.LoadingStatus.SUCCESS,
                    StoryViewModel.LoadingStatus.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    else -> {

                    }
                }
            }
        } else {
            Toast.makeText(this, "ID cerita tidak valid.", Toast.LENGTH_LONG).show()
        }


    }

    companion object {
        const val DETAIL_STORY_ID = "detail_story_id"
    }
}