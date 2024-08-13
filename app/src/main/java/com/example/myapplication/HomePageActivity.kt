package com.example.myapplication

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityHomePageBinding
import com.example.myapplication.utility.Pd
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var bookArrayList: ArrayList<bookDataClass>
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    private lateinit var adapter: MyAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        progressBar = findViewById(R.id.progress_circular)
        linearLayout = findViewById(R.id.linearLayHome)

        progressBar.visibility = View.VISIBLE

        bookRecyclerView = findViewById(R.id.bookRec)
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.setHasFixedSize(true)

        bookArrayList = arrayListOf<bookDataClass>()

        getBookDataFromFirebase(this)

        binding.toolbarLogoutButton.setOnClickListener {

            println(bookArrayList[0].bookName)


            val builder = AlertDialog.Builder(this)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure want to log out?")

            builder.setPositiveButton("OK") { dialog, _ ->
                UpdateLoginStateInFirebase()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }



        binding.toolbarPlusButton.setOnClickListener {
            startActivity(Intent(this, CreateBookActivity::class.java))
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(bookRecyclerView)
    }

    override fun onBackPressed() {
        // Create an AlertDialog to ask for exit confirmation
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
        builder.setMessage("Are you sure you want to exit the app?")

        // Positive button to confirm exit
        builder.setPositiveButton("Yes") { dialog, _ ->
            super.onBackPressed() // Call the default implementation to finish the activity
            finishAffinity() // Close all activities and exit the app
        }

        // Negative button to cancel exit
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog and continue the current activity
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun UpdateLoginStateInFirebase() {

        dbRef = FirebaseDatabase.getInstance().getReference("Users").child("SS")

        val progressDialog = ProgressDialog(this)

        Pd.showProgressDialog("Processing..", "Log out", progressDialog)

        val updatedData = mapOf("isLogin" to false)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                println("Login Value updated successfully.")
                startActivity(Intent(this, LoginActivity::class.java))
                Pd.dismissProgressDialog(progressDialog)
            }
            .addOnFailureListener { e ->
                println("Login Error updating value: ${e.message}")
            }
    }

    val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            showConfirmationDialog(viewHolder, position)
        }

    }

    private fun showConfirmationDialog(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val adapter: MyAdapter = MyAdapter(bookArrayList, this)
        // Replace this with your confirmation dialog code
        AlertDialog.Builder(viewHolder.itemView.context)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                adapter.removeBook(position)
            }
            .setNegativeButton("No") { dialog, _ ->
                // If canceled, notify adapter to restore item
                bookRecyclerView.adapter = MyAdapter(bookArrayList, this)
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun getBookDataFromFirebase(context: Context) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")

        dbRef.ref.orderByChild("timeStamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    linearLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    bookArrayList.clear()

                    for (bookSnapshot in snapshot.children) {
                        val book = bookSnapshot.getValue(bookDataClass::class.java)
                        bookArrayList.add(0, book!!)
                    }

                    bookRecyclerView.adapter = MyAdapter(bookArrayList, context)
                } else {
                    linearLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}