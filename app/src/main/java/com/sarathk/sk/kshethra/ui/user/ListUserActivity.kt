package com.sarathk.sk.kshethra.ui.user

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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.login.LoginActivity
import com.sarathk.sk.kshethra.utilities.*
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.EDIT_ITEM
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_list_user.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.set

class ListUserActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var linearLayoutManagerUsersList: LinearLayoutManager
    private lateinit var recyclerViewAdapterUsersList: RecyclerView.Adapter<*>
    private var userArrayList: MutableList<User> = ArrayList()
    private var touchListener: RecyclerTouchListener? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_user)

        customTitle(resources.getString(R.string.user_list))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        recyclerViewUsers.setHasFixedSize(true)
        linearLayoutManagerUsersList = LinearLayoutManager(this@ListUserActivity)
        recyclerViewUsers.layoutManager = linearLayoutManagerUsersList
        recyclerViewAdapterUsersList = ListUserAdapter()
        recyclerViewUsers.adapter = recyclerViewAdapterUsersList
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        if (ConnectivityReceiver.isConnected ) {
            getListBackground().execute()
        } else {
            ConnectivityReceiver.errorDialog(this@ListUserActivity)
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class getListBackground : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListUserActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APILogin&m=get_list", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        userArrayList = ArrayList()

                        for (i in 0 until jsonArray.length()) {
                            val user = User(
                                0.toLong(),
                                jsonArray.getJSONObject(i).getString("id").trim().toLong(),
                                jsonArray.getJSONObject(i).getString("username").trim(),
                                jsonArray.getJSONObject(i).getString("uniquekey").trim(),
                                jsonArray.getJSONObject(i).getString("image").trim(),
                                jsonArray.getJSONObject(i).getString("active").trim().toInt(),
                                1, 1)
                            userArrayList.add(user)
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListUserActivity)
                                startActivity(Intent(this@ListUserActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        userArrayList = ArrayList()

                        UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }

                    recyclerViewAdapterUsersList.notifyDataSetChanged()

                    touchListener = RecyclerTouchListener(this@ListUserActivity, recyclerViewUsers)
                    touchListener!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
                        override fun onRowClicked(position: Int) {}

                        override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
                    })
                        .setSwipeOptionViews(R.id.deleteRelativeLayout, R.id.editRelativeLayout)
                        .setSwipeable(R.id.rowFGLinearLayout, R.id.rowBGLinearLayout
                        ) { viewID, position ->
                            when (viewID) {
                                R.id.editRelativeLayout -> {
                                    val intent = Intent(this@ListUserActivity, AddUserActivity::class.java)
                                    intent.putExtra("action", EDIT_ITEM);
                                    AddUserActivity.user = userArrayList[position]
                                    startActivity(intent)
                                }
                                R.id.deleteRelativeLayout -> {
                                    if (ConnectivityReceiver.isConnected ) {
                                        changeStatusBackground(userArrayList[position], position).execute()
                                    } else {
                                        ConnectivityReceiver.errorDialog(this@ListUserActivity)
                                    }
                                }
                                else -> Log.e("err", "Position $position")
                            }
                        }

                    recyclerViewUsers.addOnItemTouchListener(touchListener!!)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    internal inner class ListUserAdapter : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var usernameTextView: TextView = v.findViewById<View>(R.id.usernameTextView) as TextView
            var circleImageView: ImageView = v.findViewById<View>(R.id.circleImageView) as ImageView
            var deleteRelativeLayout: RelativeLayout = v.findViewById<View>(R.id.deleteRelativeLayout) as RelativeLayout
            var deleteTextView: TextView = v.findViewById<View>(R.id.deleteTextView) as TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_user, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setIsRecyclable(false)

            holder.usernameTextView.text = userArrayList[position].username

            if (userArrayList[position].active == 0) {
                holder.deleteTextView.text = resources.getString(R.string.activate)
                holder.circleImageView.alpha = 0.5f
                holder.usernameTextView.setTextColor(ContextCompat.getColor(this@ListUserActivity, R.color.colorGrey))
            } else {
                holder.deleteTextView.text = resources.getString(R.string.deactivate)
                holder.circleImageView.alpha = 1f
                holder.usernameTextView.setTextColor(ContextCompat.getColor(this@ListUserActivity, R.color.colorBlack))
            }

            if (userArrayList[position].id == 1.toLong()) {
                holder.deleteRelativeLayout.visibility = View.GONE
            } else {
                holder.deleteRelativeLayout.visibility = View.VISIBLE
            }

            if (!userArrayList[position].image.isNullOrEmpty()) {
                Picasso.get()
                    .load(userArrayList[position].image)
                    .centerInside()
                    .resize(110, 110)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.circleImageView, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: java.lang.Exception) {
                            Picasso.get()
                                .load(userArrayList[position].image)
                                .centerInside()
                                .resize(110, 110)
                                .placeholder(ContextCompat.getDrawable(this@ListUserActivity, R.drawable.logo_default)!!)
                                .error(ContextCompat.getDrawable(this@ListUserActivity, R.drawable.logo_default)!!)
                                .into(holder.circleImageView)
                        }
                    }
                    )
            }
        }

        override fun getItemCount(): Int {
            return userArrayList.size
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class changeStatusBackground internal constructor(internal val user: User, internal val position: Int) : AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@ListUserActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["userid"] = user.id.toString()
            if (user.active == 1) {
                params["status"] = "0"
            } else {
                params["status"] = "1"
            }

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APILogin&m=change_status", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
            UtilityFunctions.hideProgressDialog()

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").trim().equals("true", true)) {
                        if (user.active == 1) {
                            UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                                resources.getString(R.string.account_deactivated), resources.getString(R.string.ok),
                                "", false, false, {
                                    userArrayList[position].active = 0
                                    recyclerViewAdapterUsersList.notifyDataSetChanged()
                                }, {})

                        } else {
                            UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                                resources.getString(R.string.account_activated), resources.getString(R.string.ok),
                                "", false, false, {
                                    userArrayList[position].active = 1
                                    recyclerViewAdapterUsersList.notifyDataSetChanged()
                                }, {})
                        }

                    } else if(jsonObject.getString("status").trim().equals("logged_out", true)) {
                        UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(Constants.PrefsConstants.IS_LOGGED_IN, false)
                                editor.putString(Constants.PrefsConstants.USER_ID, "")
                                editor.putString(Constants.PrefsConstants.USER_USERNAME, "")
                                editor.putString(Constants.PrefsConstants.USER_TOKEN, "")
                                editor.putString(Constants.PrefsConstants.USER_PHOTO, "")
                                editor.apply()

                                ActivityCompat.finishAffinity(this@ListUserActivity)
                                startActivity(Intent(this@ListUserActivity, LoginActivity::class.java))
                                finish()
                            }, {})

                    } else {
                        UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@ListUserActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }
}
