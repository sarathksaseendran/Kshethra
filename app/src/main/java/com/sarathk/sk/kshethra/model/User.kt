package com.sarathk.sk.kshethra.model

data class User(
    val fullName: String,
    val mobileNumber: String,
    val profilePicture: String?,
    val registrationTokens: MutableList<String>
) {
    constructor() : this("", "", null, registrationTokens = mutableListOf())
}