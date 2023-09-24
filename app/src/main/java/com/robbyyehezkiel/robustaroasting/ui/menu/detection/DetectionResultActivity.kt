package com.robbyyehezkiel.robustaroasting.ui.menu.detection

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.data.model.Photo
import com.robbyyehezkiel.robustaroasting.databinding.ActivityDetectionResultBinding
import com.robbyyehezkiel.robustaroasting.databinding.PopupDialogBinding
import com.robbyyehezkiel.robustaroasting.utils.createCustomTempFile
import com.robbyyehezkiel.robustaroasting.utils.showDialogInfo
import com.robbyyehezkiel.robustaroasting.utils.snackBarAction
import com.robbyyehezkiel.robustaroasting.utils.snackBarShort
import com.robbyyehezkiel.robustaroasting.utils.uriToFile
import java.io.File

class DetectionResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetectionResultBinding
    private lateinit var currentPhotoPath: String
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null
    private var roastResult: String = "Dump"
    private var getFile: File? = null
    private var valueLight: Int = 0
    private var valueMedium: Int = 0
    private var valueDark: Int = 0

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading(false)
        setupActionBar()
        initializeFirebase()
        requestPermissionsIfNeeded()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.toolbar_detection)
        }
    }

    private fun initializeFirebase() {
        storageRef = FirebaseStorage.getInstance().reference
        firebaseRef = FirebaseFirestore.getInstance()
    }

    private fun requestPermissionsIfNeeded() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupClickListeners() {
        binding.buttonUpload.setOnClickListener {
            if (getFile != null) {
                handleRoastResultUpload()
            } else {
                showNullImageSnackBar()
            }
        }

        binding.buttonDetailResult.setOnClickListener {
            if (getFile != null) {
                displayPopupDialog()
            } else {
                showNullImageSnackBar()
            }
        }

        binding.buttonCamera.setOnClickListener {
            startTakePhoto()
        }

        binding.buttonGallery.setOnClickListener {
            startGallery()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.tools_preview_image))
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@DetectionResultActivity,
                getString(R.string.tools_image_helper),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun handleRoastResultUpload() {
        val collectionRef = when (roastResult) {
            "Light" -> {
                storageRef = FirebaseStorage.getInstance().getReference("Light")
                firebaseRef.collection("light")
            }

            "Medium" -> {
                storageRef = FirebaseStorage.getInstance().getReference("Medium")
                firebaseRef.collection("medium")
            }

            "Dark" -> {
                storageRef = FirebaseStorage.getInstance().getReference("Dark")
                firebaseRef.collection("dark")
            }

            else -> {
                storageRef = FirebaseStorage.getInstance().getReference("Dump")
                firebaseRef.collection("default-collection")
            }
        }
        saveData(collectionRef)
    }

    private fun displayPopupDialog() {
        val popupDialog = createPopupDialog()
        popupDialog.show()
    }

    private fun createPopupDialog(): Dialog {
        val popupDialog = Dialog(this)
        val popupBinding = PopupDialogBinding.inflate(layoutInflater)
        popupDialog.setContentView(popupBinding.root)

        popupDialog.setCancelable(false)

        popupDialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.fade
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            popupBinding.edPopupLightPercentage.text = valueLight.toString()
            popupBinding.edPopupLightProgress.progress = valueLight

            popupBinding.edPopupMediumPercentage.text = valueMedium.toString()
            popupBinding.edPopupMediumProgress.progress = valueMedium

            popupBinding.edPopupDarkPercentage.text = valueDark.toString()
            popupBinding.edPopupDarkProgress.progress = valueDark

            val list = listOf(valueLight, valueMedium, valueDark)
            roastResult = when (list.maxOrNull() ?: 0) {
                valueLight -> getString(R.string.tools_light)
                valueMedium -> getString(R.string.tools_medium)
                valueDark -> getString(R.string.tools_dark)
                else -> getString(R.string.tools_null_data)
            }
            popupBinding.edPopupRoastInformation.text = roastResult

            popupBinding.edPopupCloseButton.setOnClickListener {
                popupDialog.dismiss()
            }
        }

        return popupDialog
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            myFile.let { file ->
                getFile = file
                binding.imageDetectionResult.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
            val uriConvert = Uri.fromFile(myFile)
            uri = uriConvert

            generateRandomRoastPercentages()
            displayPopupDialog()
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri

            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@DetectionResultActivity)
                getFile = myFile
                binding.imageDetectionResult.setImageURI(uri)
            }
            uri = selectedImg

            generateRandomRoastPercentages()
            displayPopupDialog()
        }
    }

    private fun generateRandomRoastPercentages() {
        valueLight = (0..100).random()
        valueMedium = (0..(100 - valueLight)).random()
        valueDark = 100 - valueLight - valueMedium
    }

    private fun saveData(collectionRef: CollectionReference) {
        showLoading(true)
        val photoId = collectionRef.document().id

        uri?.let { imageUri ->
            val photoRef = storageRef.child(photoId)
            photoRef.putFile(imageUri)
                .addOnSuccessListener { _ ->
                    photoRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            val photoUrl = downloadUrl.toString()
                            val photo = Photo(photoId, photoUrl)

                            collectionRef.document(photoId)
                                .set(photo)
                                .addOnSuccessListener {
                                    showLoading(false)
                                    snackBarShort(
                                        binding.root,
                                        getString(R.string.tools_stored_image)
                                    )
                                }
                                .addOnFailureListener { error ->
                                    showLoading(false)
                                    snackBarShort(
                                        binding.root,
                                        "Error storing data: ${error.message}"
                                    )
                                }
                        }
                }
                .addOnFailureListener { error ->
                    showLoading(false)
                    snackBarShort(binding.root, "Error uploading image: ${error.message}")
                }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun showNullImageSnackBar() {
        snackBarAction(
            this@DetectionResultActivity,
            getString(R.string.tools_null_image),
            getString(R.string.tools_app_info),
            {
                showDialogInfo(
                    this@DetectionResultActivity,
                    getString(R.string.tools_info_detection_title),
                    getString(R.string.tools_info_detection_content),
                    false
                )
            }
        )
    }
}
