package com.sarathk.sk.kshethra.ui.login

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.main.MainActivity
import com.sarathk.sk.kshethra.utilities.ConnectivityReceiver
import com.sarathk.sk.kshethra.utilities.FirestoreUtil
import com.sarathk.sk.kshethra.utilities.StorageUtil
import com.sarathk.sk.kshethra.utilities.UtilityFunctions
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private var fullname: String? = null
    private var phoneNumber: String? = null
    private lateinit var permissionsList: MutableList<String>
    private val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124

    private val MY_PERMISSIONS_REQUEST_CAMERA = 99
    private val RESULT_LOAD_IMAGE = 101
    private val TAKE_PHOTO_CODE = 102
    private var selectedPhotoUri: Uri? = null
    private var selectedBitmap: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        customTitle(resources.getString(R.string.login))

        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            permissionsList = ArrayList()
            if (!checkIfAlreadyhavePermission()) { requestForSpecificPermission() }
        }

        phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber!!

        profileFrameLayout.setOnClickListener {
            selectImage()
        }

        loginButton.setOnClickListener {
            if (validate()){
                if (ConnectivityReceiver.isConnected ) {
                    if (selectedPhotoUri != null && selectedBitmap != null) {
                        updateUserWithImage()
                    } else {
                        updateUserWithOutImage()
                    }
                } else {
                    ConnectivityReceiver.errorDialog(this@RegisterActivity)
                }
            }
        }
    }

    private fun updateUserWithImage() {
        UtilityFunctions.showProgressDialog(this@RegisterActivity)
        val fileName = "/users/${phoneNumber + "_" + System.nanoTime()}"

        StorageUtil.uploadFileBytes(selectedBitmap!!, fileName) { filePath ->
            FirestoreUtil.updateCurrentUser(fullname!!, phoneNumber!!, filePath) { status ->
                if (status) {
                    UtilityFunctions.hideProgressDialog()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else {
                    UtilityFunctions.hideProgressDialog()
                    UtilityFunctions.showAlertOnActivity(this@RegisterActivity,
                        resources.getString(R.string.error_occured), resources.getString(R.string.ok),
                        "", false, true, {}, {})
                }
            }
        }

        UtilityFunctions.hideProgressDialog()
    }

    private fun updateUserWithOutImage() {
        UtilityFunctions.showProgressDialog(this@RegisterActivity)

        FirestoreUtil.updateCurrentUser(fullname!!, phoneNumber!!, null) { status ->
            if (status) {
                UtilityFunctions.hideProgressDialog()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            } else {
                UtilityFunctions.hideProgressDialog()
                UtilityFunctions.showAlertOnActivity(this@RegisterActivity,
                    resources.getString(R.string.error_occured), resources.getString(R.string.ok),
                    "", false, true, {}, {})
            }
        }
    }

    private fun validate(): Boolean {
        fullname = fullnameEditText.text.toString().trim()

        if (fullname.isNullOrEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@RegisterActivity,
                resources.getString(R.string.invalid_name), resources.getString(R.string.ok),
                "", false, false, {}, {})
            fullnameEditText.requestFocus()
            return false
        } else {
            fullnameEditText.error = null
        }

        return true
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
        ActivityCompat.requestPermissions(this@RegisterActivity, stringArr, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
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

    private fun selectImage() {
        val builder = AlertDialog.Builder(this@RegisterActivity)
        val inflater = this@RegisterActivity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_image_layout, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        val cameraButton = view.findViewById<ImageView>(R.id.cameraButton)
        val galleryButton = view.findViewById<ImageView>(R.id.galleryButton)
        val cancelButton = view.findViewById<ImageView>(R.id.cancelButton)

        cameraButton.setOnClickListener {
            alertDialog.dismiss()

            if (ActivityCompat.checkSelfPermission(this@RegisterActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@RegisterActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)

            } else {
                val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    val photoFile = File(this@RegisterActivity.getExternalFilesDir(null), "pic.jpg")
                    selectedPhotoUri = FileProvider.getUriForFile(this@RegisterActivity, applicationContext.packageName + ".provider", photoFile)
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
                UtilityFunctions.showAlertOnActivity(this@RegisterActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@RegisterActivity)

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

                profileImageView.setImageBitmap(bm)
                addProfileImageView.visibility = View.GONE
                profileFrameLayout.isClickable = false
            }

        } else if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedPhotoUri!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val f = File(picturePath)

            selectedPhotoUri = f.toUri()

            val file_size = (f.length()/1024)

            if (file_size > 17000) {
                UtilityFunctions.showAlertOnActivity(this@RegisterActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@RegisterActivity)

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

                profileImageView.setImageBitmap(bm)
                addProfileImageView.visibility = View.GONE
                profileFrameLayout.isClickable = false
            }
        }
    }
}