package com.example.driftdb

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserStatusAdapter(val context: Context, private val statusList: ArrayList<StatusModel>): RecyclerView.Adapter<UserStatusAdapter.MyViewHolder>() {

    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int){

        }
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.status_item_layout,parent,false)
        return MyViewHolder(itemView , mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder , position: Int) {
        val currentItem = statusList[position]
        holder.statusImg.setImageResource(currentItem.userStatus)
        holder.statusUserName.text = currentItem.statusUserName
        holder.statusOpen.setOnClickListener{
            context.startActivity(Intent(context,Profile::class.java))
        }
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    class MyViewHolder(itemView: View , listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        val statusImg: ImageView = itemView.findViewById(R.id.userStatusPic)
        val statusUserName: TextView = itemView.findViewById(R.id.userStatusPic)
        val statusOpen: ImageButton = itemView.findViewById(R.id.statusOpenBtn)
    }
}