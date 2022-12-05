package com.example.driftdb


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driftdb.databinding.ActivityFullChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson


open class FullChats : AppCompatActivity() {
    private lateinit var binding: ActivityFullChatsBinding
    lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Messages>
    private lateinit var sharedPref: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private var receiverRoom: String?= null
    private var senderRoom: String?= null
    private lateinit var db: DatabaseReference
    private var hMp = HashMap<String, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("Users")
        handlingBackButton()


        sharedPref = getSharedPreferences("sharingChatsData",MODE_PRIVATE)
        val receiverName = sharedPref.getString("userName" , "Error! Not Found").toString()
        val receiverNumber = sharedPref.getString("userPhoneNumber" , "Error! Not Found").toString()
        binding.UserNameButton.text = receiverName

        val senderNumber = auth.currentUser?.phoneNumber
        senderRoom = receiverNumber + senderNumber
        receiverRoom = senderNumber + receiverNumber

        // call btn
        binding.phoneCallButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_CALL , Uri.parse("tel:$receiverNumber"))
            startActivity(intent)
        }
        // video call btn


        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)


        // logic for adding data to recyclerView
        db.child("All MESSAGES").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    var count = 0
                    for(ss in snapshot.children){
                        val msg = ss.getValue(Messages::class.java)
                        messageList.add(msg!!)
                        count++
                    }

                    val gson = Gson()
                    hMp[senderRoom.toString()] = count.toString()
                    sharedPref = getSharedPreferences("sharingChatsReadData", AppCompatActivity.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    val json = gson.toJson(hMp)
//                    Log.i("checking json",json.toString())
                    editor.putString("totalReadMsg" , json)
                    editor.apply()

                    messageAdapter.notifyDataSetChanged()
                    binding.personalChatsRecyclerView.post {
                        binding.personalChatsRecyclerView.scrollToPosition(
                            messageAdapter.itemCount - 1
                        )
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(baseContext, "DB ERROR",Toast.LENGTH_SHORT).show()
                }
            })



        // adding the message to Database
        binding.sentButton.setOnClickListener{
            val msg = binding.typeMessageText.text
            if(checkingUserName(msg.toString())){
                val msgObj = Messages(msg.toString(), senderNumber,"NA")
                db.child("All MESSAGES").child(senderRoom!!).child("messages").push().setValue(msgObj).addOnSuccessListener {
                    db.child("All MESSAGES").child(receiverRoom!!).child("messages").push().setValue(msgObj)
                    messageAdapter.notifyDataSetChanged()
                }
            }
            binding.typeMessageText.setText("")
        }

        binding.personalChatsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.personalChatsRecyclerView.adapter = messageAdapter
    }

    private fun checkingUserName(userName: String): Boolean {
        val n = userName.length
        var flag = 0
        for(i in 0 until n){
            val c = userName[i]
            if (c != ' ') flag = 1
        }
        return flag == 1
    }

    private fun handlingBackButton() {
        binding.backFromContactsButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

}