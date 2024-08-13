package com.example.myapplication

import android.net.Uri
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.util.Date

data class bookDataClass(
    var bookID: String? = null,
    var bookName: String? = null,
    var bookAuthor: String? = null,
    var bookNotes: String? = null,
    var bookDate: String? = null,
    var bookUrl: String? = null,
    var timeStamp: Long? = null,
    var imgUri: String? = null,
    var imgName: String? = null
)

