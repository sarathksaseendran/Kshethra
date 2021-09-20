package com.sarathk.sk.kshethra.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.sarathk.sk.kshethra.BuildConfig
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.ui.login.RegisterActivity
import com.sarathk.sk.kshethra.ui.main.MainActivity
import com.sarathk.sk.kshethra.ui.member.Gender
import com.sarathk.sk.kshethra.ui.member.Nakshatra
import com.sarathk.sk.kshethra.ui.member.Relation
import com.sarathk.sk.kshethra.utilities.Constants
import com.sarathk.sk.kshethra.utilities.FirestoreUtil
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    private var mAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Constants.VARIABLES.genderArrayList = ArrayList()
        Constants.VARIABLES.genderArrayList.add(Gender("M", "MALE"))
        Constants.VARIABLES.genderArrayList.add(Gender("F", "FEMALE"))
        Constants.VARIABLES.genderArrayList.add(Gender("U", "UNSPECIFIED"))

        Constants.VARIABLES.relationArrayList = ArrayList()
        Constants.VARIABLES.relationArrayList.add(Relation("H", "HUSBAND"))
        Constants.VARIABLES.relationArrayList.add(Relation("F", "FATHER"))

        Constants.VARIABLES.nakshatraArrayList = ArrayList()
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(1, "അശ്വതി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(2, "ഭരണി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(3, "കാർത്തിക"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(4, "രോഹിണി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(5, "മകയിരം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(6, "തിരുവാതിര"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(7, "പുണർതം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(8, "പൂയം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(9, "ആയില്യം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(10, "മകം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(11, "പൂരം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(12, "ഉത്രം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(13, "അത്തം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(14, "ചിത്തിര"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(15, "ചോതി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(16, "വിശാഖം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(17, "അനിഴം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(18, "തൃക്കേട്ട"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(19, "മൂലം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(20, "പൂരാടം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(21, "ഉത്രാടം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(22, "തിരുവോണം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(23, "അവിട്ടം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(24, "ചതയം"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(25, "പൂരുരുട്ടാതി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(26, "ഉത്രട്ടാതി"))
        Constants.VARIABLES.nakshatraArrayList.add(Nakshatra(27, "രേവതി"))

        supportActionBar?.hide()

        versionTextView.text = "Ver ${BuildConfig.VERSION_NAME}"

        mAuth = FirebaseAuth.getInstance()

//        val user = mAuth!!.currentUser
//
//        if (user != null) { // do your stuff
//        } else {
//            signInAnonymously()
//        }
//
//        val sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)
//
//        Handler().postDelayed({
//
//            if (sharedPreferences.getBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)) {
//                startActivity(Intent(applicationContext, MainActivity::class.java))
//                finish()
//            } else {
//                startActivity(Intent(applicationContext, LoginActivity::class.java))
//                finish()
//            }
//
//        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSigned()
        }, 800)
    }

    private fun checkUserSigned() {
        try {
            FirestoreUtil.getCurrentUser { user ->
                if (user != null) {
                    if (user.fullName.isEmpty()) {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Constants.VARIABLES.mobileNo = null
                    Constants.VARIABLES.fullName = null
                    Constants.VARIABLES.profilePicUrl = null
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }

        } catch (e: NullPointerException) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

//    private fun signInAnonymously() {
//        mAuth!!.signInAnonymously()
//            .addOnSuccessListener(this, OnSuccessListener<AuthResult?> {
//                // do your stuff
//            })
//            .addOnFailureListener(this,
//                OnFailureListener { exception ->
//                    Log.e("firebase", "signInAnonymously:FAILURE", exception)
//                })
//    }
}
