package com.sarathk.sk.kshethra.ui.familyheads

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.extensions.executeAsyncTask
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.utilities.*
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.RECORD_LIMIT
import kotlinx.android.synthetic.main.activity_list_family_heads.*
import org.json.JSONException
import org.json.JSONObject

class ListFamilyHeadsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var linearLayoutManagerFamilyHeadList: LinearLayoutManager
    private lateinit var recyclerViewAdapterFamilyHeadList: RecyclerView.Adapter<*>
    private var familyHeadArrayList: MutableList<FamilyHead> = ArrayList()
    private var touchListener: RecyclerTouchListener? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_family_heads)

        customTitle(resources.getString(R.string.familyheads_list))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        recyclerViewFamilyHeads.setHasFixedSize(true)
        linearLayoutManagerFamilyHeadList = LinearLayoutManager(this@ListFamilyHeadsActivity)
        recyclerViewFamilyHeads.layoutManager = linearLayoutManagerFamilyHeadList
        recyclerViewAdapterFamilyHeadList = ListUserAdapter()
        recyclerViewFamilyHeads.adapter = recyclerViewAdapterFamilyHeadList
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()


        if (ConnectivityReceiver.isConnected ) {
            familyHeadBackground()
        } else {
            ConnectivityReceiver.errorDialog(this@ListFamilyHeadsActivity)
        }
    }

    private fun familyHeadBackground() {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            UtilityFunctions.showProgressDialog (this@ListFamilyHeadsActivity)
        }, doInBackground = {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["limit"] = RECORD_LIMIT.toString()
            params["offset"] = "0"

            jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=index", "POST", params)
        }, onPostExecute = { jsonObject ->
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        familyHeadArrayList = ArrayList()

                        for (i in 0 until jsonArray.length()) {
                            val familyHead = FamilyHead(
                                0.toLong(),
                                jsonArray.getJSONObject(i).getString("id").trim().toLong(),
                                jsonArray.getJSONObject(i).getString("headname").trim(),
                                jsonArray.getJSONObject(i).getString("memberid").trim().toLong(),
                                jsonArray.getJSONObject(i).getString("image").trim(),
                                jsonArray.getJSONObject(i).getString("active").trim().toInt(),
                                1,1)
                            familyHeadArrayList.add(familyHead)
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListFamilyHeadsActivity)
                                startActivity(Intent(this@ListFamilyHeadsActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        familyHeadArrayList = ArrayList()

                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }

                    recyclerViewAdapterFamilyHeadList.notifyDataSetChanged()

                    touchListener = RecyclerTouchListener(this@ListFamilyHeadsActivity, recyclerViewFamilyHeads)
                    touchListener!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
                            override fun onRowClicked(position: Int) {
                                //if (familyHeadArrayList[position].active == "0") {
                                //    Toast.makeText(applicationContext, resources.getString(R.string.inactive) + familyHeadArrayList[position].headname ,
                                //        Toast.LENGTH_SHORT).show();
                                //} else {
                                //    Toast.makeText(applicationContext, resources.getString(R.string.active) + familyHeadArrayList[position].headname ,
                                //        Toast.LENGTH_SHORT).show();
                                //}
                            }

                            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
                        })
                        .setSwipeOptionViews(R.id.deleteRelativeLayout, R.id.editRelativeLayout)
                        .setSwipeable(R.id.rowFGLinearLayout, R.id.rowBGLinearLayout
                        ) { viewID, position ->
                            when (viewID) {
                                R.id.editRelativeLayout -> {
                                    val intent = Intent(this@ListFamilyHeadsActivity, AddFamilyHeadsActivity::class.java)
                                    intent.putExtra("action", Constants.ACTIONS.EDIT_ITEM);
                                    AddFamilyHeadsActivity.familyHead = familyHeadArrayList[position]
                                    startActivity(intent)
                                }
                                R.id.deleteRelativeLayout -> {
                                    if (ConnectivityReceiver.isConnected ) {
                                        changeStatusBackground(familyHeadArrayList[position], position).execute()
                                    } else {
                                        ConnectivityReceiver.errorDialog(this@ListFamilyHeadsActivity)
                                    }
                                }
                                else -> Log.e("err", "Position $position")
                            }
                        }

                    recyclerViewFamilyHeads.addOnItemTouchListener(touchListener!!)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        })
    }

