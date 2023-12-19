package com.example.letmeknow.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.letmeknow.R
import com.example.letmeknow.data.RecyclerViewData

class MyPollsRVAdapter(
    private val onDeleteClick: (String) -> Unit,
    private val onPollClick: (String) -> Unit
) : ListAdapter<RecyclerViewData, MyPollsRVAdapter.MyViewHolder>(MyDiffCallback()) {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val cvPoll: CardView = itemView.findViewById(R.id.cvPoll)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_my_polls, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = getItem(position)

        holder.tvQuestion.text = currentItem.question
        holder.tvAuthor.text = currentItem.author

        if (currentItem.expired != null) {
            if (currentItem.expired == true) {
                holder.tvStatus.text = "Status: Active"
                Log.d("DDDDDDDDDDDDDDDDDDDDDDDDDD", "WWWWWWWWWWWWWWWWWWW")
            } else {
                holder.tvStatus.text = "Status: Expired"
                Log.d("DDDDDDDDDDDDDDDDDDDDDDDDDD", "XXXXXXXXXXXXXXXXXXXXXXXXXXX")
            }
        } else {
            holder.tvStatus.text = "Status: Null"
            Log.d("DDDDDDDDDDDDDDDDDDDDDDDDDD", "YYYYYYYYYYYYYYYYYYYY")
        }

        holder.cvPoll.setOnClickListener {
            onPollClick.invoke(currentItem.pollId)
        }

        holder.btnRemove.setOnClickListener {
            onDeleteClick.invoke(currentItem.pollId)
        }
    }

    // DiffCallback for efficient updates
    private class MyDiffCallback : DiffUtil.ItemCallback<RecyclerViewData>() {
        override fun areItemsTheSame(
            oldItem: RecyclerViewData,
            newItem: RecyclerViewData
        ): Boolean {
            return oldItem.pollId == newItem.pollId
        }

        override fun areContentsTheSame(
            oldItem: RecyclerViewData,
            newItem: RecyclerViewData
        ): Boolean {
            return oldItem == newItem
        }
    }
}