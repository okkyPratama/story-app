package com.dicoding.storyapp.upload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.StoryViewModel
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.di.Injection
import com.dicoding.storyapp.repository.StoryRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var storyRepository: StoryRepository
    private var selectedImageUri: Uri? = null
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var viewModel: StoryViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var uploadProgressBar: ProgressBar
    private var file: File? = null


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Izin lokasi diperlukan untuk menambahkan lokasi.",
                    Toast.LENGTH_SHORT
                ).show()
                binding.switchAddLocation.isChecked = false
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.upload_title)
        storyRepository = Injection.provideRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        uploadProgressBar = binding.uploadProgressBar

        binding.switchAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestLocationPermission()
            } else {
                currentLat = null
                currentLon = null
            }
        }

        val factory = ViewModelFactory(
            UserPreference.getInstance(dataStore),
            Injection.provideApiService(),
            this
        )
        viewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]

        viewModel.uploadStatus.observe(this) { uploadStatus ->
            when (uploadStatus) {

                StoryViewModel.LoadingStatus.LOADING -> {
                    uploadProgressBar.visibility = View.VISIBLE
                }

                StoryViewModel.LoadingStatus.SUCCESS -> {
                    uploadProgressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Mengunggah cerita berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                StoryViewModel.LoadingStatus.ERROR -> {
                    uploadProgressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Gagal mengunggah cerita",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedImageUri =
                        Uri.parse(result.data?.getStringExtra(CameraActivity.IMAGE_URI))

                    selectedImageUri?.let { uri ->
                        file = uriToFile(uri, this)


                        file?.let {file ->
                            val exifInterface = ExifInterface(file.path)
                            when (exifInterface.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED
                            )) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> rotateFile(file, 90f)
                                ExifInterface.ORIENTATION_ROTATE_180 -> rotateFile(file, 180f)
                                ExifInterface.ORIENTATION_ROTATE_270 -> rotateFile(file, 270f)

                            }

                        }


                        binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file?.path))
                    }
                }
            }


        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedImageUri = result.data?.data

                    selectedImageUri?.let { uri ->
                        val inputStream = contentResolver.openInputStream(uri)
                        val result = BitmapFactory.decodeStream(inputStream)
                        file = createCustomTempFile(this)
                        val outputStream = FileOutputStream(file)
                        result.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.close()
                        inputStream?.close()
                        binding.previewImageView.setImageBitmap(result)
                    }
                }
            }

        setupListeners()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Izin kamera diperlukan untuk mengambil foto.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                getMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Izin lokasi diperlukan untuk menambahkan lokasi.",
                    Toast.LENGTH_SHORT
                ).show()
                binding.switchAddLocation.isChecked = false
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(takePictureIntent)

    }

    private fun setupListeners() {
        binding.cameraButton.setOnClickListener {
            requestCameraPermission()
        }


        binding.galleryButton.setOnClickListener {
            val pickImageIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(pickImageIntent)
        }


        binding.uploadButton.setOnClickListener {
            val storyDescription = binding.edAddDescription.text.toString()
            if (selectedImageUri != null && storyDescription.isNotEmpty()) {

                uploadStory()
            } else {
                Toast.makeText(this, "gambar dan deskripsi wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    Toast.makeText(this, "Lokasi berhasil didapatkan", Toast.LENGTH_SHORT).show()
                    Log.d("AddStoryActivity", "Koordinat lokasi: $currentLat, $currentLon")

                } else {
                    Toast.makeText(this, "Gagal mengambil lokasi", Toast.LENGTH_SHORT)
                        .show()
                    binding.switchAddLocation.isChecked = false
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Gagal mengambil lokasi: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun uploadStory() {
        selectedImageUri?.let { uri ->

            file?.let { file ->
                val reducedFile = reduceFileImage(file)
                val descriptionText = binding.edAddDescription.text.toString()

                lifecycleScope.launch {
                    val token = UserPreference.getInstance(dataStore).getToken().first()
                    viewModel.uploadStory(token, reducedFile, descriptionText, currentLat, currentLon)
                }
            }


        } ?: run {
            Toast.makeText(
                this,
                "Silakan masukkan berkas gambar terlebih dahulu.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101

    }
}