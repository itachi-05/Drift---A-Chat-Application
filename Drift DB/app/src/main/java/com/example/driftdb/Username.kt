package com.example.driftdb

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.driftdb.databinding.ActivityUserNameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Username : AppCompatActivity() {
    private lateinit var binding: ActivityUserNameBinding
    private lateinit var userName: String
    private lateinit var userPhoneNumber: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        sharedPref = getSharedPreferences("sharingDataUsingSP",MODE_PRIVATE)
        userPhoneNumber = sharedPref.getString(
            "userPhoneNumber" ,
            "Error! Not Found"
        ).toString()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        })


        binding.nextButton.setOnClickListener{
            userName = binding.yourName.text.toString()
            userName = renamingUserName(userName)
//            Log.i("checking username",userName.length.toString())
            if(!checkingUserName(userName)){
                Toast.makeText(baseContext,"Please enter valid Name", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(baseContext,"Hello $userName", Toast.LENGTH_SHORT).show()
                updateData(userPhoneNumber)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }
    }

    private fun updateData(userPhoneNumber: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        val user = mapOf<String,String>(
            "userName" to userName
        )
//        Log.i("user phone number",userPhoneNumber)
        database.child(userPhoneNumber).updateChildren(user).addOnSuccessListener {
            binding.yourName.text.clear()
        }.addOnFailureListener{
            Toast.makeText(baseContext, "Failed to Update", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renamingUserName(userName: String): String {
        var s = ""
        val parts = userName.split(" ")
        val n = parts.size
        for(i in parts.indices){
            s += if(i != n-1) parts[i] + " " else parts[i]
        }
        return s
    }
    private fun checkingUserName(userName: String): Boolean {
        val n = userName.length
        for(i in 0 until n){
            val c = userName[i]
            if (c in 'a'..'z' || c in 'A'..'Z'){
                return true
            }
        }
        return false
    }
}