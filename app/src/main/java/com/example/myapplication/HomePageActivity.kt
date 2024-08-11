package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
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

        progressBar = binding.progressBar

        progressBar.visibility = View.VISIBLE

        bookRecyclerView = findViewById(R.id.bookRec)
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.setHasFixedSize(true)

        bookArrayList = arrayListOf<bookDataClass>()
        getBookData()

        binding.toolbarPlusButton.setOnClickListener{
//            val intent = Intent(this, CreateBookActivity::class.java)
//            intent.putExtra("UserID", )
            startActivity(Intent(this, CreateBookActivity::class.java))
        }


    }

    private fun getBookData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")

        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (bookSnapshot in  snapshot.children){
                        val book = bookSnapshot.getValue(bookDataClass::class.java)
                        bookArrayList.add(book!!)
                    }

                    bookRecyclerView.adapter = MyAdapter(bookArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}