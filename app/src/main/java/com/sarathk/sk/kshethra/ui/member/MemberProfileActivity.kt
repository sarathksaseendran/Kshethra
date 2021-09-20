package com.sarathk.sk.kshethra.ui.member

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sarathk.sk.kshethra.R
import com.sarathk.sk.kshethra.extensions.customTitle
import com.sarathk.sk.kshethra.utilities.ConnectivityReceiver
import com.sarathk.sk.kshethra.utilities.Constants
import com.sarathk.sk.kshethra.utilities.RecyclerTouchListener
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_member_profile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemberProfileActivity : AppCompatActivity() {

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

    companion object {
        var member: Member? = null
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_profile)

        customTitle(resources.getString(R.string.members_list))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(Constants.PrefsConstants.NAME, Context.MODE_PRIVATE)

        myfamilyRecyclerView.setHasFixedSize(true)
        linearLayoutManagerMemberList = LinearLayoutManager(this@MemberProfileActivity)
        myfamilyRecyclerView.layoutManager = linearLayoutManagerMemberList
        //recyclerViewAdapterMemberList = ListUserAdapter()
        //myfamilyRecyclerView.adapter = recyclerViewAdapterMemberList

        if (member != null) {
            nameTextView.text = member?.name!!
            housenameTextView.text = member?.housename!!
            dobTextView.text = member?.dob!!
            mobileTextView.text = member?.mobile!!

            if (member?.landline.isNullOrEmpty()) {
                landlineTextView.visibility = View.GONE
            } else {
                landlineTextView.text = member?.landline!!
            }

            addressTextView.text = "${member?.address1!!} ${member?.address2!!}"

            if (!member?.image.isNullOrEmpty()) {
                try {
                    Picasso.get()
                        .load(member?.image)
                        .centerInside()
                        .resize(300, 300)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(userPicImageView, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: java.lang.Exception) {
                                Picasso.get()
                                    .load(member?.image)
                                    .centerInside()
                                    .resize(300, 300)
                                    .placeholder(ContextCompat.getDrawable(this@MemberProfileActivity, R.drawable.logo_default)!!)
                                    .error(ContextCompat.getDrawable(this@MemberProfileActivity, R.drawable.logo_default)!!)
                                    .into(userPicImageView)
                            }
                        }
                        )
                } catch (e : IndexOutOfBoundsException){
                    e.printStackTrace()
                }
            }

            Constants.VARIABLES.nakshatraArrayList.forEach {
                if (it.id == member?.star!!.toInt()) {
                    starTextView.text = it.name
                }
            }

            Constants.VARIABLES.relationArrayList.forEach {
                if (it.id == member?.relation!!) {
                    familyHeadRelationTextView.text = "${it.name} : ${member?.headname!!}"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (ConnectivityReceiver.isConnected ) {
            //memberBackground(true)
        } else {
            ConnectivityReceiver.errorDialog(this@MemberProfileActivity)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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
                holder.nameTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorGrey))
                holder.mobileTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorGrey))
                holder.housenameTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorGrey))
            } else {
                holder.deleteTextView.text = resources.getString(R.string.activate)
                holder.starTextView.alpha = 1f
                holder.nameTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorBlack))
                holder.mobileTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorBlack))
                holder.housenameTextView.setTextColor(ContextCompat.getColor(this@MemberProfileActivity, R.color.colorBlack))
            }

            if (!member.image.isNullOrEmpty()) {
                try {
                    Picasso.get()
                        .load(member.image)
                        .centerInside()
                        .resize(300, 300)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.userCircleImageView, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: java.lang.Exception) {
                                Picasso.get()
                                    .load(member.image)
                                    .centerInside()
                                    .resize(300, 300)
                                    .placeholder(ContextCompat.getDrawable(this@MemberProfileActivity, R.drawable.logo_default)!!)
                                    .error(ContextCompat.getDrawable(this@MemberProfileActivity, R.drawable.logo_default)!!)
                                    .into(holder.userCircleImageView)
                            }
                        }
                        )
                } catch (e : IndexOutOfBoundsException){
                    e.printStackTrace()
                }
            }
        }

        override fun getItemCount(): Int {
            return memberArrayList.size
        }
    }
}
