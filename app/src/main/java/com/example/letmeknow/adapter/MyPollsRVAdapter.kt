package com.example.letmeknow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.letmeknow.R
import com.example.letmeknow.data.RecyclerViewData

//class MyPollsRVAdapter(private val pollList: ArrayList<RecyclerViewData>): RecyclerView.Adapter<MyPollsRVAdapter.MyViewHolder>() {
//
//    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
//        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
//        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_my_polls, parent, false)
//        return MyViewHolder(itemView)
//    }
//
//    override fun getItemCount(): Int {
//        return pollList.size
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val currentItem = pollList[position]
//        holder.tvQuestion.text = currentItem.question
//        holder.tvAuthor.text = currentItem.question
//    }
//
//}

class MyPollsRVAdapter : ListAdapter<RecyclerViewData, MyPollsRVAdapter.MyViewHolder>(MyDiffCallback()) {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_my_polls, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.tvQuestion.text = currentItem.question
        holder.tvAuthor.text = currentItem.author
    }

    // DiffCallback for efficient updates
    private class MyDiffCallback : DiffUtil.ItemCallback<RecyclerViewData>() {
        override fun areItemsTheSame(oldItem: RecyclerViewData, newItem: RecyclerViewData): Boolean {
            return oldItem.pollId == newItem.pollId
        }

        override fun areContentsTheSame(oldItem: RecyclerViewData, newItem: RecyclerViewData): Boolean {
            return oldItem == newItem
        }
    }
}