package com.sarathk.sk.kshethra.utilities

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sarathk.sk.kshethra.ui.familyheads.FamilyHead
import com.sarathk.sk.kshethra.ui.member.Member
import com.sarathk.sk.kshethra.ui.user.User
import java.util.*


/**
 * Created by sarathk on 3/1/17.
 */

class DBHandler(context: Context) :
    SQLiteOpenHelper(context, Constants.SQLLITE.DATABASE_NAME, null, Constants.SQLLITE.DATABASE_VERSION) {

    val USER_DBID = "db_id"
    val USER_SRID = "server_id"
    val USER_USNM = "user_name"
    val USER_UQKY = "unique_key"
    val USER_IMAG = "user_image"
    val USER_ACTV = "user_active"
    val USER_SYNC = "user_sync"
    val USER_IMSY = "user_img_sync"

    val HEAD_DBID = "db_id"
    val HEAD_SRID = "server_id"
    val HEAD_HDNM = "head_name"
    val HEAD_MBID = "head_mem_id"
    val HEAD_IMAG = "head_image"
    val HEAD_ACTV = "head_active"
    val HEAD_SYNC = "head_sync"
    val HEAD_IMSY = "head_img_sync"

    val MEMB_DBID = "db_id"
    val MEMB_SRID = "server_id"
    val MEMB_HDNM = "memb_name"
    val MEMB_HSNM = "memb_housename"
    val MEMB_GEND = "memb_gender"
    val MEMB_DOBH = "memb_dob"
    val MEMB_STAR = "memb_star"
    val MEMB_ADD1 = "memb_address1"
    val MEMB_ADD2 = "memb_address2"
    val MEMB_LAND = "memb_landline"
    val MEMB_MOBL = "memb_mobile"
    val MEMB_ISHD = "memb_is_family_head"
    val MEMB_FHID = "memb_family_head_id"
    val MEMB_FHNM = "memb_family_head_name"
    val MEMB_FHRL = "memb_family_head_relation"
    val MEMB_IMAG = "memb_image"
    val MEMB_ACTV = "memb_active"
    val MEMB_SYNC = "memb_sync"
    val MEMB_IMSY = "memb_im_sy"

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE_USER = ("CREATE TABLE " + Constants.SQLLITE.TABLE_USER + "(" +
                USER_DBID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                USER_SRID + " INTEGER UNIQUE NOT NULL," +
                USER_USNM + " TEXT," + USER_UQKY + " TEXT," + USER_IMAG + " TEXT," +
                USER_ACTV + " TINYINT," + USER_SYNC + " TINYINT," + USER_IMSY + " TINYINT )")

        val CREATE_TABLE_HEAD = ("CREATE TABLE " + Constants.SQLLITE.TABLE_HEAD + "(" +
                HEAD_DBID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                HEAD_SRID + " INTEGER UNIQUE NOT NULL," +
                HEAD_HDNM + " TEXT," + HEAD_MBID + " INTEGER," + HEAD_IMAG + " TEXT," +
                HEAD_ACTV + " TINYINT," + HEAD_SYNC + " TINYINT," + HEAD_IMSY + " TINYINT )")

        val CREATE_TABLE_MEMB = ("CREATE TABLE " + Constants.SQLLITE.TABLE_MEMB + "(" +
                MEMB_DBID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                MEMB_SRID + " INTEGER UNIQUE NOT NULL," +
                MEMB_HDNM + " TEXT," + MEMB_HSNM + " TEXT," + MEMB_GEND + " TEXT," +
                MEMB_DOBH + " TEXT," + MEMB_STAR + " INTEGER," +
                MEMB_ADD1 + " TEXT," + MEMB_ADD2 + " TEXT," + MEMB_LAND + " TEXT," +
                MEMB_MOBL + " TEXT," + MEMB_ISHD + " TINYINT," + MEMB_FHID + " INTEGER," +
                MEMB_FHNM + " TEXT," + MEMB_FHRL + " TEXT," + MEMB_IMAG + " TEXT," +
                MEMB_ACTV + " TINYINT," + MEMB_SYNC + " TINYINT," + MEMB_IMSY + " TINYINT )")

        try {
            db.execSQL(CREATE_TABLE_USER)
            db.execSQL(CREATE_TABLE_HEAD)
            db.execSQL(CREATE_TABLE_MEMB)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.SQLLITE.TABLE_USER)
            db.execSQL("DROP TABLE IF EXISTS " + Constants.SQLLITE.TABLE_HEAD)
            db.execSQL("DROP TABLE IF EXISTS " + Constants.SQLLITE.TABLE_MEMB)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onCreate(db)
    }

    fun truncateTables() {
        val db = this.writableDatabase

        db.delete(Constants.SQLLITE.TABLE_USER, null, null)
        db.delete(Constants.SQLLITE.TABLE_HEAD, null, null)
        db.delete(Constants.SQLLITE.TABLE_MEMB, null, null)

        db.close()
    }

    fun createUser(user: User) {
        val db = this.writableDatabase

        if (isUserExists(user.id)) {
            val values1 = ContentValues()
            values1.put(USER_SRID, user.id)
            values1.put(USER_USNM, user.username)
            values1.put(USER_UQKY, user.uniquekey)
            values1.put(USER_IMAG, user.image)
            values1.put(USER_ACTV, user.active)
            values1.put(USER_SYNC, user.sync)
            values1.put(USER_IMSY, user.img_sync)

            try {
                db.update(Constants.SQLLITE.TABLE_USER, values1, "$USER_DBID = ?", arrayOf(user.id.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val values1 = ContentValues()
            values1.put(USER_SRID, user.id)
            values1.put(USER_USNM, user.username)
            values1.put(USER_UQKY, user.uniquekey)
            values1.put(USER_IMAG, user.image)
            values1.put(USER_ACTV, user.active)
            values1.put(USER_SYNC, user.sync)
            values1.put(USER_IMSY, user.img_sync)

            try {
                db.insert(Constants.SQLLITE.TABLE_USER, null, values1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isUserExists(id: Long): Boolean {
        val db = this.readableDatabase

        var isExists = false

        try {
            val cursor = db.query(Constants.SQLLITE.TABLE_USER, arrayOf("*"), "$USER_DBID = $id", null, null, null, null)

            isExists = cursor.count > 0

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isExists
    }

    fun updateSyncUser(dbID: Long, id: String) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(USER_SRID, id)
        values1.put(USER_SYNC, 1)
        values1.put(USER_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_USER, values1, "$USER_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageUser(dbID: Long, image: String, img_sync: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(USER_IMAG, image)
        values1.put(USER_IMSY, img_sync)

        try {
            db.update(Constants.SQLLITE.TABLE_USER, values1, "$USER_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageSyncUser(dbID: Long) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(USER_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_USER, values1, "$USER_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteUser(dbID: Long): Int {
        val db = this.writableDatabase

        try {
            return db.delete(Constants.SQLLITE.TABLE_USER, "$USER_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    fun changeUserStatus(dbID: Long, active: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(USER_ACTV, active)

        try {
            db.update(Constants.SQLLITE.TABLE_USER, values1, "$USER_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchcUser(type : Int): List<User> {
        val db = this.readableDatabase

        val userArrayList = ArrayList<User>()

        try {
            var selection = "$USER_SYNC = 1 AND $USER_IMSY = 1"

            if (type == Constants.SQLLITE.SEL_UNSYNC) {
                selection = "$USER_SYNC = 0"
            } else if (type == Constants.SQLLITE.SEL_IMG_UNSYNC) {
                selection = "$USER_IMSY = 0"
            }

            val cursor = db.query(
                Constants.SQLLITE.TABLE_USER, arrayOf(
                    USER_DBID,
                    USER_SRID,
                    USER_USNM,
                    USER_UQKY,
                    USER_IMAG,
                    USER_ACTV,
                    USER_SYNC,
                    USER_IMSY),
                selection, null, null, null, "$USER_SRID ASC, $USER_DBID ASC")

            if (cursor.moveToFirst()) {
                do {
                    val user = User(cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7))

                    userArrayList.add(user)

                } while (cursor.moveToNext())
            }

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return userArrayList
    }

    fun fetchUserCount(): Long {
        val db = this.readableDatabase

        var rowCount = 0.toLong()

        try {
            rowCount = DatabaseUtils.queryNumEntries(db, Constants.SQLLITE.TABLE_USER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rowCount
    }

    fun fetchLastUserId(): Long {
        val db = this.readableDatabase

        var lastId = 0.toLong()

        try {
            val cursor = db.query(Constants.SQLLITE.TABLE_USER, arrayOf(USER_DBID), "$USER_DBID != 0", null, null, null, "$USER_DBID DESC")

            if (cursor.count > 0) {
                cursor.moveToFirst()
                lastId = cursor.getLong(0)
            }

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lastId
    }

    fun createMember(member: Member) {
        val db = this.writableDatabase

        if (isMemberExists(member.id)) {
            val values1 = ContentValues()
            values1.put(MEMB_SRID, member.id)
            values1.put(MEMB_HDNM, member.name)
            values1.put(MEMB_HSNM, member.housename)
            values1.put(MEMB_GEND, member.gender)
            values1.put(MEMB_DOBH, member.dob)
            values1.put(MEMB_STAR, member.star)
            values1.put(MEMB_ADD1, member.address1)
            values1.put(MEMB_ADD2, member.address2)
            values1.put(MEMB_LAND, member.landline)
            values1.put(MEMB_MOBL, member.mobile)
            values1.put(MEMB_ISHD, member.isfmhd)
            values1.put(MEMB_FHID, member.fmhdid)
            values1.put(MEMB_FHNM, member.headname)
            values1.put(MEMB_FHRL, member.relation)
            values1.put(MEMB_IMAG, member.image)
            values1.put(MEMB_ACTV, member.active)
            values1.put(MEMB_SYNC, member.sync)
            values1.put(MEMB_IMSY, member.img_sync)

            try {
                db.update(Constants.SQLLITE.TABLE_MEMB, values1, "$MEMB_DBID = ?", arrayOf(member.id.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            val values1 = ContentValues()
            values1.put(MEMB_SRID, member.id)
            values1.put(MEMB_HDNM, member.name)
            values1.put(MEMB_HSNM, member.housename)
            values1.put(MEMB_GEND, member.gender)
            values1.put(MEMB_DOBH, member.dob)
            values1.put(MEMB_STAR, member.star)
            values1.put(MEMB_ADD1, member.address1)
            values1.put(MEMB_ADD2, member.address2)
            values1.put(MEMB_LAND, member.landline)
            values1.put(MEMB_MOBL, member.mobile)
            values1.put(MEMB_ISHD, member.isfmhd)
            values1.put(MEMB_FHID, member.fmhdid)
            values1.put(MEMB_FHNM, member.headname)
            values1.put(MEMB_FHRL, member.relation)
            values1.put(MEMB_IMAG, member.image)
            values1.put(MEMB_ACTV, member.active)
            values1.put(MEMB_SYNC, member.sync)
            values1.put(MEMB_IMSY, member.img_sync)

            try {
                db.insert(Constants.SQLLITE.TABLE_MEMB, null, values1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isMemberExists(id: Long): Boolean {
        val db = this.readableDatabase

        var isExists = false

        try {
            val cursor = db.query(Constants.SQLLITE.TABLE_MEMB, arrayOf("*"), "$MEMB_DBID = $id", null, null, null, null)

            isExists = cursor.count > 0

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isExists
    }

    fun updateSyncMember(dbID: Long, id: String) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(MEMB_SRID, id)
        values1.put(MEMB_SYNC, 1)
        values1.put(MEMB_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_MEMB, values1, "$MEMB_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageMember(dbID: Long, image: String, img_sync: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(MEMB_IMAG, image)
        values1.put(MEMB_IMSY, img_sync)

        try {
            db.update(Constants.SQLLITE.TABLE_MEMB, values1, "$MEMB_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageSyncMember(dbID: Long) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(MEMB_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_MEMB, values1, "$MEMB_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteMember(dbID: Long): Int {
        val db = this.writableDatabase

        try {
            return db.delete(Constants.SQLLITE.TABLE_MEMB, "$MEMB_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    fun changeMemberStatus(dbID: Long, active: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(MEMB_ACTV, active)

        try {
            db.update(Constants.SQLLITE.TABLE_MEMB, values1, "$MEMB_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchcMember(type : Int): List<Member> {
        val db = this.readableDatabase

        val memberArrayList = ArrayList<Member>()

        try {
            var selection = "$MEMB_SYNC = 1 AND $MEMB_IMSY = 1"

            if (type == Constants.SQLLITE.SEL_UNSYNC) {
                selection = "$MEMB_SYNC = 0"
            } else if (type == Constants.SQLLITE.SEL_IMG_UNSYNC) {
                selection = "$MEMB_IMSY = 0"
            }

            val cursor = db.query(
                Constants.SQLLITE.TABLE_MEMB, arrayOf(
                    MEMB_DBID,
                    MEMB_SRID,
                    MEMB_HDNM,
                    MEMB_HSNM,
                    MEMB_GEND,
                    MEMB_DOBH,
                    MEMB_STAR,
                    MEMB_ADD1,
                    MEMB_ADD2,
                    MEMB_LAND,
                    MEMB_MOBL,
                    MEMB_ISHD,
                    MEMB_FHID,
                    MEMB_FHNM,
                    MEMB_FHRL,
                    MEMB_IMAG,
                    MEMB_ACTV,
                    MEMB_SYNC,
                    MEMB_IMSY),
                selection, null, null, null, "$MEMB_SRID ASC, $MEMB_DBID ASC")

            if (cursor.moveToFirst()) {
                do {
                    val member = Member(cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getInt(11),
                        cursor.getLong(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getString(15),
                        cursor.getInt(16),
                        cursor.getInt(17),
                        cursor.getInt(18))

                    memberArrayList.add(member)

                } while (cursor.moveToNext())
            }

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return memberArrayList
    }

    fun fetchMemberCount(): Long {
        val db = this.readableDatabase

        var rowCount = 0.toLong()

        try {
            rowCount = DatabaseUtils.queryNumEntries(db, Constants.SQLLITE.TABLE_MEMB)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rowCount
    }

    fun createFamilyHead(familyHead: FamilyHead) {
        val db = this.writableDatabase

        if (isFamilyHeadExists(familyHead.id)) {
            val values1 = ContentValues()
            values1.put(HEAD_SRID, familyHead.id)
            values1.put(HEAD_HDNM, familyHead.headname)
            values1.put(HEAD_MBID, familyHead.memberid)
            values1.put(USER_IMAG, familyHead.image)
            values1.put(USER_ACTV, familyHead.active)
            values1.put(USER_SYNC, familyHead.sync)
            values1.put(USER_IMSY, familyHead.img_sync)

            try {
                db.update(Constants.SQLLITE.TABLE_HEAD, values1, "$HEAD_DBID = ?", arrayOf(familyHead.id.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val values1 = ContentValues()
            values1.put(HEAD_SRID, familyHead.id)
            values1.put(HEAD_HDNM, familyHead.headname)
            values1.put(HEAD_MBID, familyHead.memberid)
            values1.put(USER_IMAG, familyHead.image)
            values1.put(USER_ACTV, familyHead.active)
            values1.put(USER_SYNC, familyHead.sync)
            values1.put(USER_IMSY, familyHead.img_sync)

            try {
                db.insert(Constants.SQLLITE.TABLE_HEAD, null, values1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isFamilyHeadExists(id: Long): Boolean {
        val db = this.readableDatabase

        var isExists = false

        try {
            val cursor = db.query(Constants.SQLLITE.TABLE_HEAD, arrayOf("*"), "$HEAD_DBID = $id",
                null, null, null, null)

            isExists = cursor.count > 0

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isExists
    }

    fun updateSyncFamilyHead(dbID: Long, id: String) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(HEAD_SRID, id)
        values1.put(HEAD_SYNC, 1)
        values1.put(HEAD_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_HEAD, values1, "$HEAD_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageFamilyHead(dbID: Long, image: String, img_sync: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(HEAD_IMAG, image)
        values1.put(HEAD_IMSY, img_sync)

        try {
            db.update(Constants.SQLLITE.TABLE_HEAD, values1, "$HEAD_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImageSyncFamilyHead(dbID: Long) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(HEAD_IMSY, 1)

        try {
            db.update(Constants.SQLLITE.TABLE_HEAD, values1, "$HEAD_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFamilyHead(dbID: Long): Int {
        val db = this.writableDatabase

        try {
            return db.delete(Constants.SQLLITE.TABLE_HEAD, "$HEAD_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    fun changeFamilyHeadStatus(dbID: Long, active: Boolean) {
        val db = this.writableDatabase

        val values1 = ContentValues()
        values1.put(HEAD_ACTV, active)

        try {
            db.update(Constants.SQLLITE.TABLE_HEAD, values1, "$HEAD_DBID = ?", arrayOf(dbID.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchFamilyHead(type : Int): List<FamilyHead> {
        val db = this.readableDatabase

        val familyHeadArrayList = ArrayList<FamilyHead>()

        try {
            var selection = "$HEAD_SYNC = 1 AND $HEAD_IMSY = 1"

            if (type == Constants.SQLLITE.SEL_UNSYNC) {
                selection = "$HEAD_SYNC = 0"
            } else if (type == Constants.SQLLITE.SEL_IMG_UNSYNC) {
                selection = "$HEAD_IMSY = 0"
            }

            val cursor = db.query(
                Constants.SQLLITE.TABLE_HEAD, arrayOf(
                    HEAD_DBID,
                    HEAD_SRID,
                    HEAD_HDNM,
                    HEAD_MBID,
                    HEAD_IMAG,
                    HEAD_ACTV,
                    HEAD_SYNC,
                    HEAD_IMSY),
                selection, null, null, null, "$HEAD_SRID ASC, $HEAD_DBID ASC")

            if (cursor.moveToFirst()) {
                do {
                    val familyHead = FamilyHead(cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7))

                    familyHeadArrayList.add(familyHead)

                } while (cursor.moveToNext())
            }

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return familyHeadArrayList
    }

    fun fetchFamilyHeadCount(): Long {
        val db = this.readableDatabase

        var rowCount = 0.toLong()

        try {
            rowCount = DatabaseUtils.queryNumEntries(db, Constants.SQLLITE.TABLE_HEAD)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rowCount
    }
}