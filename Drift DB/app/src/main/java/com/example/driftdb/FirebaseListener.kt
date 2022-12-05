package com.example.driftdb

interface FirebaseListener {
    fun onSuccess(flag: Boolean, numberCheck: String, userName: String)
}
