package com.sarathk.sk.kshethra.ui.familyheads

import java.io.Serializable

class FamilyHead (
    var db_id: Long,
    var id: Long,
    var headname: String,
    var memberid: Long,
    var image: String,
    var active: Int,
    var sync: Int,
    var img_sync: Int
) : Serializable {
    override fun toString(): String {
        return "FamilyHead {" +
                "db_id='" + db_id + '\'' +
                ", id='" + id + '\'' +
                ", headname='" + headname + '\'' +
                ", memberid='" + memberid + '\'' +
                ", image='" + image + '\'' +
                ", active='" + active + '\'' +
                ", sync='" + sync + '\'' +
                ", img_sync='" + img_sync + '\'' +
                '}'
    }
}
