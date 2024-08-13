package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourapp.utils.ToastUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class MyAdapter(private val bookList: ArrayList<bookDataClass>, private val context: Context) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
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

    fun removeBook(position: Int) {
        val bookID: String? = bookList[position].bookID
        val storageRef =
            FirebaseStorage.getInstance().getReferenceFromUrl(bookList[position].bookUrl.toString())

        FirebaseDatabase.getInstance().getReference("Users").child("SS").child("book")
            .child(bookID.toString()).removeValue().addOnSuccessListener {
                storageRef.delete().addOnSuccessListener {
//                bookList.removeAt(position)
//                notifyItemRemoved(position)
                    ToastUtil.showShortToast(context, "Success")
                }.addOnFailureListener {
                    ToastUtil.showShortToast(context, "Fail deleting the picture")
                }


            }.addOnFailureListener {
                ToastUtil.showShortToast(context, "Fail deleting the data")
            }
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val bookName: TextView = itemView.findViewById(R.id.bookName)
        val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookDate: TextView = itemView.findViewById(R.id.bookDate)
        val bookPic: ImageView = itemView.findViewById(R.id.bookPic)

        init {
            itemView.setOnClickListener {
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