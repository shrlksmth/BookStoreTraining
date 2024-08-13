package com.example.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EditBookActivity : AppCompatActivity() {

    private var isButtonPressed = false
    private lateinit var progressBar: ProgressBar
    private lateinit var bookImageView: ImageView
    private lateinit var bookNameText: TextView
    private lateinit var bookAuthorText: TextView
    private lateinit var bookNotesText: TextView
    private lateinit var bookImgUrl: String
    private lateinit var bookId: String

    private var imgUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bookName = intent.getStringExtra("name")
        val bookAuthor = intent.getStringExtra("author")
        val bookNotes = intent.getStringExtra("notes")
        bookId = intent.getStringExtra("id")!!
        val bookImg = intent.getStringExtra("imgUrl")

        val editButton: Button = findViewById(R.id.editButton)
        bookNameText = findViewById(R.id.userInputBookName)
        bookAuthorText = findViewById(R.id.userInputBookAuthor)
        bookNotesText = findViewById(R.id.userInputBookNotes)
        bookImageView = findViewById(R.id.bookImageView)
        val chooseImageButton: Button = findViewById(R.id.chooseImageButtonEdit)
        progressBar = findViewById(R.id.progress_edit)

        Picasso.get()
            .load(bookImg)
            .placeholder(R.drawable.baseline_image_search_24)
            .into(bookImageView)

        bookNameText.text = bookName
        bookAuthorText.text = bookAuthor
        bookNotesText.text = bookNotes

        editButton.text = "EDIT"

        editButton.setOnClickListener {

            if (isButtonPressed) {

                println(imgUri)

                uploadBookPic()

            } else {
                editButton.text = "DONE"
                bookNameText.isEnabled = true
                bookAuthorText.isEnabled = true
                bookNotesText.isEnabled = true
                chooseImageButton.visibility = View.VISIBLE
            }
            isButtonPressed = !isButtonPressed
        }

        chooseImageButton.setOnClickListener {
            showImagePickerDialog()
        }

    }

    fun updateValue(progressDialog: ProgressDialog) {

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val editBookObject = bookDataClass(
            bookName = bookNameText.text.toString(),
            bookNotes = bookNotesText.text.toString(),
            bookAuthor = bookAuthorText.text.toString(),
            bookDate = currentDate.toString(),
            bookUrl = bookImgUrl,
            bookID = bookId
        )

        progressDialog.setTitle("Uploading Data..")
        progressDialog.setCancelable(false)

        // Get a reference to the database
        val dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")
        // Define the path to the node you want to update

        // Update a specific field
        val updatedData = mapOf(bookId to editBookObject)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                progressDialog.dismiss()
                // Handle successful update
                ToastUtil.showShortToast(this, "Successfully edit your book")

                startActivity(Intent(this, HomePageActivity::class.java))
            }
            .addOnFailureListener { e ->
                // Handle possible errors
                ToastUtil.showShortToast(this, "Error updating value: ${e.message}")

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

    private fun uploadBookPic() {
        if (imgUri != null) {

            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading Image..")
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Processing...")
            progressDialog.show()

            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val imageName = "img_${sdf.format(Date())}"

            val ref: StorageReference =
                FirebaseStorage.getInstance().getReference().child(imageName)

            ref.putFile(imgUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    bookImgUrl = uri.toString()

                    updateValue(progressDialog)

                }.addOnFailureListener {
                    progressDialog.dismiss()
                    ToastUtil.showShortToast(this, "Cannot get the url of image")
                }

            }.addOnFailureListener {
                progressDialog.dismiss()
                ToastUtil.showShortToast(this, "Fail to Edit Image")
            }
        } else {
            ToastUtil.showShortToast(this, "Please Select image to upload")
        }
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

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == CAMERA_REQUEST_CODE) {
                val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
                bookImageView.setImageBitmap(thumbNail)
                imgUri = saveBitmapToFile(this, thumbNail)
            } else if (requestCode == 200) {
                imgUri = data.data
                println(imgUri)
                bookImageView.setImageURI(imgUri)
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
}