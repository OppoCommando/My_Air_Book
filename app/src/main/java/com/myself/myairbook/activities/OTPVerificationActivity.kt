package com.myself.myairbook.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.myself.myairbook.R
import com.myself.myairbook.databinding.ActivityOtpverificationBinding
import com.myself.myairbook.databinding.ActivityPhoneNumberVerificationBinding
import java.util.concurrent.TimeUnit

class OTPVerificationActivity : AppCompatActivity() {
    var binding : ActivityOtpverificationBinding? = null
    var auth: FirebaseAuth? = null
    var dialog : ProgressDialog? = null
    var phonecredential:PhoneAuthCredential?=null
    var verificationId:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this@OTPVerificationActivity)
        dialog!!.setMessage("Sending OTP Please wait...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        val phoneNumber  = intent.getStringExtra("phoneNumber")
        val countryphoneNumber="+91$phoneNumber"
        binding!!.phoneLble.setText("Verify $countryphoneNumber")

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(countryphoneNumber!!)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    dialog!!.dismiss()
                    phonecredential=credential
                  //  signInWithPhoneAuthCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {


                    if (e is FirebaseAuthInvalidCredentialsException) {

                    } else if (e is FirebaseTooManyRequestsException) {

                    }


                }
                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                    binding!!.otpView.requestFocus()

                }
            })          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)


        binding!!.otpView.setOtpCompletionListener { otp ->
            binding!!.continueBtn01.setVisibility(View.GONE)
            dialog!!.setMessage("Verify OTP...")
            dialog!!.show()
            signInWithPhoneAuthCredential(otp!!)
        }
    }

    private fun signInWithPhoneAuthCredential(otp: String) {

        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialog!!.dismiss()
                    val intent = Intent(this@OTPVerificationActivity, SetUpProfileActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    binding!!.continueBtn01.setVisibility(View.VISIBLE)
                    Toast.makeText(this@OTPVerificationActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}