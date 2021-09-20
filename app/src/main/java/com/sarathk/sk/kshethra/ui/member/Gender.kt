package com.sarathk.sk.kshethra.ui.member

import java.io.Serializable

class Gender (
    var id: String,
    var name: String
    ) : Serializable {
    override fun toString(): String {
        return "Gender {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}