package com.example.driftdb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth


class MessageAdapter(val context: Context, private var messageList: ArrayList<Messages>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_S: Int = 1
    val ITEM_R: Int = 2


    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): RecyclerView.ViewHolder {
        return  if(viewType == ITEM_S){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sent_layout,parent,false)
            SentViewHolder(view)
        }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.received_layout,parent,false)
            ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {
        val currentMessage = messageList[position]
        if(holder.javaClass == SentViewHolder::class.java){
            // do the stuff for sent view holder
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
        }
        else{
            // do the stuff for receive view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(FirebaseAuth.getInstance().currentUser?.phoneNumber == currentMessage.senderNumber){
            ITEM_S
        } else{
            ITEM_R
        }
    }

    override fun getItemCount(): Int = messageList.size


    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage: TextView = itemView.findViewById(R.id.sent_text_message)
    }
    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_text_message)
    }
}