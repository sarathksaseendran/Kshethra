package com.sarathk.sk.kshethra.ui.member

import java.io.Serializable

class Relation (
    var id: String,
    var name: String
    ) : Serializable {
    override fun toString(): String {
        return "Relation {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}