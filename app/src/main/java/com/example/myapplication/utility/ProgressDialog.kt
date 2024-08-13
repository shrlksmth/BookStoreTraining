package com.example.myapplication.utility

import android.app.ProgressDialog
import android.content.Context

object Pd {



//    fun showProgressDialog(message : String, title : String, context: Context){
//        val progressDialog = ProgressDialog(context)
//        progressDialog.setTitle(title)
//        progressDialog.setCancelable(false)
//        progressDialog.setMessage(message)
//        progressDialog.show()
//    }
//
//    fun dismissProgressDialog(context: Context){
//        ProgressDialog(context).dismiss()
//    }

    fun showProgressDialog(message : String, title : String, progressDialog: ProgressDialog){
        progressDialog.setTitle(title)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    fun dismissProgressDialog(progressDialog: ProgressDialog){
        progressDialog.dismiss()
    }

}