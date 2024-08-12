package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values



class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler().postDelayed({
            checkLoginState()

        }, 2000)



    }

    private fun checkLoginState() {

        val dbRef =
            FirebaseDatabase.getInstance().getReference("Users").child("SS").child("isLogin")

        dbRef.get().addOnSuccessListener { data: DataSnapshot ->
            if (data.exists()) {
                val isLogin: Boolean = data.value as Boolean

                if (isLogin == true) {
                    startActivity(Intent(this, HomePageActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }.addOnFailureListener {
            println("failed")
        }

    }

    data class Book(
        val bookName: String,
        val bookAuthor: String
    )
}