package com.example.driftdb

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.driftdb.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databseReference: DatabaseReference
    private lateinit var sharedPref: SharedPreferences
    private lateinit var phoneNumber: String
    private lateinit var storageReference: StorageReference

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            storageReference = FirebaseStorage.getInstance().getReference("images/$phoneNumber")
            if (it != null) {
                storageReference.putFile(it).addOnSuccessListener {
                    Toast.makeText(baseContext, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    getProfilePic()
                }.addOnFailureListener{
                    Toast.makeText(baseContext, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        sharedPref = getSharedPreferences("sharingDataUsingSP",MODE_PRIVATE)
        phoneNumber = sharedPref.getString(
            "userPhoneNumber" ,
            "Error! Not Found"
        ).toString()

        readData(phoneNumber)
        binding.changePhoneNumber.text = phoneNumber


        binding.saveInfoBtn.setOnClickListener{
            databseReference = FirebaseDatabase.getInstance().getReference("Users")
            val changedUserName = binding.enterYourName.text.toString()
            val changedAbout = binding.enterYourStatus.text.toString()

//            NO NEED OF USER() CLASS, TO SET DATA , WE JUST HAVE TO UPDATE CHILDREN
//            val user = User(userName=changedUserName,userPhoneNumber=phoneNumber,status=changedAbout)
            databseReference.child(phoneNumber).updateChildren(
                mapOf(
                    "userName" to changedUserName,
                    "userPhoneNumber" to phoneNumber,
                    "status" to changedAbout
                )
            ).addOnCompleteListener{
                if(it.isSuccessful){
                    Toast.makeText(baseContext, "Saved Successfully", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(baseContext, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }

        uploadProfilePic()   // new upload
        getProfilePic() // access profile pic

    }

    private fun readData(numberCheck: String){
        databseReference = FirebaseDatabase.getInstance().getReference("Users")
        databseReference.child(numberCheck).get().addOnSuccessListener {
            if(it.exists()){
                val databaseUserName = it.child("userName").value.toString()
                if(databaseUserName.isNotEmpty()){
                    binding.enterYourName.setText(databaseUserName)
                }
                val databaseStatus = it.child("status").value.toString()
                if(databaseStatus.isNotEmpty()){
                    binding.enterYourStatus.setText(databaseStatus)
                }
            }
            else{
//                startActivity(Intent(this, SignIn::class.java))
                Toast.makeText(baseContext,"User does not exist",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(baseContext,"Try again later",Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePic() {
        binding.changeImageBtn.setOnClickListener{
            getImage.launch("image/*")
        }
        getProfilePic()
    }

    private fun getProfilePic() {
        storageReference = FirebaseStorage.getInstance().reference.child("images/$phoneNumber")
        val localfile = File.createTempFile("tempImage","")
        storageReference.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.userProfileImage.setImageBitmap(bitmap)
        }.addOnFailureListener{
//             Image can not be loaded when user login's for first time
//            Toast.makeText(baseContext, "Failed...", Toast.LENGTH_SHORT).show()
            Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
        }
    }
}