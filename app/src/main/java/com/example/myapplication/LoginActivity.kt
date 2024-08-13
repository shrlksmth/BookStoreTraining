package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.utility.Pd
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


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
                val progressDialog = ProgressDialog(this)
                Pd.showProgressDialog("Processing...", "Login in", progressDialog)

                val myRef = database.getReference("Users")
                myRef.child(userInputUserID.text.toString()).get().addOnSuccessListener {
                    if (it.exists()) {
                        val password = it.child("password").value.toString()
                        if (password == userInputPassword.text.toString()) {
                            updateLoginState(progressDialog)
                        } else {
                            Pd.dismissProgressDialog(progressDialog)
                            ToastUtil.showShortToast(this, "The password is wrong")
                        }
                    } else {
                        Pd.dismissProgressDialog(progressDialog)
                        ToastUtil.showShortToast(this, "The UserID doesn't exist")
                    }
                }.addOnFailureListener {
                    Pd.dismissProgressDialog(progressDialog)
                    ToastUtil.showShortToast(this, "Something Went Wrong")
                }
            }
        }
    }

    fun updateLoginState(progressDialog: ProgressDialog) {

        val dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child("SS")

        val updatedData = mapOf("isLogin" to true)
        dbRef.updateChildren(updatedData)
            .addOnSuccessListener {
                Pd.dismissProgressDialog(progressDialog)
                startActivity(Intent(this, HomePageActivity::class.java))
                ToastUtil.showShortToast(this, "Successfully Login")
            }
            .addOnFailureListener { e ->
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