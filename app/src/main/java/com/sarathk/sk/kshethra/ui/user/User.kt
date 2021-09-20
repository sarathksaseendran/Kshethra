package com.sarathk.sk.kshethra.ui.user

import java.io.Serializable

class User(
    var db_id: Long,
    var id: Long,
    var username: String,
    var uniquekey: String,
    var image: String,
    var active: Int,
    var sync: Int,
    var img_sync: Int
) : Serializable {
    override fun toString(): String {
        return "User {" +
                "db_id='" + db_id + '\'' +
                ", id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", uniquekey='" + uniquekey + '\'' +
                ", image='" + image + '\'' +
                ", active='" + active + '\'' +
                ", sync='" + sync + '\'' +
                ", img_sync='" + img_sync + '\'' +
                '}'
    }
}
