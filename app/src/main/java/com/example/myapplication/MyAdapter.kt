package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.yourapp.utils.ToastUtil

class MyAdapter(private val bookList: ArrayList<bookDataClass>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        println(bookList.size)
        return bookList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]

        holder.bookName.text = currentItem.bookName
        holder.bookAuthor.text = currentItem.bookAuthor
        holder.bookDate.text = currentItem.bookDate
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val bookName: TextView = itemView.findViewById(R.id.bookName)
        val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookDate: TextView = itemView.findViewById(R.id.bookDate)

    }
}