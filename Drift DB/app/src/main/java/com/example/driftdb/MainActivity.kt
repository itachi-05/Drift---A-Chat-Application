package com.example.driftdb


import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.driftdb.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var userNameView: TextView
    private lateinit var userProfileImage: ImageView
    private lateinit var userPhoneNumber: String
    private lateinit var userPhoneNumberView: TextView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var sharedPref: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var builder: AlertDialog.Builder
    private lateinit var storageReference: StorageReference
    private lateinit var tabLayout: TabLayout
    private lateinit var myViewPager: ViewPager2



    // permissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    //    private var isRecordPermissionGranted = false
    private var isReadContactsPermissionGranted = false
    private var isWriteContactsPermissionGranted = false
    private var isReadPermissionGranted = false
    private var isDialPermissionGranted = false
    private val permissionList: MutableList<String> = ArrayList()


    // FirebaseListener Interface Members
//    override fun onSuccess(flag: Boolean, numberCheck: String, userName: String) {
//        if (!flag) {
////            Log.i("Checking 1", numberCheck)
//            val intent = Intent(this , Username::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(intent)
//        }
//    }
//    override fun onStart() {
//        super.onStart()
//        var receivedPhoneNumber = ""
//        if(auth.currentUser != null){
//            sharedPref = getSharedPreferences("sharingDataUsingSP" , MODE_PRIVATE)
//            receivedPhoneNumber = sharedPref.getString(
//                "userPhoneNumber" ,
//                "Error! Not Found"
//            ).toString()
//            readData(receivedPhoneNumber,this)
//        }
//    }
//    private fun readData(numberCheck: String, listener: FirebaseListener){
////        Log.i("A",numberCheck)
//        database = FirebaseDatabase.getInstance().getReference("Users")
//        database.child(numberCheck).get().addOnSuccessListener {
//            if(it.exists()){
////                Log.i("B","OK")
//                val databaseUserName = it.child("userName").value.toString()
//                if(databaseUserName.isNotEmpty()){
////                    Log.i("C1","OK")
//                    listener.onSuccess(true,numberCheck,"")
//                }
//                else{
////                    Log.i("C2","OK")
//                    listener.onSuccess(false,numberCheck,"")
//                }
//            }
//            else{
////                Log.i("D","OK")
//                listener.onSuccess(false,numberCheck,"")
//                Toast.makeText(baseContext,"User does not exist",Toast.LENGTH_SHORT).show()
//            }
//        }.addOnFailureListener{
//            listener.onSuccess(false,numberCheck,"")
////            Log.i("E","OK")
//            Toast.makeText(baseContext,"Try again later",Toast.LENGTH_SHORT).show()
//        }
////        Log.i("F","OK")
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        init()
        requestingPermissions()


        val drawerLayout = binding.drawerLayout
        binding.navBarButton.setOnClickListener{
            drawerLayout.openDrawer(GravityCompat.START)
            val navView = binding.navView

            toggle = ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.isDrawerSlideAnimationEnabled = true
            toggle.syncState()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
            navView.setNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.nav_modes -> {
//                        Toast.makeText(baseContext, "Modes", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(this, Modes::class.java)
//                        startActivity(intent)
                        Toast.makeText(this,"Be Happy",Toast.LENGTH_SHORT).show()
                    }
                    R.id.Profile -> {
                        Toast.makeText(baseContext, "Account", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Profile::class.java)
                        startActivity(intent)
                    }
                    R.id.Themes -> Toast.makeText(baseContext, "Themes", Toast.LENGTH_SHORT).show()
                    R.id.Log_out -> {
                        confirmingLogOut()
                    }
                    R.id.Account -> Toast.makeText(baseContext, "Account", Toast.LENGTH_SHORT).show()
                    R.id.nav_share -> Toast.makeText(baseContext, "Share", Toast.LENGTH_SHORT).show()
                    R.id.contact_us -> Toast.makeText(baseContext, "Contact Us", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }


    private fun init(){
        auth = FirebaseAuth.getInstance()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        })
        // handling pages using swipe -> viewpager
        tabLayout = findViewById(R.id.tabLayout)
        myViewPager = findViewById(R.id.MyViewPager)
        myViewPager.adapter = MyAdapter(this)
        TabLayoutMediator(tabLayout,myViewPager){ tab, index ->
            tab.text = when(index){
                0 -> { "Chats" }
                1 -> { "Status" }
                2 -> { "Calls" }
                else -> { throw Resources.NotFoundException("Position not found") }
            }
        }.attach()
//        tabLayout.setTabTextColors()


        // initializing all View's
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val header: View = navigationView.getHeaderView(0)
        userNameView = header.findViewById(R.id.navheader_userName)
        userProfileImage = header.findViewById(R.id.circularImageView)
        userPhoneNumberView = header.findViewById(R.id.navheader_userPhoneNumber)

        // calling shared Preferences
        sharedPref = getSharedPreferences("sharingDataUsingSP",MODE_PRIVATE)
        userPhoneNumber = sharedPref.getString(
            "userPhoneNumber" ,
            "Error! Not Found"
        ).toString()
        // getting username from database
        readData(userPhoneNumber)
        userPhoneNumberView.text = userPhoneNumber
        getProfilePic(userPhoneNumber)
    }

    private fun getProfilePic(userNumber: String) {
        storageReference = FirebaseStorage.getInstance().reference.child("images/$userNumber")
        val localfile = File.createTempFile("tempImage","")
        storageReference.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            userProfileImage.setImageBitmap(bitmap)
        }.addOnFailureListener{
//             Image can not be loaded when user login's for first time
//            Toast.makeText(baseContext, "Failed...", Toast.LENGTH_SHORT).show()
            Log.i("GET PROFILE PIC","CAN NOT BE LOADED or IS NOT UPLOADED")
        }
    }

    private fun readData(numberCheck: String){
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(numberCheck).get().addOnSuccessListener {
            if(it.exists()){
                val databaseUserName = it.child("userName").value.toString()
                if(databaseUserName.isNotEmpty()){
                    userNameView.text = databaseUserName
                }
            }
            else{
                startActivity(Intent(this, SignIn::class.java))
                Toast.makeText(baseContext,"User does not exist",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(baseContext,"Try again later",Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmingLogOut() {
        builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.alert_message))
            .setMessage("Do you wish to Log Out?")
            .setCancelable(true)
            .setPositiveButton("Yes"){dialogInterface, it ->
                logout()
            }
            .setNegativeButton("No"){dialogInterface, it ->
                dialogInterface.cancel()
            }
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, SignIn::class.java)
        // set the new task and clear flags
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }



    // PERMISSIONS
    private fun requestingPermissions() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isWriteContactsPermissionGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] ?: isWriteContactsPermissionGranted
            isReadContactsPermissionGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: isReadContactsPermissionGranted
            isDialPermissionGranted = permissions[android.Manifest.permission.CALL_PHONE] ?: isDialPermissionGranted
        }
        requestPermission()
    }
    private fun requestPermission(){
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isReadContactsPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isWriteContactsPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isDialPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CALL_PHONE
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
        if(!isDialPermissionGranted){
            permissionList.add(android.Manifest.permission.CALL_PHONE)
        }

        if(permissionList.isNotEmpty()){
            permissionLauncher.launch(permissionList.toTypedArray())
        }

    }
}