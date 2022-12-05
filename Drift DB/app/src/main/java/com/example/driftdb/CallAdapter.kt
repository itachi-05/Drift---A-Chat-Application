package com.example.driftdb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class CallAdapter(val context: Context , private val ContactsList: ArrayList<ContactModel>): RecyclerView.Adapter<CallAdapter.MyViewHolder>() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int){
        }
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.call_logs_layout,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder , position: Int) {
        val currentItem = ContactsList[position]
        holder.myContactProfilePic.setImageBitmap(currentItem.userProfilePic)
        holder.myContactName.text = currentItem.userNameText
        holder.myCallBtn.setOnClickListener{
            callingPerson(currentItem.userNumberText)
        }
    }

    override fun getItemCount(): Int {
        return ContactsList.size
    }

    class MyViewHolder(itemView : View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val myContactProfilePic: ImageView = itemView.findViewById(R.id.userCallPic)
        val myContactName: TextView = itemView.findViewById(R.id.callUserName)
        val myCallBtn: ImageButton = itemView.findViewById(R.id.callButton)
    }
    private fun callingPerson(callingNumber: String) {
        val intent = Intent(Intent.ACTION_CALL , Uri.parse("tel:$callingNumber"))
        context.startActivity(intent)
    }
}