package com.example.driftdb


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driftdb.databinding.ActivityContactsPageBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ContactsPage : AppCompatActivity() {
    private lateinit var binding: ActivityContactsPageBinding
    private lateinit var contactModelArrayList: ArrayList<ContactModel>
    private lateinit var tempArrayList: ArrayList<ContactModel>
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var sharedPref: SharedPreferences
    private val bothNameNumber: MutableMap<String, String> = HashMap()
    private var contactsDuplicateCheckMap = mutableMapOf<String, Int>()
    private val nameList: ArrayList<String> = ArrayList()
    private val numberList: ArrayList<String> = ArrayList()
    private lateinit var resultantList: Map<Any?,Any?>
    private lateinit var storageReference: StorageReference


    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        handlingBackButton()
//        binding.contactsSearchButton.setOnClickListener{
//            val intent = Intent(this, ContactsSearchingKT::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
//            startActivity(intent)
//        }

        contactModelArrayList = ArrayList()
        tempArrayList = ArrayList()

        contactsRecyclerView = binding.contactsRecyclerView
        contactsRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        contactsRecyclerView.setHasFixedSize(true)


        val phones: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_menu_item, menu)
        val item = menu?.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("HEHEHEH")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if(searchText.isNotEmpty()){
                    contactModelArrayList.forEach{
                        if(it.userNameText.lowercase(Locale.getDefault()).contains(searchText)){
                            tempArrayList.add(it)
                        }
                    }
                    contactsRecyclerView.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(contactModelArrayList)
                    contactsRecyclerView.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun sharedPrefInit() {
        sharedPref = getSharedPreferences("sharingContactsDataUsingSP" , MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("bothNameNumber",null)
        val json2 = sharedPref.getString("userName",null)
        val type: Type = object : TypeToken<MutableMap<String , String>?>() {}.type
        val type2: Type = object : TypeToken<ArrayList<String>?>() {}.type

        val myHashMapList = gson.fromJson<Any?>(json, type) as MutableMap<* , *>
        val myUserNameList = gson.fromJson<Any>(json2, type2) as ArrayList<*>
        resultantList = HashMap()
        resultantList = myHashMapList.toList().sortedBy { (_, value) -> value.toString()}.toMap()

        Log.i("PRINTING OUTPUT:::",resultantList.toString())
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
                    contactAdapter = ContactAdapter(this,contactModelArrayList)
                    contactsRecyclerView.adapter = contactAdapter
                    contactAdapter.setOnItemClickListener(object : ContactAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            super.onItemClick(position)
                            startActivity(Intent(baseContext,FullChats::class.java))
                        }
                    })

                }.addOnFailureListener{
                    Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this,"Try again later",Toast.LENGTH_SHORT).show()
        }
    }


//    private fun addingModelTOList(contactModel: ContactModel) {
//        Log.i("ADTL FUN CALLED","hohohohohoh")
////        contactModelArrayList.add(contactModel)
//        contactModelArrayListDup = contactModelArrayList
//        Log.i("NODUP SIZE: ${contactModelArrayList.size}","DUP SIZE: ${contactModelArrayListDup.size}")
//    }


    private fun handlingBackButton() {
        binding.backFromContactsButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}