package com.sarathk.sk.kshethra.ui.familyheads

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.utilities.*
import io.github.lizhangqu.coreprogress.ProgressHelper
import io.github.lizhangqu.coreprogress.ProgressUIListener
import kotlinx.android.synthetic.main.activity_add_family_heads.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class AddFamilyHeadsActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var sharedPreferences: SharedPreferences
    private var action = Constants.ACTIONS.ADD_ITEM
    private val MY_PERMISSIONS_REQUEST_CAMERA = 99
    private val RESULT_LOAD_IMAGE = 101
    private val TAKE_PHOTO_CODE = 102
    private var selectedPhotoUri: Uri? = null
    private var selectedBitmap: ByteArray? = null
    //private var docPath: String = ""
    //private var downloadUrl: String = ""
    private val simpleDateFormat = SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH)

    companion object {
        var familyHead: FamilyHead? = null
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_family_heads)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        try {
            action = intent.getStringExtra("action")?:""
        } catch (e: Exception) {
            Log.e("err", e.localizedMessage)
        }

        if (action == Constants.ACTIONS.EDIT_ITEM) {
            customTitle(resources.getString(R.string.edit_head))
            name = familyHead?.headname!!
            nameEditText.setText(name)
            if (!familyHead?.image.isNullOrEmpty() && familyHead?.image != "null") {
                selectphotoButton.alpha = 0f
                selectphotoImageView.setImageBitmap(UtilityFunctions.convertBase64ToBitmap(familyHead?.image!!))
                //Picasso.get()
                //    .load(familyHead?.image)
                //    .centerInside()
                //    .resize(300, 300)
                //    .networkPolicy(NetworkPolicy.OFFLINE)
                //    .into(selectphotoImageView, object : Callback {
                //        override fun onSuccess() {}
                //        override fun onError(e: java.lang.Exception) {
                //            Picasso.get()
                //                .load(familyHead?.image)
                //                .centerInside()
                //                .resize(300, 300)
                //                .placeholder(ContextCompat.getDrawable(this@AddFamilyHeadsActivity, R.drawable.logo_default)!!)
                //                .error(ContextCompat.getDrawable(this@AddFamilyHeadsActivity, R.drawable.logo_default)!!)
                //                .into(selectphotoImageView)
                //        }
                //    }
                //    )
            }
            createButton.text = resources.getString(R.string.save_head)
        } else {
            customTitle(resources.getString(R.string.add_familyheads))
            name = ""
            nameEditText.setText("")
            createButton.text = resources.getString(R.string.create_head)
        }

        createButton.setOnClickListener {
            if (validate()){
                if (ConnectivityReceiver.isConnected ) {
                    //if (selectedPhotoUri != null && selectedBitmap != null) {
                    //    uploadImageToFirebaseStorage()
                    //} else
                    if (action == Constants.ACTIONS.EDIT_ITEM) {
                        editHeadBackground().execute()
                    } else {
                        createHeadBackground().execute()
                    }
                } else {
                    ConnectivityReceiver.errorDialog(this@AddFamilyHeadsActivity)
                }
            }
        }

        selectphotoButton.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {
        val builder = AlertDialog.Builder(this@AddFamilyHeadsActivity)
        val inflater = this@AddFamilyHeadsActivity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_image_layout, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        val cameraButton = view.findViewById<ImageView>(R.id.cameraButton)
        val galleryButton = view.findViewById<ImageView>(R.id.galleryButton)
        val cancelButton = view.findViewById<ImageView>(R.id.cancelButton)

        cameraButton.setOnClickListener {
            alertDialog.dismiss()

            if (ActivityCompat.checkSelfPermission(this@AddFamilyHeadsActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@AddFamilyHeadsActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)

            } else {
                val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    //val photoFile = UtilityFunctions.createImageFile()
                    val photoFile = File(this@AddFamilyHeadsActivity.getExternalFilesDir(null), "pic.jpg")
                    selectedPhotoUri = FileProvider.getUriForFile(this@AddFamilyHeadsActivity, applicationContext.packageName + ".provider", photoFile)
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedPhotoUri)
                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_CODE)
                } catch (e: IOException) {
                    Log.e("takePhotoIntent", e.localizedMessage)
                    e.printStackTrace()
                }
            }
        }

        galleryButton.setOnClickListener {
            alertDialog.dismiss()

            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, RESULT_LOAD_IMAGE)
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedPhotoUri = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedPhotoUri!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val f = File(picturePath)
            val file_size = (f.length()/1024)

            if (file_size > 17000) {
                UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@AddFamilyHeadsActivity)

                val baos = ByteArrayOutputStream()

                when {
                    file_size < 1000 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                    file_size in 1000..1999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    file_size in 2000..2999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    file_size in 3000..3999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                    file_size in 4000..7999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    file_size >= 8000 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                }

                selectedBitmap = baos.toByteArray()

                selectphotoImageView.setImageBitmap(bm)
                selectphotoButton.alpha = 0f
            }

        } else if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedPhotoUri!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val f = File(picturePath)
            val file_size = (f.length()/1024)

            if (file_size > 17000) {
                UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@AddFamilyHeadsActivity)

                val baos = ByteArrayOutputStream()

                when {
                    file_size < 1000 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                    file_size in 1000..1999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    file_size in 2000..2999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    file_size in 3000..3999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                    file_size in 4000..7999 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    file_size >= 8000 -> bm!!.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                }

                selectedBitmap = baos.toByteArray()

                selectphotoImageView.setImageBitmap(bm)
                selectphotoButton.alpha = 0f
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun validate(): Boolean {
        name = nameEditText.text.toString().trim()

        if (name.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                resources.getString(R.string.invalid_name), resources.getString(R.string.ok),
                "", false, false, {}, {})
            nameEditText.requestFocus()
            return false
        } else {
            nameEditText.error = null
        }

        return true
    }

    //private fun uploadImageToFirebaseStorage() {
    //    UtilityFunctions.showProgressDialog(this@AddFamilyHeadsActivity)
    //
    //    val temp = simpleDateFormat.format(Calendar.getInstance().time)
    //    docPath = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!! + "_HEAD_" + temp + ".jpg"
    //
    //    val firebaseStorage = FirebaseStorage.getInstance().getReference("/kshethra/$docPath")
    //
    //    val uploadTask = firebaseStorage.putBytes(selectedBitmap!!)
    //
    //    uploadTask.addOnFailureListener (this@AddFamilyHeadsActivity) {
    //        UtilityFunctions.hideProgressDialog()
    //        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
    //            resources.getString(R.string.photo_upload_failed), resources.getString(R.string.ok),
    //            "", false, false, {}, {})
    //        //Log.e("AddUserActivity", "Failed to upload image to storage: ${it.message}")
    //
    //    }.addOnSuccessListener (this@AddFamilyHeadsActivity) {
    //        //Log.e("AddUserActivity", "Successfully uploaded image: ${it.metadata?.path}")
    //
    //        firebaseStorage.downloadUrl
    //            .addOnSuccessListener (this@AddFamilyHeadsActivity) {
    //                //Log.e("AddUserActivity", "File Location: $it")
    //                downloadUrl = it.toString()
    //                if (action == Constants.ACTIONS.EDIT_ITEM) {
    //                    editHeadBackground().execute()
    //                } else {
    //                    createHeadBackground().execute()
    //                }
    //            }
    //            .addOnFailureListener (this@AddFamilyHeadsActivity) {
    //                UtilityFunctions.hideProgressDialog()
    //                UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
    //                    resources.getString(R.string.photo_upload_failed), resources.getString(R.string.ok),
    //                    "", false, false, {}, {})
    //            }
    //    }.addOnProgressListener { taskSnapshot ->
    //        val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
    //        Log.e("AddUserActivity", "Upload is $progress% done")
    //    }
    //}

    @SuppressLint("StaticFieldLeak")
    private inner class uploadImageToDB : AsyncTask<Void, Void, String?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog(this@AddFamilyHeadsActivity)
        }

        override fun doInBackground(vararg params: Void?): String? {
            var result: String? = null

            try {
                val content_type = "image/jpeg"
                val file_body = selectedBitmap!!.toRequestBody(content_type.toMediaType())
                val temp = simpleDateFormat.format(Calendar.getInstance().time)
                val docPath = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!! + "_HEAD_" + temp + ".jpg"

                val bodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", content_type)
                    .addFormDataPart("uploaded_file", docPath, file_body)
                    .addFormDataPart("loginusr", sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!)
                    .addFormDataPart("uniquekey", sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!)
                    .addFormDataPart("memberid", familyHead?.memberid.toString())
                    .addFormDataPart("headid", familyHead?.id.toString())
                    .build()

                Log.e("_data", bodyBuilder.toString())
                Log.e("_url", Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=save_image")

                val request_body = ProgressHelper.withProgress(bodyBuilder, object : ProgressUIListener() {
                    override fun onUIProgressChanged(numBytes: Long, totalBytes: Long, percent: Float, speed: Float) {
                    }
                })

                val request = Request.Builder()
                    .url(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=save_image")
                    .post(request_body)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                result = response.body?.string()

                Log.e("file_upload", "Response : $result");

                return result

            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Log.e("file_upload", "Error: " + e.message)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                Log.e("file_upload", "Error: " + e.message)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("file_upload", "Other Error: " + e.message)
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            UtilityFunctions.hideProgressDialog()

            if (result != null) {
                try {
                    val jsonObject = JSONObject(result)

                    if (jsonObject.getString("status").equals("true", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            resources.getString(R.string.success), resources.getString(R.string.ok),
                            "", false, false, {
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            resources.getString(R.string.failed), resources.getString(R.string.ok),
                            "", false, false, {
                                finish()
                            }, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class editHeadBackground : AsyncTask<Void, Void, JSONObject?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@AddFamilyHeadsActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["headid"] = familyHead?.id.toString()
            params["headname"] = name
            //params["imageurl"] = downloadUrl
            //params["docpath"] = docPath

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=edit_head", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        uploadImageToDB().execute()
//                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
//                            jsonObject.getString("message"), resources.getString(R.string.ok),
//                            "", false, false, {
//                                finish()
//                            }, {})

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@AddFamilyHeadsActivity)
                                startActivity(Intent(this@AddFamilyHeadsActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class createHeadBackground : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@AddFamilyHeadsActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["headname"] = name
            //params["imageurl"] = downloadUrl
            //params["docpath"] = docPath

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=create_head", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        familyHead?.id = jsonObject.getLong("data")
                        uploadImageToDB().execute()
//                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
//                            jsonObject.getString("message"), resources.getString(R.string.ok),
//                            "", false, false, {
//                                finish()
//                            }, {})

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@AddFamilyHeadsActivity)
                                startActivity(Intent(this@AddFamilyHeadsActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@AddFamilyHeadsActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }
}
