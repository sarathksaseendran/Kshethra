package com.sarathk.sk.kshethra.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.main.MainActivity
import com.sarathk.sk.kshethra.utilities.FirestoreUtil
import com.sarathk.sk.kshethra.utilities.UtilityFunctions


class LoginActivity : AppCompatActivity() {

    //    private lateinit var username: String
    //    private lateinit var password: String
    //    private var passwordVisible = false
    //    //private var requestQueue: RequestQueue? = null
    //    private lateinit var permissionsList: MutableList<String>
    //    private val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
    //    private lateinit var sharedPreferences: SharedPreferences

    private val RC_SIGN_IN: Int = 1000

    private lateinit var mAuth: FirebaseAuth

    private lateinit var phoneNumber: String

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        customTitle(resources.getString(R.string.login))

        mAuth = FirebaseAuth.getInstance()

        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build()
            //AuthUI.IdpConfig.EmailBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.mipmap.ic_launcher)
                .build(),
            RC_SIGN_IN
        )

        //        val MyVersion = Build.VERSION.SDK_INT
        //        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
        //            permissionsList = ArrayList()
        //            if (!checkIfAlreadyhavePermission()) { requestForSpecificPermission() }
        //        }
        //
        //
        //        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)
        //
        //        loginButton.setOnClickListener {
        //            if (validate()){
        //                if (ConnectivityReceiver.isConnected ) {
        //                    loginBackground().execute()
        //                } else {
        //                    ConnectivityReceiver.errorDialog(this@LoginActivity)
        //                }
        //            }
        //        }
        //
        //        passwordImageView.setOnClickListener {
        //            if (passwordVisible) {
        //                passwordImageView.setImageDrawable(ContextCompat.getDrawable(this@LoginActivity, R.drawable.ic_eye_open))
        //                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        //            } else {
        //                passwordImageView.setImageDrawable(ContextCompat.getDrawable(this@LoginActivity, R.drawable.ic_eye_closed))
        //                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        //            }
        //
        //            passwordVisible = !passwordVisible
        //        }
        //
        //        //requestQueue = Volley.newRequestQueue(this)
    }

    //    private fun validate(): Boolean {
    //        username = usernameEditText.text.toString().trim()
    //        password = passwordEditText.text.toString().trim()
    //
    //        if (username.isEmpty()) {
    //            UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    //                resources.getString(R.string.invalid_username), resources.getString(R.string.ok),
    //                "", false, false, {}, {})
    //            usernameEditText.requestFocus()
    //            return false
    //        } else {
    //            usernameEditText.error = null
    //        }
    //
    //        if (password.isEmpty()) {
    //            UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    //                resources.getString(R.string.invalid_password), resources.getString(R.string.ok),
    //                "", false, false, {}, {})
    //            passwordEditText.requestFocus()
    //            return false
    //
    //        } else if (!UtilityFunctions.validPassword(password)) {
    //            UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    //                resources.getString(R.string.password_content), resources.getString(R.string.ok),
    //                "", false, false, {}, {})
    //            passwordEditText.requestFocus()
    //            return false
    //
    //        } else {
    //            passwordEditText.error = null
    //        }
    //
    //        return true
    //    }
    //
    //    override fun onStop() {
    //        //requestQueue?.cancelAll(this)
    //        super.onStop()
    //    }
    //
    //    @SuppressLint("StaticFieldLeak")
    //    internal inner class loginBackground : AsyncTask<Void, Void, JSONObject>() {
    //
    //        override fun onPreExecute() {
    //            super.onPreExecute()
    //            UtilityFunctions.showProgressDialog (this@LoginActivity)
    //        }
    //
    //        override fun doInBackground(vararg param: Void): JSONObject? {
    //            val jsonParser = JsonParser()
    //            val params = HashMap<String, String>()
    //
    //            params["username"] = username
    //            params["password"] = password
    //
    //            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APILogin&m=index", "POST", params)
    //        }
    //
    //        override fun onPostExecute(response: JSONObject?) {
    //            UtilityFunctions.hideProgressDialog()
    //
    //            if (response != null) {
    //                try {
    //                    if (response.getString("status").trim().equals("true", true)) {
    //                        val editor = sharedPreferences.edit()
    //
    //                        editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, true)
    //                        editor.putString(Constants.PrefsConstants.USER_ID,
    //                            response.getJSONArray("data").getJSONObject(0).getString("id").trim())
    //                        editor.putString(Constants.PrefsConstants.USER_USERNAME,
    //                            response.getJSONArray("data").getJSONObject(0).getString("username").trim())
    //                        editor.putString(Constants.PrefsConstants.USER_TOKEN,
    //                            response.getJSONArray("data").getJSONObject(0).getString("uniquekey").trim())
    //                        editor.putString(Constants.PrefsConstants.USER_PHOTO,
    //                            response.getJSONArray("data").getJSONObject(0).getString("image").trim())
    //                        editor.apply()
    //                        editor.apply()
    //
    //                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    //                        finish()
    //
    //                    } else {
    //                        UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    //                            response.getString("message"), resources.getString(R.string.ok),
    //                            "", false, false, {}, {})
    //                    }
    //                } catch (e: JSONException) {
    //                    e.printStackTrace()
    //                }
    //
    //            } else {
    //                UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    //                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
    //                    "", false, false, {}, {})
    //            }
    //        }
    //    }
    //
    ////    private fun loginBackground() {
    ////        UtilityFunctions.showProgressDialog(this)
    ////
    ////        val blogRequest: StringRequest = object :
    ////            StringRequest(Method.POST, Constants.APP_CONSTANTS.SERVER_URL + "c=APILogin&m=index",
    ////
    ////                Response.Listener<String> {
    ////                    Log.e("url", Constants.APP_CONSTANTS.SERVER_URL + "c=APILogin&m=index")
    ////                    Log.e("res", it.toString())
    ////                    UtilityFunctions.hideProgressDialog()
    ////                    try {
    ////                        val jsonObject = JSONObject(it)
    ////                        if (jsonObject.getString("status").trim().equals("true", true)) {
    ////                            val editor = sharedPreferences.edit()
    ////
    ////                            editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, true)
    ////                            editor.putString(Constants.PrefsConstants.USER_ID,
    ////                                jsonObject.getJSONArray("data").getJSONObject(0).getString("id").trim())
    ////                            editor.putString(Constants.PrefsConstants.USER_USERNAME,
    ////                                jsonObject.getJSONArray("data").getJSONObject(0).getString("username").trim())
    ////                            editor.putString(Constants.PrefsConstants.USER_TOKEN,
    ////                                jsonObject.getJSONArray("data").getJSONObject(0).getString("uniquekey").trim())
    ////                            editor.putString(Constants.PrefsConstants.USER_PHOTO,
    ////                                jsonObject.getJSONArray("data").getJSONObject(0).getString("image").trim())
    ////                            editor.apply()
    ////
    ////                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    ////                            finish()
    ////                        } else {
    ////                            UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    ////                                jsonObject.getString("message"), resources.getString(R.string.ok),
    ////                                "", false, false, {}, {})
    ////                        }
    ////                    } catch (e: Exception) {
    ////                        UtilityFunctions.showAlertOnActivity(this@LoginActivity,
    ////                            resources.getString(R.string.login_failed), resources.getString(R.string.ok),
    ////                            "", false, false, {}, {})
    ////                        e.printStackTrace()
    ////                    }
    ////
    ////                }, Response.ErrorListener {
    ////                    Log.e("err", it.toString())
    ////                    UtilityFunctions.hideProgressDialog()
    ////                    it.printStackTrace()
    ////                }) {
    ////
    ////            override fun getParams(): MutableMap<String, String> {
    ////                val params: MutableMap<String, String> = hashMapOf()
    ////                params["username"] = username
    ////                params["password"] = password
    ////                Log.e("par", params.toString())
    ////                return params
    ////            }
    ////
    ////            override fun getBodyContentType(): String? {
    ////                return "application/x-www-form-urlencoded; charset=utf-8"
    ////            }
    ////        }
    ////        blogRequest.retryPolicy = DefaultRetryPolicy(2000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    ////        requestQueue?.add(blogRequest)
    ////    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                if (user?.phoneNumber != null) {
                    phoneNumber = user.phoneNumber!!
                    checkUserAlreadySigned()
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                finish()
            }
        }
    }

    private fun checkUserAlreadySigned() {
        val db = Firebase.firestore

        db.collection("users")
            .whereEqualTo("mobileNumber", phoneNumber)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (document in result) {
                        if ((document.data["fullName"] as String).isEmpty()) {
                            updateData()
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                }  else {
                    updateData()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("here_", "Error getting documents.", exception)
                updateData()
            }
    }

    private fun updateData() {
        FirestoreUtil.initCurrentUserIfFirstTime { status ->
            if (status) {
                //FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                //    this@LoginActivity
                //) { instanceIdResult ->
                //    val newToken = instanceIdResult.token
                //    AjoowebFirebaseMessagingService.addTokenToFireStore(newToken)
                //}
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            } else {
                UtilityFunctions.showAlertOnActivity(this@LoginActivity,
                    resources.getString(R.string.error_occured), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    //    private fun checkIfAlreadyhavePermission(): Boolean {
    //        var result = true
    //
    //        val permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    //        val permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    //        val permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    //
    //        if (!(permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED)) {
    //            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    //            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    //            result = false
    //        }
    //
    //        if (permission3 != PackageManager.PERMISSION_GRANTED) {
    //            permissionsList.add(Manifest.permission.CAMERA)
    //            result = false
    //        }
    //
    //        return result
    //    }
    //
    //    private fun requestForSpecificPermission() {
    //        val stringArr = permissionsList.toTypedArray<String>()
    //        ActivityCompat.requestPermissions(this@LoginActivity, stringArr, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
    //    }
    //
    //    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    //        when (requestCode) {
    //            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> when (grantResults[0]) {
    //                PackageManager.PERMISSION_GRANTED -> {
    //                }
    //                else -> finish()
    //            }//granted
    //            //System.exit(0);
    //            //not granted
    //            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    //        }
    //    }
}