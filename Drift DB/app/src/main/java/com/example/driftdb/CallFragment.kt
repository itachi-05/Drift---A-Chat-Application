package com.example.driftdb


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driftdb.databinding.FragmentCallBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class CallFragment : Fragment() {
    private var binding: FragmentCallBinding?= null
    private val bind get() = binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var contactModelArrayList: ArrayList<ContactModel>
    private lateinit var callRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var callAdapter: CallAdapter
    private lateinit var sharedPref: SharedPreferences
    private val bothNameNumber: MutableMap<String, String> = HashMap()
    private var contactsDuplicateCheckMap = mutableMapOf<String, Int>()
    private val nameList: ArrayList<String> = ArrayList()
    private val numberList: ArrayList<String> = ArrayList()
    private lateinit var resultantList: Map<Any?,Any?>

    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup? , savedInstanceState: Bundle? ): View {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        binding = FragmentCallBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().getReference("Users")


        // contacts work
        contactModelArrayList = ArrayList()

        callRecyclerView = binding!!.callLogsRecyclerView
        callRecyclerView.layoutManager = LinearLayoutManager(activity)
        callRecyclerView.setHasFixedSize(true)


        val phones: Cursor? = activity?.contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phoneNumber = reorderingPhoneNumber(phoneNumber)
            val ok = checkValidPhoneNumber(phoneNumber)


            if(!contactsDuplicateCheckMap.containsKey(phoneNumber) && ok){
                contactsDuplicateCheckMap[phoneNumber] = 1
                setContactsData(phoneNumber,name)
            }
        }
        phones.close()

        return binding!!.root

    }


    private fun checkValidPhoneNumber(phoneNumber: String): Boolean {
        for(i in phoneNumber.indices){
            if(phoneNumber[i].isDigit() || phoneNumber[i]=='+'){
                continue
            }
            else{
                return false
            }
        }
        return true
    }
    private fun reorderingPhoneNumber(phoneNumber: String?): String {
        var s = ""
        if (phoneNumber != null) {
            for(i in phoneNumber.indices){
                if(phoneNumber[i] != ' '){
                    s += phoneNumber[i]
                }
            }
        }
        return s
    }

    private fun setContactsData(numberCheck: String,name: String){
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(numberCheck).get().addOnSuccessListener {
            if(it.exists()){
                storageReference = FirebaseStorage.getInstance().reference.child("images/$numberCheck")
                val localFile = File.createTempFile("tempImage","")
                storageReference.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    val contactModel = ContactModel(bitmap ,name,numberCheck)
                    contactModelArrayList.add(contactModel)
                    callAdapter = CallAdapter(requireActivity(),contactModelArrayList)
                    callRecyclerView.adapter = callAdapter
                    callAdapter.setOnItemClickListener(object : CallAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            super.onItemClick(position)
                        }
                    })
                }.addOnFailureListener{
                    val myLogo = (ResourcesCompat.getDrawable(this.resources, R.drawable.ic_baseline_person_24, null) as VectorDrawable).toBitmap()
                    val contactModel = ContactModel(myLogo ,name,numberCheck)
                    contactModelArrayList.add(contactModel)
                    callAdapter = CallAdapter(requireActivity(),contactModelArrayList)
                    callRecyclerView.adapter = callAdapter
                    callAdapter.setOnItemClickListener(object : CallAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            super.onItemClick(position)
                        }
                    })
                    Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
                }
            }
        }.addOnFailureListener{
            Toast.makeText(activity,"Try again later",Toast.LENGTH_SHORT).show()
        }
    }


}