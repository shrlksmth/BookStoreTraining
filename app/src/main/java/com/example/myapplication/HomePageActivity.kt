package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityHomePageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var bookRecyclerView : RecyclerView
    private lateinit var bookArrayList: ArrayList<bookDataClass>
    private lateinit var progressBar : ProgressBar
    private  lateinit var linearLayout : LinearLayout
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

        getBookData(this)

        binding.toolbarLogoutButton.setOnClickListener{
            updateValue()
        }

        binding.toolbarPlusButton.setOnClickListener{
//            val intent = Intent(this, CreateBookActivity::class.java)
//            intent.putExtra("UserID", )
            startActivity(Intent(this, CreateBookActivity::class.java))
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(bookRecyclerView)
//
//        adapter.onItemClick = {
//            val intent = Intent(this, EditBookActivity::class.java)
//            intent.putExtra("android", "d")
//        }

    }



    fun updateValue() {
        // Get a reference to the database
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child("SS")

        // Define the path to the node you want to update

        // Update a specific field
        val updatedData = mapOf("isLogin" to false)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                // Handle successful update
                println("Value updated successfully.")
                startActivity(Intent(this, LoginActivity::class.java))

            }
            .addOnFailureListener { e ->
                // Handle possible errors
                println("Error updating value: ${e.message}")
            }
    }
    
    val simpleCallback = object :  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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

        val adapter : MyAdapter = MyAdapter(bookArrayList, this)
        // Replace this with your confirmation dialog code
        AlertDialog.Builder(viewHolder.itemView.context)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                adapter.removeItem(position, dbRef)
            }
            .setNegativeButton("No") { dialog, _ ->
                // If canceled, notify adapter to restore item
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun getBookData(context: Context) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")

        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    linearLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    bookArrayList.clear()

                    for (bookSnapshot in snapshot.children){
                        val book = bookSnapshot.getValue(bookDataClass::class.java)
                        bookArrayList.add(book!!)
                    }
                    bookRecyclerView.adapter = MyAdapter(bookArrayList, context)
                }

                else{
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