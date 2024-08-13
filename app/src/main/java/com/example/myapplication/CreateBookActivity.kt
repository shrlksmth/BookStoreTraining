package com.example.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityCreateBookBinding
import com.example.myapplication.utility.Pd
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
import java.util.Date
import java.util.Locale

class CreateBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBookBinding
    private var imgUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 1
    private lateinit var userInputBookName: EditText
    private lateinit var userInputBookAuthor: EditText
    private lateinit var userInputBookNotes: EditText
    private lateinit var createButton: Button
    private lateinit var chooseImageButton: Button
    private lateinit var backButton: Button


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

        createButton = binding.createButton
        chooseImageButton = binding.chooseImage
        backButton = binding.backButton
        userInputBookName = binding.userInputBookName
        userInputBookAuthor = binding.userInputBookAuthor
        userInputBookNotes = binding.userInputBookNotes

        backButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        chooseImageButton.setOnClickListener {
            showImagePickerDialog()

        }

        createButton.setOnClickListener {

            if (checkTextNull()) {
                uploadBookPic()
            }

        }
    }

    private fun insertDataToFirebase(bookImageUrl: String, progressDialog: ProgressDialog, imageName : String) {

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val database = FirebaseDatabase.getInstance().getReference("Users").child("SS")
        val bookId = database.push().key

        val currentMillis = System.currentTimeMillis()

        val book = bookDataClass(
            bookName = userInputBookName.text.toString(),
            bookAuthor = userInputBookAuthor.text.toString(),
            bookNotes = userInputBookNotes.text.toString(),
            bookDate = currentDate,
            bookUrl = bookImageUrl,
            bookID = bookId,
            timeStamp = currentMillis,
            imgUri = imgUri.toString(),
            imgName = imageName
        )

        database.child("book").child(bookId.toString()).setValue(book).addOnCompleteListener() {
            if (it.isSuccessful) {
                Pd.dismissProgressDialog(progressDialog)
                ToastUtil.showShortToast(this, "Successfully upload your data")
                startActivity(Intent(this, HomePageActivity::class.java))
            } else {
                Pd.dismissProgressDialog(progressDialog)
                ToastUtil.showShortToast(this, "Something went wrong")
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Action")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> useCamera()
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

    private fun useCamera() {
        cameraCheckPermission()
    }

    private fun cameraCheckPermission() {

        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            openCamera()
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
        AlertDialog.Builder(this).setMessage("It looks it u have turned off permissions")
            .setPositiveButton("Go to settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val imageView: ImageView = binding.bookImageView
            if (requestCode == CAMERA_REQUEST_CODE) {
                val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
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

    override fun onBackPressed() {
        super.onBackPressed()
        // Navigate to SpecificActivity when back button is pressed
        val intent = Intent(this, HomePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Optional: Clear the activity stack
        startActivity(intent)
        // Optional: Finish the current activity if you want to remove it from the stack
        finish()
    }

    private fun uploadBookPic() {
        if (imgUri != null) {

            val progressDialog = ProgressDialog(this)


            Pd.showProgressDialog("Uploading Data", "Processing...", progressDialog)

            val imageName = System.currentTimeMillis()

            val ref: StorageReference =
                FirebaseStorage.getInstance().getReference().child(imageName.toString())

            ref.putFile(imgUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val bookImageUrl = uri.toString()
                    insertDataToFirebase(bookImageUrl, progressDialog, imageName.toString())

                }.addOnFailureListener {
                    Pd.dismissProgressDialog(progressDialog)
                    ToastUtil.showShortToast(this, "Cannot get the url of image")
                }

            }.addOnFailureListener {
                Pd.dismissProgressDialog(progressDialog)
                ToastUtil.showShortToast(this, "Fail")
            }
        } else {
            ToastUtil.showShortToast(this, "Please Select image to upload")
        }
    }

    private fun checkTextNull() : Boolean {
        if(userInputBookName.text.isEmpty()){
            ToastUtil.showShortToast(this, "Please insert the book name")
            return false
        } else if(userInputBookAuthor.text.isEmpty()){
            ToastUtil.showShortToast(this, "Please insert the book author")
            return false
        } else {
            return true
        }
    }
}