//    @SuppressLint("StaticFieldLeak")
//    internal inner class familyHeadBackground : AsyncTask<Void, Void, JSONObject>() {
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            UtilityFunctions.showProgressDialog (this@ListFamilyHeadsActivity)
//        }
//
//        override fun doInBackground(vararg param: Void): JSONObject? {
//            val jsonParser = JsonParser()
//            val params = HashMap<String, String>()
//
//            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
//            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
//            params["limit"] = RECORD_LIMIT.toString()
//            params["offset"] = "0"
//
//            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=index", "POST", params)
//        }
//
//        override fun onPostExecute(jsonObject: JSONObject?) {
//            UtilityFunctions.hideProgressDialog()
//
//            if (jsonObject != null) {
//                try {
//                    if (jsonObject.getString("status").trim().equals("true", true)) {
//                        val jsonArray = jsonObject.getJSONArray("data")
//                        familyHeadArrayList = ArrayList()
//
//                        for (i in 0 until jsonArray.length()) {
//                            val familyHead = FamilyHead(
//                                0.toLong(),
//                                jsonArray.getJSONObject(i).getString("id").trim().toLong(),
//                                jsonArray.getJSONObject(i).getString("headname").trim(),
//                                jsonArray.getJSONObject(i).getString("memberid").trim().toLong(),
//                                jsonArray.getJSONObject(i).getString("image").trim(),
//                                jsonArray.getJSONObject(i).getString("active").trim().toInt(),
//                                1,1)
//                            familyHeadArrayList.add(familyHead)
//                        }
//
//                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
//                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
//                            jsonObject.getString("message"), resources.getString(R.string.ok),
//                            "", false, false, {
//                                val editor = sharedPreferences.edit()
//                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
//                                editor.putString(Constants.PrefsConstants.USER_ID, "")
//                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
//                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
//                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
//                                editor.apply()
//
//                                ActivityCompat.finishAffinity(this@ListFamilyHeadsActivity)
//                                startActivity(Intent(this@ListFamilyHeadsActivity, LoginActivity::class.java))
//                                finish()
//                            }, {})
//
//                    } else {
//                        familyHeadArrayList = ArrayList()
//
//                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
//                            jsonObject.getString("message"), resources.getString(R.string.ok),
//                            "", false, false, {}, {})
//                    }
//
//                    recyclerViewAdapterFamilyHeadList.notifyDataSetChanged()
//
//                    touchListener = RecyclerTouchListener(this@ListFamilyHeadsActivity, recyclerViewFamilyHeads)
//                    touchListener!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
//                            override fun onRowClicked(position: Int) {
//                                //if (familyHeadArrayList[position].active == "0") {
//                                //    Toast.makeText(applicationContext, resources.getString(R.string.inactive) + familyHeadArrayList[position].headname ,
//                                //        Toast.LENGTH_SHORT).show();
//                                //} else {
//                                //    Toast.makeText(applicationContext, resources.getString(R.string.active) + familyHeadArrayList[position].headname ,
//                                //        Toast.LENGTH_SHORT).show();
//                                //}
//                            }
//
//                            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
//                        })
//                        .setSwipeOptionViews(R.id.deleteRelativeLayout, R.id.editRelativeLayout)
//                        .setSwipeable(R.id.rowFGLinearLayout, R.id.rowBGLinearLayout
//                        ) { viewID, position ->
//                            when (viewID) {
//                                R.id.editRelativeLayout -> {
//                                    val intent = Intent(this@ListFamilyHeadsActivity, AddFamilyHeadsActivity::class.java)
//                                    intent.putExtra("action", Constants.ACTIONS.EDIT_ITEM);
//                                    AddFamilyHeadsActivity.familyHead = familyHeadArrayList[position]
//                                    startActivity(intent)
//                                }
//                                R.id.deleteRelativeLayout -> {
//                                    if (ConnectivityReceiver.isConnected ) {
//                                        changeStatusBackground(familyHeadArrayList[position], position).execute()
//                                    } else {
//                                        ConnectivityReceiver.errorDialog(this@ListFamilyHeadsActivity)
//                                    }
//                                }
//                                else -> Log.e("err", "Position $position")
//                            }
//                        }
//
//                    recyclerViewFamilyHeads.addOnItemTouchListener(touchListener!!)
//
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//
//            } else {
//                UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
//                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
//                    "", false, false, {}, {})
//            }
//        }
//    }

    internal inner class ListUserAdapter : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var usernameTextView: TextView = v.findViewById<View>(R.id.usernameTextView) as TextView
            var circleImageView: ImageView = v.findViewById<View>(R.id.circleImageView) as ImageView
            var deleteTextView: TextView = v.findViewById<View>(R.id.deleteTextView) as TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_user, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setIsRecyclable(false)

            holder.usernameTextView.text = familyHeadArrayList[position].headname

            if (familyHeadArrayList[position].active == 0) {
                holder.deleteTextView.text = resources.getString(R.string.activate)
                holder.circleImageView.alpha = 0.5f
                holder.usernameTextView.setTextColor(ContextCompat.getColor(this@ListFamilyHeadsActivity, R.color.colorGrey))
            } else {
                holder.deleteTextView.text = resources.getString(R.string.deactivate)
                holder.circleImageView.alpha = 1f
                holder.usernameTextView.setTextColor(ContextCompat.getColor(this@ListFamilyHeadsActivity, R.color.colorBlack))
            }

            if (!familyHeadArrayList[position].image.isNullOrEmpty()) {
                holder.circleImageView.setImageBitmap(UtilityFunctions.convertBase64ToBitmap(familyHeadArrayList[position].image))
                //Picasso.get()
                //    .load(familyHeadArrayList[position].image)
                //    .centerInside()
                //    .resize(110, 110)
                //    .networkPolicy(NetworkPolicy.OFFLINE)
                //    .into(holder.circleImageView, object : Callback {
                //        override fun onSuccess() {}
                //        override fun onError(e: java.lang.Exception) {
                //            Picasso.get()
                //                .load(familyHeadArrayList[position].image)
                //                .centerInside()
                //                .resize(110, 110)
                //                .placeholder(ContextCompat.getDrawable(this@ListFamilyHeadsActivity, R.drawable.logo_default)!!)
                //                .error(ContextCompat.getDrawable(this@ListFamilyHeadsActivity, R.drawable.logo_default)!!)
                //                .into(holder.circleImageView)
                //        }
                //    }
                //    )
            }
        }

        override fun getItemCount(): Int {
            return familyHeadArrayList.size
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class changeStatusBackground internal constructor(private var familyHead: FamilyHead, private var position: Int) :
        AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListFamilyHeadsActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["headid"] = familyHead.id.toString()
            if (familyHead.active == 1) {
                params["status"] = "0"
            } else {
                params["status"] = "1"
            }

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=change_status", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        if (familyHead.active == 1) {
                            UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                                resources.getString(R.string.head_deactivated), resources.getString(R.string.ok),
                                "", false, false, {
                                    familyHeadArrayList[position].active = 0
                                    recyclerViewAdapterFamilyHeadList.notifyDataSetChanged()
                                }, {})

                        } else {
                            UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                                resources.getString(R.string.head_activated), resources.getString(R.string.ok),
                                "", false, false, {
                                    familyHeadArrayList[position].active = 1
                                    recyclerViewAdapterFamilyHeadList.notifyDataSetChanged()
                                }, {})
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListFamilyHeadsActivity)
                                startActivity(Intent(this@ListFamilyHeadsActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListFamilyHeadsActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }
}