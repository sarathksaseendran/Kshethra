package com.sarathk.sk.kshethra.ui.member

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.ui.familyheads.AddFamilyHeadsActivity
import com.sarathk.sk.kshethra.ui.familyheads.FamilyHead
import com.sarathk.sk.kshethra.utilities.*
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.GET_HEAD_CONST
import com.sarathk.sk.kshethra.utilities.Constants.ACTIONS.RECORD_LIMIT
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_search_head.*
import org.json.JSONException
import org.json.JSONObject

class SearchHeadActivity : AppCompatActivity() {

    private lateinit var searchstr: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var linearLayoutManagerFamilyHeadList: LinearLayoutManager
    private lateinit var recyclerViewAdapterFamilyHeadList: RecyclerView.Adapter<*>
    private var familyHeadArrayList: MutableList<FamilyHead> = ArrayList()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_head)

        customTitle(resources.getString(R.string.search_familyheads))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        searchRecyclerView.setHasFixedSize(true)
        linearLayoutManagerFamilyHeadList = LinearLayoutManager(this@SearchHeadActivity)
        searchRecyclerView.layoutManager = linearLayoutManagerFamilyHeadList
        recyclerViewAdapterFamilyHeadList = ListUserAdapter()
        searchRecyclerView.adapter = recyclerViewAdapterFamilyHeadList

        searchViewHead.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchstr = query!!
                if (ConnectivityReceiver.isConnected ) {
                    searchHeadBackground(true).execute()
                } else {
                    ConnectivityReceiver.errorDialog(this@SearchHeadActivity)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchstr = newText!!
                if (ConnectivityReceiver.isConnected ) {
                    searchHeadBackground(false).execute()
                } else {
                    ConnectivityReceiver.errorDialog(this@SearchHeadActivity)
                }
                return false
            }
        })

        addFamilyHeadButton.paintFlags = addFamilyHeadButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        addFamilyHeadButton.setOnClickListener {
            val intent = Intent(this@SearchHeadActivity, AddFamilyHeadsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class searchHeadBackground internal constructor(private var showdialog : Boolean) :
        AsyncTask<Void, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute()
            UtilityFunctions.showProgressDialog (this@SearchHeadActivity)
        }

        override fun doInBackground(vararg param: Void): JSONObject? {
            val jsonParser = JsonParser()
            val params = HashMap<String, String>()

            params["loginusr"] = sharedPreferences.getString(Constants.PrefsConstants.USER_ID, "")!!
            params["uniquekey"] = sharedPreferences.getString(Constants.PrefsConstants.USER_TOKEN, "")!!
            params["searchstr"] = searchstr
            params["limit"] = RECORD_LIMIT.toString()
            params["offset"] = "0"

            return jsonParser.makeHttpRequest(Constants.APP_CONSTANTS.SERVER_URL + "c=APIFamilyHead&m=search_head", "POST", params)
        }

        override fun onPostExecute(jsonObject: JSONObject?) {
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

                    } else {
                        familyHeadArrayList = ArrayList()

                        UtilityFunctions.showAlertOnActivity(this@SearchHeadActivity,
                            jsonObject.getString("message"), resources.getString(R.string.ok),
                            "", false, false, {}, {})
                    }

                    recyclerViewAdapterFamilyHeadList.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                UtilityFunctions.showAlertOnActivity(this@SearchHeadActivity,
                    resources.getString(R.string.connection_error), resources.getString(R.string.ok),
                    "", false, false, {}, {})
            }
        }
    }

    internal inner class ListUserAdapter : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var usernameTextView: TextView = v.findViewById<View>(R.id.usernameTextView) as TextView
            var circleImageView: ImageView = v.findViewById<View>(R.id.circleImageView) as ImageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_user, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setIsRecyclable(false)
            val familyHead = familyHeadArrayList[position]

            holder.usernameTextView.text = familyHead.headname

            if (!familyHead.image.isNullOrEmpty()) {
                try {
                    Picasso.get()
                        .load(familyHead.image)
                        .centerInside()
                        .resize(300, 300)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.circleImageView, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: java.lang.Exception) {
                                Picasso.get()
                                    .load(familyHead.image)
                                    .centerInside()
                                    .resize(300, 300)
                                    .placeholder(ContextCompat.getDrawable(this@SearchHeadActivity, R.drawable.logo_default)!!)
                                    .error(ContextCompat.getDrawable(this@SearchHeadActivity, R.drawable.logo_default)!!)
                                    .into(holder.circleImageView)
                            }
                        }
                        )
                } catch (e : IndexOutOfBoundsException){
                    e.printStackTrace()
                }
            }

            holder.usernameTextView.setOnClickListener{
                holder.itemView.performClick()
            }

            holder.circleImageView.setOnClickListener {
                holder.itemView.performClick()
            }

            holder.itemView.setOnClickListener{
                UtilityFunctions.showAlertOnActivity(this@SearchHeadActivity,
                    resources.getString(R.string.are_you_sure), resources.getString(R.string.yes),
                    resources.getString(R.string.no), true, false, {
                        val intent = Intent()
                        intent.putExtra("selectedHead", familyHeadArrayList[position])
                        setResult(GET_HEAD_CONST, intent)
                        finish()
                    }, {})
            }
        }

        override fun getItemCount(): Int {
            return familyHeadArrayList.size
        }
    }
}
