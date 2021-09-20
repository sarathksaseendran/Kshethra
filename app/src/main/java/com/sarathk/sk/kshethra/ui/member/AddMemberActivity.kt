package com.sarathk.sk.kshethra.ui.member

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.familyheads.FamilyHead
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.utilities.*
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.GET_HEAD_CONST
import kotlinx.android.synthetic.main.activity_add_member.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AddMemberActivity : AppCompatActivity() , View.OnClickListener {

    private lateinit var name: String
    private lateinit var housename: String
    private lateinit var gender: String
    private lateinit var dob: String
    private var star: Int? = null
    private lateinit var address1: String
    private lateinit var address2: String
    private lateinit var landline: String
    private lateinit var mobile: String
    private var fmheadid: Long? = null
    private lateinit var relation: String
    private var isfmhead: Int? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var action = Constants.ACTIONS.ADD_ITEM
    private var datePickerDialog: DatePickerDialog? = null

    companion object {
        var member: Member? = null
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        try {
            action = intent.getStringExtra("action")?:""
        } catch (e: Exception) {
            Log.e("err", e.localizedMessage)
        }

        if (action == Constants.ACTIONS.EDIT_ITEM) {
            customTitle(resources.getString(R.string.edit_member))
            name = member?.name!!
            housename = member?.housename!!
            gender = member?.gender!!
            dob = member?.dob!!
            star = member?.star!!
            address1 = member?.address1!!
            address2 = member?.address2!!
            landline = member?.landline!!
            mobile = member?.mobile!!
            fmheadid = member?.fmhdid!!
            relation = member?.relation!!
            isfmhead = member?.isfmhd!!

            nameEditText.setText(name)
            housenameEditText.setText(housename)
            dobButton.text = dob
            dobButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
            address1EditText.setText(address1)
            address2EditText.setText(address2)
            landlineEditText.setText(landline)
            mobileEditText.setText(mobile)
            familyHeadButton.text = member?.headname!!
            familyHeadButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
            isHeadCheckBox.isChecked = (isfmhead == 1)

            Constants.VARIABLES.genderArrayList.forEach {
                if (it.id == gender) {
                    genderButton.text = it.name
                    genderButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                }
            }

            Constants.VARIABLES.nakshatraArrayList.forEach {
                if (it.id == star) {
                    starButton.text = it.name
                    starButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                }
            }

            Constants.VARIABLES.relationArrayList.forEach {
                if (it.id == relation) {
                    relationButton.text = it.name
                    relationButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                }
            }

            createButton.text = resources.getString(R.string.save_member)
        } else {
            customTitle(resources.getString(R.string.add_member))
            name = ""
            housename = ""
            gender = ""
            dob = ""
            star = 0
            address1 = ""
            address2 = ""
            landline = ""
            mobile = ""
            fmheadid = 0
            relation = ""
            isfmhead = 0
            createButton.text = resources.getString(R.string.create_member)
        }

        createButton.setOnClickListener(this@AddMemberActivity)
        genderButton.setOnClickListener(this@AddMemberActivity)
        dobButton.setOnClickListener(this@AddMemberActivity)
        starButton.setOnClickListener(this@AddMemberActivity)
        familyHeadButton.setOnClickListener(this@AddMemberActivity)
        relationButton.setOnClickListener(this@AddMemberActivity)
        isHeadCheckBoxLinearLayout.setOnClickListener(this@AddMemberActivity)
        isHeadCheckBox.setOnClickListener(this@AddMemberActivity)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun validate(): Boolean {
        name = nameEditText.text.toString().trim()
        housename = housenameEditText.text.toString().trim()
        address1 = address1EditText.text.toString().trim()
        address2 = address2EditText.text.toString().trim()
        landline = landlineEditText.text.toString().trim()
        mobile = mobileEditText.text.toString().trim()

        if (name.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_name), resources.getString(R.string.ok),
                "", false, false, {}, {})
            nameEditText.requestFocus()
            return false
        } else {
            nameEditText.error = null
        }

        if (housename.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_housename), resources.getString(R.string.ok),
                "", false, false, {}, {})
            housenameEditText.requestFocus()
            return false
        } else {
            housenameEditText.error = null
        }

        if (gender.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_gender), resources.getString(R.string.ok),
                "", false, false, {}, {})
            genderButton.performClick()
            return false
        }

        if (dob.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_dob), resources.getString(R.string.ok),
                "", false, false, {}, {})
            dobButton.performClick()
            return false
        }

        if (star == 0) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_star), resources.getString(R.string.ok),
                "", false, false, {}, {})
            starButton.performClick()
            return false
        }

        if (address1.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_address1), resources.getString(R.string.ok),
                "", false, false, {}, {})
            address1EditText.requestFocus()
            return false
        } else {
            address1EditText.error = null
        }

        if (mobile.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_mobile), resources.getString(R.string.ok),
                "", false, false, {}, {})
            mobileEditText.requestFocus()
            return false
        } else {
            mobileEditText.error = null
        }

        if (fmheadid == 0.toLong()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_familyHead), resources.getString(R.string.ok),
                "", false, false, {}, {})
            familyHeadButton.performClick()
            return false
        }

        if (relation.isEmpty()) {
            UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                resources.getString(R.string.invalid_relation), resources.getString(R.string.ok),
                "", false, false, {}, {})
            relationButton.performClick()
            return false
        }

        return true
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.createButton) {
            if (validate()){
                if (ConnectivityReceiver.isConnected ) {
                    if (action == Constants.ACTIONS.EDIT_ITEM) {
                        editMemberBackground().execute()
                    } else {
                        createMemberBackground().execute()
                    }
                } else {
                    ConnectivityReceiver.errorDialog(this@AddMemberActivity)
                }
            }
        } else if (v?.id == R.id.genderButton) {
            val selectStateDialog = Dialog(this@AddMemberActivity)
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
            val arrayAdapter = GenderAdapter(Constants.VARIABLES.genderArrayList, this@AddMemberActivity)
            selectListView.adapter = arrayAdapter
            selectListView.onItemClickListener =
                OnItemClickListener { adapterView, view, i, l ->
                    val selectedGender: Gender = adapterView.getItemAtPosition(i) as Gender
                    gender = selectedGender.id
                    genderButton.text = selectedGender.name
                    genderButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                    selectStateDialog.dismiss()
                }

            val selectTitle = selectStateDialog.findViewById<TextView>(R.id.select_title)
            selectTitle.text = resources.getString(R.string.select_gender)
            val cancel = selectStateDialog.findViewById<Button>(R.id.cancel)
            cancel.setOnClickListener { selectStateDialog.dismiss() }

            selectStateDialog.setOnKeyListener { arg0, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    selectStateDialog.dismiss()
                }
                true
            }
        } else if (v?.id == R.id.dobButton) {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            datePickerDialog = DatePickerDialog(this@AddMemberActivity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    run {
                        var monthOfYear = monthOfYear
                        monthOfYear += 1
                        var revString: String = "$year"

                        if (monthOfYear < 10)
                            revString += "/0$monthOfYear"
                        else
                            revString += "/$monthOfYear"

                        if (dayOfMonth < 10)
                            revString += "/0$dayOfMonth"
                        else
                            revString += "/$dayOfMonth"

                        dobButton.text = revString
                        dobButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                        dob = revString
                    }
                }, mYear, mMonth, mDay)
            //c.set(mYear, 0, 1)
            c.set(mYear, mMonth, mDay)
            datePickerDialog!!.datePicker.maxDate = c.timeInMillis
            datePickerDialog!!.show()

        } else if (v?.id == R.id.starButton) {
            val selectStateDialog = Dialog(this@AddMemberActivity)
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
            val arrayAdapter = NakshatraAdapter(Constants.VARIABLES.nakshatraArrayList, this@AddMemberActivity)
            selectListView.adapter = arrayAdapter
            selectListView.onItemClickListener =
                OnItemClickListener { adapterView, view, i, l ->
                    val selectedStar: Nakshatra = adapterView.getItemAtPosition(i) as Nakshatra
                    star = selectedStar.id
                    starButton.text = selectedStar.name
                    starButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                    selectStateDialog.dismiss()
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
        } else if (v?.id == R.id.familyHeadButton) {
            val intent = Intent(this@AddMemberActivity, SearchHeadActivity::class.java)
            startActivityForResult(intent, GET_HEAD_CONST)

        } else if (v?.id == R.id.relationButton) {
            val selectStateDialog = Dialog(this@AddMemberActivity)
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
            val arrayAdapter = RelationAdapter(Constants.VARIABLES.relationArrayList, this@AddMemberActivity)
            selectListView.adapter = arrayAdapter
            selectListView.onItemClickListener =
                OnItemClickListener { adapterView, view, i, l ->
                    val selectedRelation: Relation = adapterView.getItemAtPosition(i) as Relation
                    relation = selectedRelation.id
                    relationButton.text = selectedRelation.name
                    relationButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                    selectStateDialog.dismiss()
                }

            val selectTitle = selectStateDialog.findViewById<TextView>(R.id.select_title)
            selectTitle.text = resources.getString(R.string.select_relation)
            val cancel = selectStateDialog.findViewById<Button>(R.id.cancel)
            cancel.setOnClickListener { selectStateDialog.dismiss() }

            selectStateDialog.setOnKeyListener { arg0, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    selectStateDialog.dismiss()
                }
                true
            }
        } else if (v?.id == R.id.isHeadCheckBoxLinearLayout) {
            if (isfmhead == 1) {
                isHeadCheckBox.isChecked = false
                isfmhead = 0
            } else {
                isHeadCheckBox.isChecked = true
                isfmhead = 1
            }
        } else if (v?.id == R.id.isHeadCheckBox) {
            if (isfmhead == 1) {
                isHeadCheckBox.isChecked = false
                isfmhead = 0
            } else {
                isfmhead = 1
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_HEAD_CONST) {
            if (data != null && resultCode != Activity.RESULT_CANCELED) {
                try {
                    val selectedHead = data.getSerializableExtra("selectedHead") as FamilyHead
                    fmheadid = selectedHead.id
                    familyHeadButton.text = selectedHead.headname
                    familyHeadButton.setTextColor(ContextCompat.getColor(this@AddMemberActivity, R.color.colorBlack))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    class RelationAdapter(data: ArrayList<Relation>, context: Context) : ArrayAdapter<Relation>(context, android.R.layout.simple_list_item_1, data) {
        private class ViewHolder {
            var txtName: TextView? = null
        }

        private var lastPosition = -1
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val dataModel: Relation? = getItem(position)
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
            return convertView!!
        }
    }

    class NakshatraAdapter(data: ArrayList<Nakshatra>, context: Context) : ArrayAdapter<Nakshatra>(context, android.R.layout.simple_list_item_1, data) {
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
            return convertView!!
        }
    }

    class GenderAdapter(data: ArrayList<Gender>, context: Context) : ArrayAdapter<Gender>(context, android.R.layout.simple_list_item_1, data) {
        //var dataSet: ArrayList<Gender> = data
        //var mContext: Context = context
        private class ViewHolder {
            var txtName: TextView? = null
        }

        private var lastPosition = -1
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val dataModel: Gender? = getItem(position)
            val viewHolder: ViewHolder
            //val result: View?
            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                viewHolder.txtName = convertView.findViewById(android.R.id.text1)
                //result = convertView
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                //result = convertView
            }
            //val animation = AnimationUtils.loadAnimation(mContext, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top)
            //result!!.startAnimation(animation)
            lastPosition = position
            viewHolder.txtName?.text = dataModel?.name
            return convertView!!
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class editMemberBackground : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@AddMemberActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["memberid"] = member?.id.toString()
            params["name"] = name
            params["housename"] = housename
            params["gender"] = gender
            params["dob"] = dob
            params["star"] = star.toString()
            params["address1"] = address1
            params["address2"] = address2
            params["landline"] = landline
            params["mobile"] = mobile
            params["isfmhead"] = isfmhead.toString()
            params["fmheadid"] = fmheadid.toString()
            params["relation"] = relation

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIMember&m=edit_member", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                finish()
                            }, {})

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@AddMemberActivity)
                                startActivity(Intent(this@AddMemberActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class createMemberBackground : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@AddMemberActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["name"] = name
            params["housename"] = housename
            params["gender"] = gender
            params["dob"] = dob
            params["star"] = star.toString()
            params["address1"] = address1
            params["address2"] = address2
            params["landline"] = landline
            params["mobile"] = mobile
            params["isfmhead"] = isfmhead.toString()
            params["fmheadid"] = fmheadid.toString()
            params["relation"] = relation

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIMember&m=create_member", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                finish()
                            }, {})

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@AddMemberActivity)
                                startActivity(Intent(this@AddMemberActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@AddMemberActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }
}
