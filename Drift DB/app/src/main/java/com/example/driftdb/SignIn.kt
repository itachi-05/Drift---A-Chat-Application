package com.example.driftdb

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.driftdb.databinding.ActivitySignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit


class SignIn : AppCompatActivity(), FirebaseListener {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userphonenumber: String
    private lateinit var fullNumber: String
    private lateinit var countrycode: String
    private lateinit var myProgressBar: View
    private lateinit var sharedPref: SharedPreferences
    private lateinit var receivedPhoneNumber: String
    // permissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    //    private var isRecordPermissionGranted = false
    private var isReadContactsPermissionGranted = false
    private var isWriteContactsPermissionGranted = false
    private var isReadPermissionGranted = false
    private var isDialPermissionGranted = false
    private val permissionList: MutableList<String> = ArrayList()

    // FirebaseListener Interface Members
    override fun onSuccess(flag: Boolean, numberCheck: String, userName: String) {
        if (flag) {
//            Log.i("Checking 1", numberCheck)
            val intent = Intent(this , MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        else {
//            Log.i("Checking 2", numberCheck)
            val intent = Intent(this , Username::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null){
            sharedPref = getSharedPreferences("sharingDataUsingSP" , MODE_PRIVATE)
            receivedPhoneNumber = sharedPref.getString(
                "userPhoneNumber" ,
                "Error! Not Found"
            ).toString()
            startActivity(Intent(this,MainActivity::class.java))
//            val numberCheck = auth.currentUser!!.phoneNumber.toString()
//            Log.i("USER","LOGGED IN WITH $numberCheck")
//            NO NEED TO CALL READ_DATA
//            readData(receivedPhoneNumber,this)
        }
    }
    private fun readData(numberCheck: String, listener: FirebaseListener){
//        Log.i("A",numberCheck)
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(numberCheck).get().addOnSuccessListener {
            if(it.exists()){
//                Log.i("B","OK")
                val databaseUserName = it.child("userName").value.toString()
                if(databaseUserName.isNotEmpty()){
//                    Log.i("C1","OK")
                    listener.onSuccess(true,numberCheck,"")
                }
                else{
//                    Log.i("C2","OK")
                    listener.onSuccess(false,numberCheck,"")
                }
            }
            else{
//                Log.i("D","OK")
                listener.onSuccess(false,numberCheck,"")
                Toast.makeText(baseContext,"User does not exist",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            listener.onSuccess(false,numberCheck,"")
//            Log.i("E","OK")
            Toast.makeText(baseContext,"Try again later",Toast.LENGTH_SHORT).show()
        }
//        Log.i("F","OK")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        })

        requestingPermissions()


        auth = FirebaseAuth.getInstance()
        myProgressBar = binding.progressBarLogin
        myProgressBar.visibility = View.INVISIBLE
        val country_code_list = arrayListOf<String>(
            "886","93","355","213","376","244","672","54","374","297","61",
            "43","994","973","880","375","32","501","229","975","591","599",
            "387","267","47","55","246","673","359","226","257","238","855",
            "237","1","236","235","56","86","852","853","61","61","57","269",
            "242","682","506","385","53","599","357","420","225","850","243",
            "45","253","593","20","503","240","291","372","268","251","500",
            "298","679","358","33","594","689","262","241","220","995","49",
            "233","350","30","299","590","502","44","224","245","592","509",
            "672","504","36","354","91","62","98","964","353","44","972","39",
            "81","44","962","7","254","686","965","996","856","371","961","266",
            "231","218","423","370","352","261","265","60","960","223","356",
            "692","596","222","230","262","52","691","377","976","382","212",
            "258","95","264","674","977","31","687","64","505","227","234",
            "683","672","47","968","92","680","507","675","595","51","63",
            "870","48","351","1","974","82","373","40","7","250","262","590",
            "290","590","508","685","378","239","966","221","381","248","232",
            "65","421","386","677","252","27","500","211","34","94","970",
            "249","597","47","46","41","963","992","66","389","670","228","690",
            "676","216","90","993","688","256","380","971","44","255","1","598",
            "998","678","58","84","681","212","967","260","263","358"
        )

//        Log.i("A1","")


        binding.otpBtn.setOnClickListener{
//            username = binding.userNameEditText.text.toString()
//            Log.i("username",username)
//            Log.i("username",username.length.toString())
            userphonenumber = binding.userPhoneNumber.text.toString()
            countrycode = binding.countryCode.text.toString()
//            Log.i("A2","")

//            if(!checkingUserName(username)){
//                Toast.makeText(baseContext,"Please enter valid Name", Toast.LENGTH_SHORT).show()
//            }
            if(!country_code_list.contains(countrycode)){
                Toast.makeText(baseContext, "Please enter valid Country Code", Toast.LENGTH_SHORT).show()
            }
            else if(userphonenumber.isNotEmpty()) {
                if (userphonenumber.length == 10) {
//                    Log.i("A3","")
//                    username = renamingUserName(username)
                    myProgressBar.visibility = View.VISIBLE
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    fullNumber = "+$countrycode$userphonenumber"
//                    uniqueID = fullNumber + "_" + username
//                    Log.i("UNIQUE ID",uniqueID)
                    val user = User(userPhoneNumber = fullNumber)
//                    Log.i("A4","")
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(fullNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

                    // adding to database
                    database.child(fullNumber).get().addOnSuccessListener {
                        if(!it.exists()){
                            database.child(fullNumber).setValue(user).addOnSuccessListener {
//                        binding.userNameEditText.text.clear()
                                binding.userPhoneNumber.text.clear()
                                binding.countryCode.text.clear()
                                Toast.makeText(baseContext, "Successfully Saved",Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener{
                                Toast.makeText(baseContext, "Failed",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(baseContext , "Try again later" , Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, "Please enter correct Number", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else{
                Toast.makeText(baseContext,"Please enter your Mobile Number", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun renamingUserName(username: String): String {
        var s = ""
        val n = username.length
        var lastIndex = 0
        for (i in n-1 downTo 0) {
            val c = username[i]
            if (c in 'a'..'z' || c in 'A'..'Z'){
                lastIndex = i
                break
            }
        }
        for(i in 0 until lastIndex+1){
            s += username[i]
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

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
//                    val user = task.result?.user
                    sendToMain()
                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                } else {
                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    Log.d(ContentValues.TAG , "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    Toast.makeText(baseContext, " ${task.exception.toString()}",Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(baseContext, "Enter valid Verification Code",Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }
    private fun sendToMain(){
        val intent = Intent(this, MainActivity::class.java)
//         set the new task and clear flags
//        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
//            Log.d(TAG, "onVerificationCompleted:$credential")
//            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
//            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(baseContext, "Invalid Request, try again",Toast.LENGTH_SHORT).show()
//                Log.d("TAG","onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(baseContext, "SMS quota for the project exceeded",Toast.LENGTH_SHORT).show()
//                Log.d("TAG","onVerificationFailed: ${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent( verificationId: String, token: PhoneAuthProvider.ForceResendingToken ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            Log.d(TAG, "onCodeSent:$verificationId")
//            TRY TO PAUSE MOVING TO NEXT STEP, IF USER ACCOUNT IS NOT VALID
//            ########################  signInWithPhoneAuthCredential(credential) : my personal checking ########################
//            Save verification ID and resending token so we can use them later
//            storedVerificationId = verificationId
//            resendToken = token
            val intent = Intent(applicationContext, OtpAct::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)

            sharedPref = getSharedPreferences("sharingDataUsingSP" , MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("userPhoneNumber" , fullNumber)
            editor.apply()

            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            myProgressBar.visibility = View.INVISIBLE
        }
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