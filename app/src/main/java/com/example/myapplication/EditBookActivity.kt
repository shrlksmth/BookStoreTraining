package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditBookActivity : AppCompatActivity() {

    private var isButtonPressed = false
    private lateinit var progressBar : ProgressBar
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
        val bookId = intent.getStringExtra("id")
        val bookImg = intent.getStringExtra("imgUrl")

        println(bookImg)

        val editButton: Button = findViewById(R.id.editButton)
        val bookNameText: TextView = findViewById(R.id.userInputBookName)
        val bookAuthorText: TextView = findViewById(R.id.userInputBookAuthor)
        val bookNotesText: TextView = findViewById(R.id.userInputBookNotes)
        progressBar = findViewById(R.id.progress_edit)

        bookNameText.text = bookName
        bookAuthorText.text = bookAuthor
        bookNotesText.text = bookNotes

        editButton.text = "EDIT"

        editButton.setOnClickListener {

            if (isButtonPressed) {
                progressBar.visibility = View.VISIBLE


                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val currentDate = sdf.format(Date())

                val editBookObject = bookDataClass(
                    bookName = bookNameText.text.toString(),
                    bookNotes = bookNotesText.text.toString(),
                    bookAuthor = bookAuthorText.text.toString(),
                    bookDate = currentDate.toString(),
                    bookUrl = bookImg.toString()
                )

                updateValue(editBookObject, bookId)

            } else {
                editButton.text = "DONE"
                bookNameText.isEnabled = true
                bookAuthorText.isEnabled = true
                bookNotesText.isEnabled = true
            }
            isButtonPressed = !isButtonPressed
        }

    }

    fun updateValue(book: bookDataClass, id: String? = null) {

        // Get a reference to the database
        val dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")
        // Define the path to the node you want to update

        // Update a specific field
        val updatedData = mapOf(id to book)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                // Handle successful update
                ToastUtil.showShortToast(this, "Successfully edit your book")

                startActivity(Intent(this, HomePageActivity::class.java))

            }
            .addOnFailureListener { e ->
                // Handle possible errors
                ToastUtil.showShortToast(this, "Error updating value: ${e.message}")

            }
    }
}