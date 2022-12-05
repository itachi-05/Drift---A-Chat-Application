package com.example.driftdb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(val context: Context , private val ContactsList: ArrayList<ContactModel>): RecyclerView.Adapter<ContactAdapter.MyViewHolder>() {
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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contacts_item,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder , position: Int) {
        val currentItem = ContactsList[position]
        holder.myContactProfilePic.setImageBitmap(currentItem.userProfilePic)
        holder.myContactName.text = currentItem.userNameText
        holder.myContactNumber.text = currentItem.userNumberText
        holder.myChatButton.setOnClickListener{
            val intent = Intent(context, FullChats::class.java)
            sharedPref = context.getSharedPreferences("sharingChatsData",AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("userName" , currentItem.userNameText)
            editor.putString("userPhoneNumber",currentItem.userNumberText)
            editor.apply()
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return ContactsList.size
    }

    class MyViewHolder(itemView : View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val myContactProfilePic: ImageView = itemView.findViewById(R.id.contactsProfileImage)
        val myContactName: TextView = itemView.findViewById(R.id.contactsName)
        val myContactNumber: TextView = itemView.findViewById(R.id.contactsNumber)
        val myChatButton: ImageButton  = itemView.findViewById(R.id.clickToChatBtn)
    }
}