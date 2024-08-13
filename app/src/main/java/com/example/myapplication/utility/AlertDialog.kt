package com.example.myapplication.utility

import android.app.AlertDialog.Builder

object AlertDialog {

     fun showAlertDialog(builder: Builder, title: String, message : String) {

        builder.setTitle("Alert Dialog Title")
        builder.setMessage("This is an alert dialog message.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}