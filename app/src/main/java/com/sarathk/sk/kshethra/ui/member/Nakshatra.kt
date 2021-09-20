package com.sarathk.sk.kshethra.ui.member

import java.io.Serializable

class Nakshatra (
    var id: Int,
    var name: String
    ) : Serializable {
    override fun toString(): String {
        return "Nakshatra {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}