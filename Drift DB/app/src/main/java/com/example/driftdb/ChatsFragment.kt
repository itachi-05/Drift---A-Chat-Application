package com.example.driftdb


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driftdb.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class ChatsFragment : Fragment(R.layout.fragment_chats) {
    private var binding: FragmentChatsBinding?= null
    private val bind get() = binding!!
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private lateinit var userName: String
    private lateinit var userPhoneNumber: String
    // recycler view stuff
    private lateinit var msgRecyclerView: RecyclerView
    private lateinit var msgArrayList: ArrayList<Users>
    private lateinit var msgUserImageID: Array<Int>
    private lateinit var msgUserName: Array<String>
    private lateinit var msgLastMsg: Array<String>
    private lateinit var msgLastMsgTime: Array<String>
    // copied lateinits from ContactsPage.kt
    private lateinit var contactModelArrayList: ArrayList<ContactModel>
    private lateinit var userModelArrayList: ArrayList<Users>
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var usersAdapter: UserAdapter
    private val bothNameNumber: MutableMap<String, String> = HashMap()
    private var contactsDuplicateCheckMap = mutableMapOf<String, Int>()
    private val nameList: ArrayList<String> = ArrayList()
    private val numberList: ArrayList<String> = ArrayList()
    // message uids
    private var receiverRoom: String?= null
    private var senderRoom: String?= null
    private lateinit var storageReference: StorageReference
    // permissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    //    private var isRecordPermissionGranted = false
    private var isReadContactsPermissionGranted = false
    private var isWriteContactsPermissionGranted = false
    private var isReadPermissionGranted = false
    private val permissionList: MutableList<String> = ArrayList()



    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup? , savedInstanceState: Bundle? ): View {
        // Inflate the layout for this fragment
        requestingPermissions()
        auth = FirebaseAuth.getInstance()
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().getReference("Users")

        sharedPref = requireActivity().getSharedPreferences("sharingDataUsingSP", AppCompatActivity.MODE_PRIVATE)
        userPhoneNumber = sharedPref.getString(
            "userPhoneNumber" ,
            "Error! Not Found"
        ).toString()
        // *********************************************************************************************


        binding!!.contactsButton.setOnClickListener{
//            Toast.makeText(activity, "Coming Soon...",Toast.LENGTH_SHORT).show()
            startActivity(Intent(activity, ContactsPage::class.java))
        }



        msgUserImageID = arrayOf(
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e
        )
        msgUserName = arrayOf(
            "Joseph Grant", "Barbara Vance", "Erin Dixon", "Tara Smith", "Megan Rollins",
            "Ernest Howe", "Margaret Brown", "Joshua Andersen", "Jason Ramsey", "Alyssa Ford"
        )
        msgLastMsg = arrayOf(
            "Hi","Hi","Hi","Hi","Hi","Hi","Hi","Hi","Hi","Hi"
        )
        msgLastMsgTime = arrayOf(
            "8 am","8 am","8 am","8 am","8 am","8 am","8 am","8 am","8 am","8 am"
        )


//        contactModelArrayList = ArrayList()
        userModelArrayList = ArrayList()
//        contactsRecyclerView = binding!!.chatsRecyclerView
        usersRecyclerView = binding!!.chatsRecyclerView
//        contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        usersRecyclerView.layoutManager = LinearLayoutManager(context)
//        contactsRecyclerView.setHasFixedSize(true)
        usersRecyclerView.setHasFixedSize(true)


//        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"       -----------------------> let sort be defined later
        val phones: Cursor? = activity?.contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phoneNumber = reorderingPhoneNumber(phoneNumber)
            val ok = checkValidPhoneNumber(phoneNumber)

            if(!contactsDuplicateCheckMap.containsKey(phoneNumber) && ok && phoneNumber!=auth.currentUser?.phoneNumber){
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
        db = FirebaseDatabase.getInstance().getReference("Users")
        db.child(numberCheck).get().addOnSuccessListener {
            if(it.exists()){
                hasAnyMessages(numberCheck,name)
            }
        }.addOnFailureListener{
            Toast.makeText(activity,"Try again later",Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasAnyMessages(receiverNumber: String , name: String) {
        val senderNumber = auth.currentUser?.phoneNumber
        senderRoom = receiverNumber + senderNumber
        receiverRoom = senderNumber + receiverNumber

        db = FirebaseDatabase.getInstance().getReference("Users").child("All MESSAGES").child(receiverRoom!!)
        // we have to check if the senderRoom exists in database or not

        db.get().addOnSuccessListener {
            if(it.exists()){
                // here lastMsgText will be the last message of ChatsRecyclerView
                // ************************ USING DB TO RETRIEVE THE LAST MESSAGE ************************
                var msg = ""
                var num = ""
                var msgCount = 0
                for(m in it.child("messages").children){
                    msg = m.getValue(Messages::class.java)?.message.toString()
                    num = m.getValue(Messages::class.java)?.senderNumber.toString()
                    msgCount += 1
                }

//                val gson = Gson()
//                sharedPref = requireActivity().getSharedPreferences("sharingChatsReadData", AppCompatActivity.MODE_PRIVATE)
//                val readCount = sharedPref.getString("totalReadMsg" ,"Error!")
//                Log.i("............",readCount.toString())
//                val type = object : TypeToken<HashMap<String,String>>() {}.type
////                val rhMap: HashMap<String , String> = gson.fromJson(readCount , type)
//
//
////                var count = (msgCount - rhMap[senderRoom].toInt()).toString()
//                var count = ""
//                if(readCount == "Error!"){
//                    count = ""
//                }
                // ************************* setting profile pic *************************
                storageReference = FirebaseStorage.getInstance().reference.child("images/$receiverNumber")
                val localFile = File.createTempFile("tempImage","")
                storageReference.getFile(localFile).addOnSuccessListener {
                    if(num == senderNumber){
                        msg = "You: $msg"
                    }
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    val userModel = Users(bitmap,name,receiverNumber,msg,"","")
                    userModelArrayList.add(userModel)
                    usersAdapter = UserAdapter(requireActivity(), userModelArrayList)
                    usersRecyclerView.adapter = usersAdapter
                    usersAdapter.setOnItemClickListener(object : UserAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            super.onItemClick(position)
                        }
                    })
                }.addOnFailureListener{
                    if(num == senderNumber){
                        msg = "You: $msg"
                    }
//                    val myLogo: Bitmap? = BitmapFactory.decodeResource(requireContext().resources , R.drawable.ic_baseline_person_24)
                    val myLogo = (ResourcesCompat.getDrawable(this.resources, R.drawable.ic_baseline_person_24, null) as VectorDrawable).toBitmap()
                    val userModel = Users(myLogo ,name,receiverNumber,msg,"","")
                    userModelArrayList.add(userModel)
                    usersAdapter = UserAdapter(requireActivity(), userModelArrayList)
                    usersRecyclerView.adapter = usersAdapter
                    usersAdapter.setOnItemClickListener(object : UserAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            super.onItemClick(position)
                        }
                    })
                    Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
                }
                // ************************* setting profile pic *************************

                // ************************ USING DB TO RETRIEVE THE LAST MESSAGE ************************
            }
        }.addOnFailureListener {
            Toast.makeText(activity , "Try again later" , Toast.LENGTH_SHORT).show()
        }
    }


    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
    }

    // to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


    // PERMISSIONS
    private fun requestingPermissions() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isWriteContactsPermissionGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] ?: isWriteContactsPermissionGranted
            isReadContactsPermissionGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: isReadContactsPermissionGranted
        }
        requestPermission()
    }
    private fun requestPermission(){
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isReadContactsPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isWriteContactsPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED


        if(!isReadPermissionGranted){
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!isReadContactsPermissionGranted){
            permissionList.add(android.Manifest.permission.READ_CONTACTS)
        }
        if(!isWriteContactsPermissionGranted){
            permissionList.add(android.Manifest.permission.WRITE_CONTACTS)
        }

        if(permissionList.isNotEmpty()){
            permissionLauncher.launch(permissionList.toTypedArray())
        }

    }
}