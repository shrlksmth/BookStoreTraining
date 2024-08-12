package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // checkLoginState()

        val loginButton = binding.loginButtonID as Button
        val userInputUserID = binding.userInputUserID as EditText
        val userInputPassword = binding.userInputPasswordID as EditText
        val database = FirebaseDatabase.getInstance()

        loginButton.setOnClickListener {

            if (userInputPassword.text.toString().isEmpty() && userInputUserID.text.toString()
                    .isEmpty()
            ) {
                ToastUtil.showShortToast(this, "Please enter the userID and Password")
            } else if (userInputUserID.text.toString().isEmpty()) {
                ToastUtil.showShortToast(this, "Please enter the userID")
            } else if (userInputPassword.text.toString().isEmpty()) {
                ToastUtil.showShortToast(this, "Please enter the password")
            } else {
                val myRef = database.getReference("Users")
                myRef.child(userInputUserID.text.toString()).get().addOnSuccessListener {
                    if (it.exists()) {
                        val password = it.child("password").value.toString()
                        if (password == userInputPassword.text.toString()) {
                            updateValue()

                        } else {
                            ToastUtil.showShortToast(this, "The password is wrong")
                        }
                    } else {
                        ToastUtil.showShortToast(this, "The UserID doesn't exist")
                    }
                }.addOnFailureListener {
                    ToastUtil.showShortToast(this, "Something Went Wrong")
                }
            }
        }
    }

    fun updateValue() {
        // Get a reference to the database
        val dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child("SS")

        // Define the path to the node you want to update

        // Update a specific field
        val updatedData = mapOf("isLogin" to true)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                // Handle successful update
                startActivity(Intent(this, HomePageActivity::class.java))
                ToastUtil.showShortToast(this, "Successfully Login")
            }
            .addOnFailureListener { e ->
                // Handle possible errors
                println("Error updating value: ${e.message}")
            }
    }

    private fun checkLoginState() {

        val dbRef =
            FirebaseDatabase.getInstance().getReference("Users").child("SS").child("isLogin")

        dbRef.get().addOnSuccessListener { data: DataSnapshot ->
            if (data.exists()) {
                val isLogin: Boolean = data.value as Boolean

                if (isLogin == true) {
                    startActivity(Intent(this, HomePageActivity::class.java))
                }

            }
        }.addOnFailureListener {
            println("faileddddd")
        }

    }

    data class Book(
        val bookName: String,
        val bookAuthor: String
    )
}