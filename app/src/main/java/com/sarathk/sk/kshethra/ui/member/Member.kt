package com.sarathk.sk.kshethra.ui.member

import java.io.Serializable

class Member (
    var db_id: Long,
    var id: Long,
    var name: String,
    var housename: String,
    var gender: String,
    var dob: String,
    var star: Int,
    var address1: String,
    var address2: String,
    var landline: String,
    var mobile: String,
    var isfmhd: Int,
    var fmhdid: Long,
    var headname: String,
    var relation: String,
    var image: String,
    var active: Int,
    var sync: Int,
    var img_sync: Int
    ) : Serializable {
    override fun toString(): String {
        return "Member {" +
                "db_id='" + db_id + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", housename='" + housename + '\'' +
                ", gender='" + gender + '\'' +
                ", dob='" + dob + '\'' +
                ", star='" + star + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", landline='" + landline + '\'' +
                ", mobile='" + mobile + '\'' +
                ", isfmhd='" + isfmhd + '\'' +
                ", fmhdid='" + fmhdid + '\'' +
                ", headname='" + headname + '\'' +
                ", relation='" + relation + '\'' +
                ", image='" + image + '\'' +
                ", active='" + active + '\'' +
                ", sync='" + sync + '\'' +
                ", img_sync='" + img_sync + '\'' +
                '}'
    }
}