package com.sarathk.sk.kshethra.utilities

import com.sarathk.sk.kshethra.BuildConfig
import com.sarathk.sk.kshethra.ui.member.Gender
import com.sarathk.sk.kshethra.ui.member.Nakshatra
import com.sarathk.sk.kshethra.ui.member.Relation

object Constants {

    object PrefsConstants {
        const val NAME              = BuildConfig.APPLICATION_ID + "prefs"
        const val IS_LOGGED_IN      = BuildConfig.APPLICATION_ID + "logged_in"
        const val USER_ID           = BuildConfig.APPLICATION_ID + "user_id"
        const val USER_USERNAME     = BuildConfig.APPLICATION_ID + "user_username"
        const val USER_TOKEN        = BuildConfig.APPLICATION_ID + "user_token"
        const val USER_PHOTO        = BuildConfig.APPLICATION_ID + "user_photo"
    }
    object APP_CONSTANTS {
        const val SUP_ADMIN_ID     = "1"
        const val SERVER_URL       = "http://192.168.1.6/kshethra/index.php?"
    }

    object VARIABLES {
        var nakshatraArrayList : ArrayList<Nakshatra> = ArrayList()
        var genderArrayList : ArrayList<Gender> = ArrayList()
        var relationArrayList : ArrayList<Relation> = ArrayList()
        var mobileNo: String? = null
        var fullName: String? = null
        var profilePicUrl: String? = null
    }

    object ACTIONS {
        const val EDIT_ITEM         = "EDIT"
        const val ADD_ITEM          = "ADD"
        const val GET_HEAD_CONST    = 1010
        const val RECORD_LIMIT      = 100
    }

    object SQLLITE {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "kshethraDB"
        const val TABLE_USER = "user_tbl"
        const val TABLE_HEAD = "head_tbl"
        const val TABLE_MEMB = "memb_tbl"
        const val SEL_ALL = 0
        const val SEL_UNSYNC = 1
        const val SEL_IMG_UNSYNC = 2
    }
}