package com.example.driftdb


import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.driftdb.databinding.FragmentStatusBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class StatusFragment : Fragment() {
    private var binding: FragmentStatusBinding?= null
    private val bind get() = binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup? , savedInstanceState: Bundle? ): View {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        binding = FragmentStatusBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().getReference("Users")


        binding!!.clickToAddStatusBtn.setOnClickListener{
            uploadProfilePic()
            getProfilePic()
        }
        getProfilePic()

        // recycler view work

        return binding!!.root

    }

    private fun uploadProfilePic() {
        binding!!.clickToAddStatusBtn.setOnClickListener{
            getImage.launch("image/*")
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            storageReference = FirebaseStorage.getInstance().getReference("status/${auth.currentUser?.phoneNumber}")
            if (it != null) {
                storageReference.putFile(it).addOnSuccessListener {
                    Toast.makeText(activity, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    getProfilePic()
                }.addOnFailureListener{
                    Toast.makeText(activity, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    private fun getProfilePic() {
        storageReference = FirebaseStorage.getInstance().reference.child("status/${auth.currentUser?.phoneNumber}")
        val localfile = File.createTempFile("tempImage","")
        storageReference.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding!!.StatusPosition.setImageBitmap(bitmap)
        }.addOnFailureListener{
//             Image can not be loaded when user login's for first time
//            Toast.makeText(baseContext, "Failed...", Toast.LENGTH_SHORT).show()
            Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
        }
    }
}