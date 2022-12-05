package com.example.driftdb

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.driftdb.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OtpAct : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verifyBtn: Button
    private lateinit var resendOtp: TextView
    private lateinit var inputOtp1: EditText
    private lateinit var inputOtp2: EditText
    private lateinit var inputOtp3: EditText
    private lateinit var inputOtp4: EditText
    private lateinit var inputOtp5: EditText
    private lateinit var inputOtp6: EditText
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var otpProgressBar: ProgressBar
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        sharedPref = getSharedPreferences("sharingDataUsingSP",MODE_PRIVATE)
        phoneNumber = sharedPref.getString(
            "userPhoneNumber" ,
            "Error! Not Found"
        ).toString()
//        Log.i("OTPACT_0",phoneNumber)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
//                val context = SignIn()
                val intent = Intent(this@OtpAct, SignIn::class.java)
                startActivity(intent)
            }
        })


        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!

        init()
        otpProgressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPvisibility()
        resendOtp.setOnClickListener{
            resendVerificationCode()
            resendOTPvisibility()
        }
        verifyBtn.setOnClickListener{
            // collect otp from all the edit texts
            val typedOTP = ( inputOtp1.text.toString() + inputOtp2.text.toString() + inputOtp3.text.toString()
                    + inputOtp4.text.toString() + inputOtp5.text.toString() + inputOtp6.text.toString() )

            if(typedOTP.isNotEmpty()){
                if(typedOTP.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    otpProgressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                }
                else{
                    Toast.makeText(this, "Please Enter Current Otp",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Please Enter The Otp",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOTPvisibility(){
        inputOtp1.setText("")
        inputOtp2.setText("")
        inputOtp3.setText("")
        inputOtp4.setText("")
        inputOtp5.setText("")
        inputOtp6.setText("")
        resendOtp.visibility = View.INVISIBLE
        resendOtp.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            resendOtp.visibility = View.VISIBLE
            resendOtp.isEnabled = true
        }, 10000)
    }

    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    otpProgressBar.visibility = View.VISIBLE
                    sendToUserName()
                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                } else {
                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    Log.d(ContentValues.TAG, "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    Toast.makeText(baseContext, " ${task.exception.toString()}",Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(baseContext, "Enter valid Verification Code",Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }

    private fun sendToUserName(){
        val intent = Intent(this, Username::class.java)
        // set the new task and clear flags
//        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
//        finish()
    }

    private fun init(){
        auth = FirebaseAuth.getInstance()
        otpProgressBar = binding.otpProgressBar
        verifyBtn = binding.verifyButton
        resendOtp = binding.resendOtpBtn
        inputOtp1 = binding.otpEditText1
        inputOtp2 = binding.otpEditText2
        inputOtp3 = binding.otpEditText3
        inputOtp4 = binding.otpEditText4
        inputOtp5 = binding.otpEditText5
        inputOtp6 = binding.otpEditText6
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
//                Log.d("TAG","onVerificationFailed: ${e.toString()}")
                Toast.makeText(baseContext, "Invalid Request, try again",Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
//                Log.d("TAG","onVerificationFailed: ${e.toString()}")
                Toast.makeText(baseContext, "SMS quota for the project exceeded",Toast.LENGTH_SHORT).show()
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            OTP = verificationId
            resendToken = token
        }
    }

    private fun addTextChangeListener(){
        inputOtp1.addTextChangedListener(EditTextWatcher(inputOtp1))
        inputOtp2.addTextChangedListener(EditTextWatcher(inputOtp2))
        inputOtp3.addTextChangedListener(EditTextWatcher(inputOtp3))
        inputOtp4.addTextChangedListener(EditTextWatcher(inputOtp4))
        inputOtp5.addTextChangedListener(EditTextWatcher(inputOtp5))
        inputOtp6.addTextChangedListener(EditTextWatcher(inputOtp6))
    }

    inner class EditTextWatcher(private val view: View): TextWatcher {
        override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {

        }

        override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            when (view) {
                binding.otpEditText1 -> if (text.length == 1) inputOtp2.requestFocus()
                binding.otpEditText2 -> if (text.length == 1) inputOtp3.requestFocus() else if (text.isEmpty()) inputOtp1.requestFocus()
                binding.otpEditText3 -> if (text.length == 1) inputOtp4.requestFocus() else if (text.isEmpty()) inputOtp2.requestFocus()
                binding.otpEditText4 -> if (text.length == 1) inputOtp5.requestFocus() else if (text.isEmpty()) inputOtp3.requestFocus()
                binding.otpEditText5 -> if (text.length == 1) inputOtp6.requestFocus() else if (text.isEmpty()) inputOtp4.requestFocus()
                binding.otpEditText6 -> if (text.isEmpty()) inputOtp5.requestFocus()
            }
        }
    }
}