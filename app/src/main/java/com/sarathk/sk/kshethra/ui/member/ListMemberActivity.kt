package com.sarathk.sk.kshethra.ui.member

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.utilities.*
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.RECORD_LIMIT
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_list_member.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ListMemberActivity : AppCompatActivity() {

    private var searchstr = ""
    private var selected_star = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var linearLayoutManagerMemberList: LinearLayoutManager
    private lateinit var recyclerViewAdapterMemberList: RecyclerView.Adapter<*>
    private var memberArrayList: MutableList<Member> = ArrayList()
    private var touchListener: RecyclerTouchListener? = null
    private val MY_PERMISSIONS_REQUEST_CAMERA = 99
    private val RESULT_LOAD_IMAGE = 101
    private val TAKE_PHOTO_CODE = 102
    private var selectedPhotoUri: Uri? = null
    private var selectedBitmap: ByteArray? = null
    private var docPath: String = ""
    private var downloadUrl: String = ""
    private var imagePosition: Int = -1
    private val simpleDateFormat = SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH)

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_member)

        customTitle(resources.getString(R.string.members_list))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        recyclerViewMembers.setHasFixedSize(true)
        linearLayoutManagerMemberList = LinearLayoutManager(this@ListMemberActivity)
        recyclerViewMembers.layoutManager = linearLayoutManagerMemberList
        recyclerViewAdapterMemberList = ListUserAdapter()
        recyclerViewMembers.adapter = recyclerViewAdapterMemberList

        searchViewMember.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchstr = query!!
                if (ConnectivityReceiver.isConnected ) {
                    memberBackground(true).execute()
                } else {
                    ConnectivityReceiver.errorDialog(this@ListMemberActivity)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchstr = newText!!
                if (ConnectivityReceiver.isConnected ) {
                    memberBackground(false).execute()
                } else {
                    ConnectivityReceiver.errorDialog(this@ListMemberActivity)
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (ConnectivityReceiver.isConnected ) {
            memberBackground(true).execute()
        } else {
            ConnectivityReceiver.errorDialog(this@ListMemberActivity)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_menu -> {
                val nakshatraArrayList1: ArrayList<Nakshatra> = ArrayList()
                nakshatraArrayList1.addAll(Constants.VARIABLES.nakshatraArrayList)
                nakshatraArrayList1.add(0, Nakshatra(0, resources.getString(R.string.show_all)))

                val selectStateDialog = Dialog(this@ListMemberActivity)
                selectStateDialog.setCancelable(false)
                selectStateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                selectStateDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
                selectStateDialog.setContentView(R.layout.dialog_dropdown)
                val Ip = WindowManager.LayoutParams()
                Ip.copyFrom(selectStateDialog.window!!.attributes)
                Ip.width = WindowManager.LayoutParams.WRAP_CONTENT
                Ip.height = WindowManager.LayoutParams.WRAP_CONTENT
                selectStateDialog.show()
                selectStateDialog.window!!.attributes = Ip

                val selectListView = selectStateDialog.findViewById<ListView>(R.id.select_listview)
                val arrayAdapter = NakshatraAdapter(nakshatraArrayList1, this@ListMemberActivity)
                selectListView.adapter = arrayAdapter
                selectListView.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val selectedStar: Nakshatra = adapterView.getItemAtPosition(i) as Nakshatra

                        selected_star = selectedStar.id
                        selectStateDialog.dismiss()

                        if (ConnectivityReceiver.isConnected ) {
                            memberBackground(true).execute()
                        } else {
                            ConnectivityReceiver.errorDialog(this@ListMemberActivity)
                        }
                    }

                val selectTitle = selectStateDialog.findViewById<TextView>(R.id.select_title)
                selectTitle.text = resources.getString(R.string.select_star)
                val cancel = selectStateDialog.findViewById<Button>(R.id.cancel)
                cancel.setOnClickListener { selectStateDialog.dismiss() }

                selectStateDialog.setOnKeyListener { arg0, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        selectStateDialog.dismiss()
                    }
                    true
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    class NakshatraAdapter(data: java.util.ArrayList<Nakshatra>, context: Context) : ArrayAdapter<Nakshatra>(context, android.R.layout.simple_list_item_1, data) {
        private class ViewHolder {
            var txtName: TextView? = null
        }

        private var lastPosition = -1
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val dataModel: Nakshatra? = getItem(position)
            val viewHolder: ViewHolder

            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                viewHolder.txtName = convertView.findViewById(android.R.id.text1)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            lastPosition = position
            viewHolder.txtName?.text = dataModel?.name
            viewHolder.txtName?.gravity = Gravity.CENTER
            return convertView!!
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class memberBackground internal constructor(private var showdialog : Boolean) :
        AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListMemberActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["searchstr"] = searchstr
            params["selectedstar"] = selected_star.toString()
            params["limit"] = RECORD_LIMIT.toString()
            params["offset"] = "0"

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIMember&m=index", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        memberArrayList = ArrayList()

                        for (i in 0 until jsonArray.length()) {
                            val member = Member(
                                0.toLong(),
                                jsonArray.getJSONObject(i).getString("id").trim().toLong(),
                                jsonArray.getJSONObject(i).getString("name").trim(),
                                jsonArray.getJSONObject(i).getString("housename").trim(),
                                jsonArray.getJSONObject(i).getString("gender").trim(),
                                jsonArray.getJSONObject(i).getString("dob").trim(),
                                jsonArray.getJSONObject(i).getString("star").trim().toInt(),
                                jsonArray.getJSONObject(i).getString("address1").trim(),
                                jsonArray.getJSONObject(i).getString("address2").trim(),
                                jsonArray.getJSONObject(i).getString("landline").trim(),
                                jsonArray.getJSONObject(i).getString("mobile").trim(),
                                jsonArray.getJSONObject(i).getString("isfmhead").trim().toInt(),
                                jsonArray.getJSONObject(i).getString("fmheadid").trim().toLong(),
                                jsonArray.getJSONObject(i).getString("headname").trim(),
                                jsonArray.getJSONObject(i).getString("relation").trim(),
                                jsonArray.getJSONObject(i).getString("image").trim(),
                                jsonArray.getJSONObject(i).getString("active").trim().toInt(),
                                1, 1)
                            memberArrayList.add(member)
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListMemberActivity)
                                startActivity(Intent(this@ListMemberActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        memberArrayList = ArrayList()

                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            resources.getString(R.string.no_records_found), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }

                    recyclerViewAdapterMemberList.notifyDataSetChanged()

                    touchListener = RecyclerTouchListener(this@ListMemberActivity, recyclerViewMembers)
                    touchListener!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
                            override fun onRowClicked(position: Int) {}

                            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
                        })
                        .setSwipeOptionViews(R.id.deleteRelativeLayout, R.id.editRelativeLayout, R.id.changeImageRelativeLayout)
                        .setSwipeable(R.id.rowFGLinearLayout, R.id.rowBGLinearLayout
                        ) { viewID, position ->
                            when (viewID) {
                                R.id.editRelativeLayout -> {
                                    val intent = Intent(this@ListMemberActivity, AddMemberActivity::class.java)
                                    intent.putExtra("action", Constants.ACTIONS.EDIT_ITEM)
                                    AddMemberActivity.member = memberArrayList[position]
                                    startActivity(intent)
                                }
                                R.id.deleteRelativeLayout -> {
                                    if (ConnectivityReceiver.isConnected ) {
                                        changeStatusBackground(memberArrayList[position], position).execute()
                                    } else {
                                        ConnectivityReceiver.errorDialog(this@ListMemberActivity)
                                    }
                                }
                                R.id.changeImageRelativeLayout -> {
                                    imagePosition = position
                                    selectImage()
                                }
                                else -> Log.e("err", "Position $position")
                            }
                        }

                    recyclerViewMembers.addOnItemTouchListener(touchListener!!)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    internal inner class ListUserAdapter : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var nameTextView: TextView = v.findViewById<View>(R.id.nameTextView) as TextView
            var housenameTextView: TextView = v.findViewById<View>(R.id.housenameTextView) as TextView
            var starTextView: TextView = v.findViewById<View>(R.id.starTextView) as TextView
            var dobTextView: TextView = v.findViewById<View>(R.id.dobTextView) as TextView
            var mobileTextView: TextView = v.findViewById<View>(R.id.mobileTextView) as TextView
            var userCircleImageView: ImageView = v.findViewById<View>(R.id.userCircleImageView) as ImageView
            var deleteTextView: TextView = v.findViewById<View>(R.id.deleteTextView) as TextView
            var rowFGLinearLayout: LinearLayout = v.findViewById<View>(R.id.rowFGLinearLayout) as LinearLayout
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_member, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setIsRecyclable(false)
            val member = memberArrayList[position]

            holder.nameTextView.text = member.name
            holder.housenameTextView.text = member.housename
            holder.mobileTextView.text = member.mobile
            holder.dobTextView.text = member.dob

            Constants.VARIABLES.nakshatraArrayList.forEach {
                if (it.id == member.star.toInt()) {
                    holder.starTextView.text = it.name
                }
            }

            if (member.active == 0) {
                holder.deleteTextView.text = resources.getString(R.string.activate)
                holder.mobileTextView.alpha = 0.5f
                holder.starTextView.alpha = 0.5f
                holder.nameTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorGrey))
                holder.mobileTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorGrey))
                holder.housenameTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorGrey))
            } else {
                holder.deleteTextView.text = resources.getString(R.string.activate)
                holder.starTextView.alpha = 1f
                holder.nameTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorBlack))
                holder.mobileTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorBlack))
                holder.housenameTextView.setTextColor(ContextCompat.getColor(this@ListMemberActivity, R.color.colorBlack))
            }

            if (!member.image.isNullOrEmpty()) {
                try {
                    Picasso.get()
                        .load(member.image)
                        .centerInside()
                        .resize(110, 110)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.userCircleImageView, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: java.lang.Exception) {
                                Picasso.get()
                                    .load(member.image)
                                    .centerInside()
                                    .resize(110, 110)
                                    .placeholder(ContextCompat.getDrawable(this@ListMemberActivity, R.drawable.logo_default)!!)
                                    .error(ContextCompat.getDrawable(this@ListMemberActivity, R.drawable.logo_default)!!)
                                    .into(holder.userCircleImageView)
                            }
                        }
                        )
                } catch (e : IndexOutOfBoundsException){
                    e.printStackTrace()
                }
            }

            holder.rowFGLinearLayout.setOnClickListener {
                holder.itemView.performClick()
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(this@ListMemberActivity, MemberProfileActivity::class.java)
                MemberProfileActivity.member = member
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return memberArrayList.size
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class changeStatusBackground internal constructor(private var member: Member, private var position: Int) :
        AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListMemberActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["memberid"] = member.id.toString()
            if (member.active == 1) {
                params["status"] = "0"
            } else {
                params["status"] = "1"
            }

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIMember&m=change_status", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        if (member.active == 1) {
                            UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                                resources.getString(R.string.member_deactivated), resources.getString(R.string.ok),
                                "", false, false, {
                                    memberArrayList[position].active = 0
                                    recyclerViewAdapterMemberList.notifyDataSetChanged()
                                }, {})

                        } else {
                            UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                                resources.getString(R.string.member_activated), resources.getString(R.string.ok),
                                "", false, false, {
                                    memberArrayList[position].active = 1
                                    recyclerViewAdapterMemberList.notifyDataSetChanged()
                                }, {})
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListMemberActivity)
                                startActivity(Intent(this@ListMemberActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    private fun selectImage() {
        val builder = AlertDialog.Builder(this@ListMemberActivity)
        val inflater = this@ListMemberActivity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_image_layout, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        val cameraButton = view.findViewById<ImageView>(R.id.cameraButton)
        val galleryButton = view.findViewById<ImageView>(R.id.galleryButton)
        val cancelButton = view.findViewById<ImageView>(R.id.cancelButton)

        cameraButton.setOnClickListener {
            alertDialog.dismiss()

            if (ActivityCompat.checkSelfPermission(this@ListMemberActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@ListMemberActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)

            } else {
                val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    //val photoFile = UtilityFunctions.createImageFile()
                    val photoFile = File(this@ListMemberActivity.getExternalFilesDir(null), "pic.jpg")
                    selectedPhotoUri = FileProvider.getUriForFile(this@ListMemberActivity, applicationContext.packageName + ".provider", photoFile)
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
                UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@ListMemberActivity)

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

                if (bm != null) {
                    showUploadDialog(bm)
                }
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
                UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                    resources.getString(R.string.size_exceeded), resources.getString(R.string.ok),
                    "", false, false, {}, {})

            } else {
                var bm = UtilityFunctions.decodeFile(f)
                bm = UtilityFunctions.scaleDownBitmap(bm, 300, this@ListMemberActivity)

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

                if (bm != null) {
                    showUploadDialog(bm)
                }
            }
        }
    }

    private fun showUploadDialog(bm: Bitmap) {
        val selectStateDialog = Dialog(this@ListMemberActivity)
        selectStateDialog.setCancelable(false)
        selectStateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        selectStateDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        selectStateDialog.setContentView(R.layout.dialog_image_upload)
        val Ip = WindowManager.LayoutParams()
        Ip.copyFrom(selectStateDialog.window!!.attributes)
        Ip.width = WindowManager.LayoutParams.WRAP_CONTENT
        Ip.height = WindowManager.LayoutParams.WRAP_CONTENT
        selectStateDialog.show()
        selectStateDialog.window!!.attributes = Ip

        val selectedImageView = selectStateDialog.findViewById<ImageView>(R.id.selectedImageView)
        selectedImageView.setImageBitmap(bm)
        val submit = selectStateDialog.findViewById<Button>(R.id.ok_button)
        submit.setOnClickListener {
            selectStateDialog.dismiss()
            if (ConnectivityReceiver.isConnected ) {
                if (selectedPhotoUri != null && selectedBitmap != null) {
                    uploadImageToFirebaseStorage()
                }
            } else {
                ConnectivityReceiver.errorDialog(this@ListMemberActivity)
            }
        }

        val cancel = selectStateDialog.findViewById<Button>(R.id.cancel_button)
        cancel.setOnClickListener {
                selectStateDialog.dismiss()
        }
    }

    private fun uploadImageToFirebaseStorage() {
        UtilityFunctions.showProgressDialog(this@ListMemberActivity)

        val temp = simpleDateFormat.format(Calendar.getInstance().time)
        docPath = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!! + "_MMBR_" + temp + ".jpg"

        val firebaseStorage = FirebaseStorage.getInstance().getReference("/kshethra/$docPath")

        val uploadTask = firebaseStorage.putBytes(selectedBitmap!!)

        uploadTask.addOnFailureListener (this@ListMemberActivity) {
            UtilityFunctions.hideProgressDialog()
            UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                resources.getString(R.string.photo_upload_failed), resources.getString(R.string.ok),
                "", false, false, {}, {})
            //Log.e("AddUserActivity", "Failed to upload image to storage: ${it.message}")

        }.addOnSuccessListener (this@ListMemberActivity) {
            //Log.e("AddUserActivity", "Successfully uploaded image: ${it.metadata?.path}")

            firebaseStorage.downloadUrl
                .addOnSuccessListener (this@ListMemberActivity) {
                    //Log.e("AddUserActivity", "File Location: $it")
                    downloadUrl = it.toString()
                    changeImageBackground().execute()
                }
                .addOnFailureListener (this@ListMemberActivity) {
                    UtilityFunctions.hideProgressDialog()
                    UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                        resources.getString(R.string.photo_upload_failed), resources.getString(R.string.ok),
                        "", false, false, {}, {})
                }
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.e("AddUserActivity", "Upload is $progress% done")
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class changeImageBackground : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListMemberActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["memberid"] = memberArrayList[imagePosition].id.toString()
            params["imageurl"] = downloadUrl
            params["docpath"] = docPath

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIMember&m=change_image", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                memberArrayList[imagePosition].image = downloadUrl
                                recyclerViewAdapterMemberList.notifyDataSetChanged()
                            }, {})

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListMemberActivity)
                                startActivity(Intent(this@ListMemberActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListMemberActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }
}
