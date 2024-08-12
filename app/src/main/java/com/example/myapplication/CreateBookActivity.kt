package com.example.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityCreateBookBinding
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBookBinding
    private var imgUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 1

    companion object {
        private const val CAMERA = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val createButton = binding.createButton as Button
        val chooseImageButton = binding.chooseImage as Button
        val backButton = binding.backButton as Button

        backButton.setOnClickListener{
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        chooseImageButton.setOnClickListener {
            showImagePickerDialog()
        }

        createButton.setOnClickListener {
            uploadBookPic()
        }
    }

    private fun insertDataToFirebase(dwUrl: String) {
        val userInputBookName = binding.userInputBookName as EditText
        val userInputBookAuthor = binding.userInputBookAuthor as EditText
        val userInputBookNotes = binding.userInputBookNotes as EditText

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())



        val database = FirebaseDatabase.getInstance().getReference("Users").child("SS")
        val bookId = database.push().key

        val book = bookDataClass(
            bookName = userInputBookName.text.toString(),
            bookAuthor = userInputBookAuthor.text.toString(),
            bookNotes = userInputBookNotes.text.toString(),
            bookDate = currentDate,
            bookUrl = dwUrl,
            bookID = bookId
        )

        database.child("book").child(bookId.toString()).setValue(book).addOnCompleteListener() {
            if (it.isSuccessful) {

            } else {
                ToastUtil.showShortToast(this, "Failed")
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Action")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> chooseImage()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Choose Image to Upload"), 200
        )
    }

    private fun openCamera() {
        ToastUtil.showShortToast(this, "Still under development")


        cameraCheckPermission()
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent, CAMERA)
    }

    private fun cameraCheckPermission() {

        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA).withListener(
            object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if(report.areAllPermissionsGranted()){
                            camera()
                        }
                    }


                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                    showRorationalDialogForPermission()

                }

            }
        ).onSameThread().check()

    }

    private fun showRorationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage("It looks it u have turned off permissions").setPositiveButton("Go to settings"){
            _,_ ->
            try {

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)

            }catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }

            .setNegativeButton("CANCEL"){
                dialog, _->
                dialog.dismiss()
            }.show()
    }

    private fun camera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val imageView: ImageView = binding.bookImageView
            if (requestCode == CAMERA_REQUEST_CODE) {
                val thumbNail : Bitmap = data!!.extras!!.get("data") as Bitmap
                imageView.setImageBitmap(thumbNail)
                imgUri = saveBitmapToFile(this, thumbNail)
            } else if (requestCode == 200) {
                imgUri = data.data
                imageView.setImageURI(imgUri)
            }
        }
    }

    @Throws(IOException::class)
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri {
        // Create a file in the cache directory
        val file = File(context.cacheDir, "bitmap_${System.currentTimeMillis()}.png")

        // Write bitmap to the file
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        }

        // Return the URI of the file
        return Uri.fromFile(file)
    }

    private fun uploadBookPic() {
        if (imgUri != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading Image..")
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Processing...")
            progressDialog.show()

            val ref: StorageReference =
                FirebaseStorage.getInstance().getReference().child(UUID.randomUUID().toString())

            ref.putFile(imgUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val dwUrl = uri.toString()
                    insertDataToFirebase(dwUrl)
                }.addOnFailureListener{
                    ToastUtil.showShortToast(this,"Cannot get the url of image")
                }
                progressDialog.dismiss()
                ToastUtil.showShortToast(this, "Success")
                startActivity(Intent(this, HomePageActivity::class.java))
            }.addOnFailureListener {
                progressDialog.dismiss()
                ToastUtil.showShortToast(this, "Fail")
            }
        } else{
            ToastUtil.showShortToast(this, "Please Select image to upload")
        }
    }
}