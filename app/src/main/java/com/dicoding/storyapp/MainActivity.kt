package com.dicoding.storyapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.adapter.LoadingStateAdapter
import com.dicoding.storyapp.adapter.StoryAdapter
import com.dicoding.storyapp.data.StoryItem
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.maps.MapsActivity
import com.dicoding.storyapp.remote.ApiConfig
import com.dicoding.storyapp.upload.AddStoryActivity
import com.dicoding.storyapp.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val addStoryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                storyViewModel.refreshStories()

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupRecyclerView()
        setupViewModel()
        setupStoryItemClickListener()
        setupBottomNavigation()

    }


    private fun setupView() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun setupRecyclerView() {
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        storyAdapter = StoryAdapter()
        val loadingStateAdapter = LoadingStateAdapter { storyAdapter.retry() }
        binding.rvStory.adapter = storyAdapter.withLoadStateFooter(loadingStateAdapter)
    }

    private fun setupViewModel() {
        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(dataStore)
        val factory = ViewModelFactory(userPreference, apiService, applicationContext)

        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        storyViewModel = ViewModelProvider(this, factory).get(StoryViewModel::class.java)

        mainViewModel.getUser().observe(this, { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                observeStoryViewModel(userPreference)

            }
        })
    }

    private fun setupStoryItemClickListener() {
        storyAdapter.onItemClickListener = object : StoryAdapter.OnItemClickListener {
            override fun onItemClick(story: StoryItem) {
                val intent = Intent(this@MainActivity, StoryDetailActivity::class.java).apply {
                    putExtra(StoryDetailActivity.DETAIL_STORY_ID, story.id)
                }
                startActivity(intent)            }
        }
    }


    private fun observeStoryViewModel(userPreference: UserPreference) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                userPreference.getToken().collect { token ->
                    storyViewModel.stories.observe(this@MainActivity, { pagingData ->
                        storyAdapter.submitData(lifecycle, pagingData)
                    })
                }
                storyAdapter.loadStateFlow.collectLatest { loadStates ->
                    binding.progressBar.isVisible = loadStates.refresh is LoadState.Loading

                }
            }
        }

    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener  { menuItem ->
            when (menuItem.itemId) {
                R.id.action_map -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_add_story -> {
                    val intent = Intent(this, AddStoryActivity::class.java)
                    addStoryLauncher.launch(intent)
                    true
                }
                R.id.action_logout -> {
                    mainViewModel.logout()
                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }


}