package com.sarathk.sk.kshethra.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.familyheads.AddFamilyHeadsActivity
import com.sarathk.sk.kshethra.ui.familyheads.ListFamilyHeadsActivity
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.ui.member.AddMemberActivity
import com.sarathk.sk.kshethra.ui.member.ListMemberActivity
import com.sarathk.sk.kshethra.ui.user.AddUserActivity
import com.sarathk.sk.kshethra.ui.user.ListUserActivity
import com.sarathk.sk.kshethra.utilities.Constants
import com.sarathk.sk.kshethra.utilities.FirestoreUtil
import com.sarathk.sk.kshethra.utilities.StorageUtil
import com.sarathk.sk.kshethra.utilities.UtilityFunctions
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var permissionsList: MutableList<String>

    private val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        customTitle(resources.getString(R.string.app_name))

        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            permissionsList = ArrayList()
            if (!checkIfAlreadyhavePermission()) { requestForSpecificPermission() }
        }

        FirestoreUtil.getCurrentUser { user ->
            Constants.VARIABLES.mobileNo = user?.mobileNumber
            Constants.VARIABLES.fullName = user?.fullName
            Constants.VARIABLES.profilePicUrl = user?.profilePicture
        }

        Log.e("mobileNo", Constants.VARIABLES.mobileNo?:"")
        Log.e("fullName", Constants.VARIABLES.fullName?:"")
        Log.e("profilePicUrl", Constants.VARIABLES.profilePicUrl?:"")

//        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        nameTextView?.text = Constants.VARIABLES.fullName

        if (Constants.VARIABLES.mobileNo.equals("+919809412755")) {
            userListView.visibility = View.VISIBLE
            addUserView.visibility = View.VISIBLE
        } else {
            userListView.visibility = View.GONE
            addUserView.visibility = View.GONE
        }

        if (!Constants.VARIABLES.profilePicUrl.isNullOrEmpty() &&
            Constants.VARIABLES.profilePicUrl != "null") {
            StorageUtil.pathToReference(Constants.VARIABLES.profilePicUrl!!) { fileUrl ->
                Picasso.get()
                    .load(fileUrl)
                    .centerInside()
                    .resize(300, 300)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(circleImageView, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: java.lang.Exception) {
                            Picasso.get()
                                .load(fileUrl)
                                .centerInside()
                                .resize(300, 300)
                                .placeholder(ContextCompat.getDrawable(this@MainActivity, R.drawable.logo_default)!!)
                                .error(ContextCompat.getDrawable(this@MainActivity, R.drawable.logo_default)!!)
                                .into(circleImageView)
                        }
                    }
                    )
            }

        }

        membersListView.setOnClickListener {
            startActivity(Intent(this@MainActivity, ListMemberActivity::class.java))
        }

        addMemeberView.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddMemberActivity::class.java))
        }

        familyHeadsListView.setOnClickListener {
            startActivity(Intent(this@MainActivity, ListFamilyHeadsActivity::class.java))
        }

        addFamilyHeadsView.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddFamilyHeadsActivity::class.java))
        }

        userListView.setOnClickListener {
            startActivity(Intent(this@MainActivity, ListUserActivity::class.java))
        }

        addUserView.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddUserActivity::class.java))
        }

        logoutView.setOnClickListener {
            UtilityFunctions.showAlertOnActivity(this@MainActivity,
                resources.getString(R.string.are_you_sure), resources.getString(R.string.yes),
                resources.getString(R.string.no), true, false, {
                    FirebaseAuth.getInstance().signOut()

                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    ActivityCompat.finishAffinity(this@MainActivity)
                    finish()

                    //val editor = sharedPreferences.edit()
                    //editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                    //editor.putString(Constants.PrefsConstants.USER_ID, "")
                    //editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                    //editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                    //editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                    //editor.commit()
                }, {})
        }
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        var result = true

        val permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (!(permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED)) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            result = false
        }

        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA)
            result = false
        }

        return result
    }

    private fun requestForSpecificPermission() {
        val stringArr = permissionsList.toTypedArray<String>()
        ActivityCompat.requestPermissions(this@MainActivity, stringArr, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                }
                else -> finish()
            }//granted
            //System.exit(0);
            //not granted
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}