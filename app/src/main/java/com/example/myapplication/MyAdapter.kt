package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class MyAdapter(private val bookList: ArrayList<bookDataClass>, private val context: Context) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]

        holder.bookName.text = currentItem.bookName!!.uppercase()
        holder.bookAuthor.text = currentItem.bookAuthor!!.uppercase()
        holder.bookDate.text = currentItem.bookDate

        Picasso.get()
            .load(currentItem.bookUrl)
            .placeholder(R.drawable.baseline_image_search_24)
            .into(holder.bookPic)
    }

    fun removeItem(position: Int) {
        val itemId : String? = bookList[position].bookID
        println("item id $position : $itemId")

        bookList.removeAt(position)
        notifyItemRemoved(position)

        println("item id : $itemId")
        // Delete from Firebase
        FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book").child(itemId.toString()).removeValue().addOnSuccessListener {
            //deleteImage(bookList[position].imgUri!!)
        }
    }

    private fun deleteImage(imageName: String) {

        println(imageName)
        // Reference to the image file in Firebase Storage
        val ref: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(imageName)
            // Delete the file
        ref.delete().addOnSuccessListener {
            // File deleted successfully
            println("success")
        }.addOnFailureListener {
            // Error occurred during delete
            println("fail")

        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val bookName: TextView = itemView.findViewById(R.id.bookName)
        val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookDate: TextView = itemView.findViewById(R.id.bookDate)
        val bookPic: ImageView = itemView.findViewById(R.id.bookPic)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val intent = Intent(context, EditBookActivity::class.java)
                    intent.putExtra("name", bookList[position].bookName)
                    intent.putExtra("author", bookList[position].bookAuthor)
                    intent.putExtra("notes", bookList[position].bookNotes)
                    intent.putExtra("image", bookList[position].bookUrl)
                    intent.putExtra("id", bookList[position].bookID)
                    intent.putExtra("imgUrl", bookList[position].bookUrl)
                    intent.putExtra("imgUri", bookList[position].imgUri)
                    intent.putExtra("imgName", bookList[position].imgName)

                    context.startActivity(intent)
                }
            }
        }

    }




}