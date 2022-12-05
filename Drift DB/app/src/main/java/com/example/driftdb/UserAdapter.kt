package com.example.driftdb

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class UserAdapter(val context: Context , private val usersList: ArrayList<Users>): RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chats_list_item,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: MyViewHolder , position: Int) {
        val currentItem = usersList[position]
        holder.myUserProfilePic.setImageBitmap(currentItem.userProfilePic)
        holder.myUserName.text = currentItem.userNameText
        holder.myUserLastMsgTxt.text = currentItem.lastMsgText
        holder.myUserLastMsgTxtTime.text = currentItem.lastMsgTime
        holder.myUnreadMessages.text = currentItem.unreadMsg
        holder.myChatButton.setOnClickListener{
            val intent = Intent(context, FullChats::class.java)
            sharedPref = context.getSharedPreferences("sharingChatsData",AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("userName" , currentItem.userNameText)
            editor.putString("userPhoneNumber",currentItem.userPhoneNUmber)
            editor.apply()
            context.startActivity(intent)
        }
        holder.myUserProfilePic.setOnClickListener{
//            val stream = ByteArrayOutputStream()
//            val byteArray = stream.toByteArray()
//            val bmp = BitmapFactory.decodeStream(ByteArrayInputStream(stream.toByteArray()))
//            bmp.compress(Bitmap.CompressFormat.PNG , 100 , stream)
//            val i = Intent(context , UserImageZoom::class.java)
//            i.putExtra("image" , byteArray)
//            context.startActivity(i)
            Toast.makeText(context,"Later",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    class MyViewHolder(itemView : View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val myUserProfilePic: ImageView = itemView.findViewById(R.id.userProfilePic)
        val myUserName: TextView = itemView.findViewById(R.id.userNameText)
        val myUserLastMsgTxt: TextView = itemView.findViewById(R.id.lastMsgText)
        val myUserLastMsgTxtTime: TextView = itemView.findViewById(R.id.lastMsgTime)
        val myUnreadMessages: TextView = itemView.findViewById(R.id.msgCount)
        val myChatButton: ImageButton  = itemView.findViewById(R.id.chatsBtn)
    }
}